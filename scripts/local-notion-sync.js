#!/usr/bin/env node
/**
 * local-notion-sync.upsert.fixmap.nopath.js
 * - DB에 "File Path"가 없을 때도 오류 없이 동작 (해당 조회/업데이트 자동 비활성화)
 * - 업서트: mapping -> (File Path 조회: available일 때만) -> Title 조회 -> create
 * - 항상 가능한 경우에만 File Path/Last Updated 갱신
 * - 진단/청크/ID기반/페이지네이션 삭제/self-heal/backup 제외/빈 매핑 안내 로그 유지
 */

const { Client } = require('@notionhq/client');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { execSync } = require('child_process');

try { require('dotenv').config(); } catch (_) { }

const DEFAULT_PROP_NAMES = {
  title: process.env.NOTION_TITLE_PROP_NAME || 'Name',
  filePath: process.env.NOTION_FILEPATH_PROP_NAME || 'File Path',
  updated: process.env.NOTION_UPDATED_PROP_NAME || 'Last Updated',
};
const CONTINUE_ON_ERROR = String(process.env.NOTION_CONTINUE_ON_ERROR || '').toLowerCase() === 'true';
const SAFE_CHUNK = 1900;
const EXCLUDE_DIRS = (process.env.EXCLUDE_DIRS || '.git,.github,node_modules,backup')
  .split(',').map(s => s.trim()).filter(Boolean);

async function withRetry(fn, { retries = 3, baseDelayMs = 500 } = {}) {
  let attempt = 0;
  while (true) {
    try { return await fn(); }
    catch (err) {
      attempt++;
      const status = err?.status || err?.statusCode;
      const retriable = status === 429 || (status >= 500 && status < 600);
      if (!retriable || attempt > retries) throw err;
      const delay = baseDelayMs * Math.pow(2, attempt - 1);
      await new Promise(r => setTimeout(r, delay));
    }
  }
}

const LANG_ALIAS = {
  '': 'plain text', 'text': 'plain text', 'plaintext': 'plain text',
  'sh': 'bash', 'zsh': 'bash', 'shell': 'bash', 'js': 'javascript', 'jsx': 'javascript',
  'ts': 'typescript', 'tsx': 'typescript', 'yml': 'yaml', 'csharp': 'c#', 'objc': 'objective-c', 'md': 'markdown',
  'properties': 'plain text', 'mermaidjs': 'mermaid', 'mermaid.js': 'mermaid',
};
function normalizeLang(s) { const k = String(s || '').trim().toLowerCase(); return LANG_ALIAS[k] || k || 'plain text'; }
function chunkText(s, n = SAFE_CHUNK) { const out = []; for (let i = 0; i < s.length; i += n) out.push(s.slice(i, i + n)); return out; }
function toRT(s) { return chunkText(s).map(p => ({ type: 'text', text: { content: p } })); }

class NotionSyncUpsertNoPathGuard {
  constructor() {
    if (!process.env.NOTION_API_TOKEN) { console.error('❌ NOTION_API_TOKEN required'); process.exit(1); }
    if (!process.env.NOTION_DATABASE_ID) { console.error('❌ NOTION_DATABASE_ID required'); process.exit(1); }
    this.notion = new Client({ auth: process.env.NOTION_API_TOKEN });
    this.databaseId = process.env.NOTION_DATABASE_ID;
    this.root = process.cwd();
    this.docsPath = path.resolve(__dirname, '../docs');
    this.mappingFile = path.resolve(__dirname, 'notion-mapping.json');
    this.pageMapping = this._loadMap();
    if (!this.pageMapping || typeof this.pageMapping !== 'object') this.pageMapping = {};
    if (!this.pageMapping.mappings || typeof this.pageMapping.mappings !== 'object') this.pageMapping.mappings = {};
    if (!this.pageMapping.hashes || typeof this.pageMapping.hashes !== 'object') this.pageMapping.hashes = {};
    if (!('lastSync' in this.pageMapping)) this.pageMapping.lastSync = null;

    this.propIds = { titleId: '', filePathId: '', updatedId: '' };
    this.hasFilePath = false;
    this.hasUpdated = false;
  }

  _loadMap() {
    try {
      if (fs.existsSync(this.mappingFile)) {
        const raw = fs.readFileSync(this.mappingFile, 'utf8').trim();
        if (!raw) return { mappings: {}, hashes: {}, lastSync: null };
        const obj = JSON.parse(raw);
        return {
          mappings: obj.mappings && typeof obj.mappings === 'object' ? obj.mappings : {},
          hashes: obj.hashes && typeof obj.hashes === 'object' ? obj.hashes : {},
          lastSync: 'lastSync' in obj ? obj.lastSync : null,
        };
      }
    } catch (e) { console.warn('⚠️ mapping read fail:', e.message); }
    return { mappings: {}, hashes: {}, lastSync: null };
  }

  _saveMap() {
    try { this.pageMapping.lastSync = new Date().toISOString(); fs.writeFileSync(this.mappingFile, JSON.stringify(this.pageMapping, null, 2)); }
    catch (e) { console.error('❌ map save fail:', e.message); }
  }

  async initPropertyIds() {
    const db = await withRetry(() => this.notion.databases.retrieve({ database_id: this.databaseId }));
    const props = db.properties || {};
    const titleEntry = Object.entries(props).find(([, v]) => v.type === 'title');
    if (!titleEntry) throw new Error('Title property not found');
    const findByName = (name) => { const e = Object.entries(props).find(([k]) => k === name); return e ? e[1].id : null; };
    this.propIds = {
      titleId: titleEntry[1].id,
      filePathId: findByName(DEFAULT_PROP_NAMES.filePath),
      updatedId: findByName(DEFAULT_PROP_NAMES.updated),
    };
    this.hasFilePath = !!this.propIds.filePathId;
    this.hasUpdated = !!this.propIds.updatedId;
    console.log('🧭 Property IDs:', this.propIds);
    if (!this.hasFilePath) console.log('ℹ️ "File Path" 속성이 없어 경로 기반 중복 체크/업데이트를 건너뜁니다.');
    if (!this.hasUpdated) console.log('ℹ️ "Last Updated" 속성이 없어 갱신 시간 기록을 건너뜁니다.');
  }

  _titleFromPath(rel) {
    const base = path.basename(rel, '.md'); const WL = new Set(['API', 'ERD', 'DDD', 'SQL', 'HTTP', 'JWT']);
    return base.split('-').map(w => WL.has(w.toUpperCase()) ? w.toUpperCase() : w[0].toUpperCase() + w.slice(1)).join(' ');
  }

  markdownToBlocksWithOrigins(text) {
    const lines = text.split('\n'); const blocks = []; const origins = [];
    let inCode = false, lang = '', buf = [], codeStart = 0;
    const push = (b, s, e, t) => { blocks.push(b); origins.push({ startLine: s, endLine: e, type: t }); };
    for (let i = 0; i < lines.length; i++) {
      const raw = lines[i]; const t = raw.trim();
      if (t.startsWith('```')) {
        if (!inCode) { inCode = true; lang = normalizeLang(t.replace(/^`{3,}/, '').trim().split(/[ \t{\[\(\:]/)[0]); buf = []; codeStart = i + 1; }
        else { inCode = false; push({ type: 'code', code: { language: normalizeLang(lang), rich_text: toRT(buf.join('\n')) } }, codeStart, i + 1, 'code'); buf = []; }
        continue;
      }
      if (inCode) { buf.push(raw); continue; }
      if (!t) continue;
      if (t.startsWith('#')) { const level = Math.min(t.match(/^#+/)[0].length, 3); const txt = t.replace(/^#+\s*/, ''); push({ type: `heading_${level}`, [`heading_${level}`]: { rich_text: toRT(txt) } }, i + 1, i + 1, `heading_${level}`); }
      else if (/^- \[( |x|X)\]\s+/.test(t)) { const checked = /^\- \[(x|X)\]\s+/.test(t); const txt = t.replace(/^- \[( |x|X)\]\s+/, ''); push({ type: 'to_do', to_do: { checked, rich_text: toRT(txt) } }, i + 1, i + 1, 'to_do'); }
      else if (/^>\s+/.test(t)) { const txt = t.replace(/^>\s+/, ''); push({ type: 'quote', quote: { rich_text: toRT(txt) } }, i + 1, i + 1, 'quote'); }
      else if (t.startsWith('- ') || t.startsWith('* ')) { const txt = t.replace(/^[-*]\s*/, ''); push({ type: 'bulleted_list_item', bulleted_list_item: { rich_text: toRT(txt) } }, i + 1, i + 1, 'bulleted_list_item'); }
      else if (/^\d+\.\s/.test(t)) { const txt = t.replace(/^\d+\.\s*/, ''); push({ type: 'numbered_list_item', numbered_list_item: { rich_text: toRT(txt) } }, i + 1, i + 1, 'numbered_list_item'); }
      else { push({ type: 'paragraph', paragraph: { rich_text: toRT(t) } }, i + 1, i + 1, 'paragraph'); }
    }
    return { blocks, origins };
  }

  async _appendBatches(pageId, blocks, origins, rel) {
    const batch = 100;
    for (let i = 0; i < blocks.length; i += batch) {
      const children = blocks.slice(i, i + batch); const o = origins.slice(i, i + batch);
      try { await withRetry(() => this.notion.blocks.children.append({ block_id: pageId, children })); }
      catch (e) {
        console.error(`\n❌ append 실패: ${rel} (batch ${i}-${i + children.length - 1}) → ${e.message}`);
        console.error('🔎 문제 블록을 개별 업로드로 탐지합니다...');
        for (let k = 0; k < children.length; k++) {
          const b = children[k]; const info = o[k];
          try { await withRetry(() => this.notion.blocks.children.append({ block_id: pageId, children: [b] })); }
          catch (ee) {
            console.error(`❌ 블록 업로드 실패: ${rel} L${info.startLine}${info.endLine !== info.startLine ? '-L' + info.endLine : ''} type=${info.type} → ${ee.message}`);
            if (CONTINUE_ON_ERROR) { console.warn('⚠️ CONTINUE_ON_ERROR=true: 건너뜀'); continue; } else { throw ee; }
          }
        }
      }
    }
  }
  async _deleteAll(pageId) {
    let cursor; do {
      const resp = await withRetry(() => this.notion.blocks.children.list({ block_id: pageId, start_cursor: cursor }));
      for (const b of resp.results) await withRetry(() => this.notion.blocks.delete({ block_id: b.id }));
      cursor = resp.next_cursor;
    } while (cursor);
  }

  async _queryByFilePath(rel) {
    if (!this.hasFilePath) return null; // 속성이 없으면 조회 자체를 건너뜀
    const resp = await withRetry(() => this.notion.databases.query({
      database_id: this.databaseId,
      filter: { property: DEFAULT_PROP_NAMES.filePath, rich_text: { equals: rel } },
      page_size: 2,
    }));
    return resp.results?.[0]?.id || null;
  }
  async _queryByTitle(title) {
    const resp = await withRetry(() => this.notion.databases.query({
      database_id: this.databaseId,
      filter: { property: DEFAULT_PROP_NAMES.title, title: { equals: title } },
      page_size: 2,
    }));
    return resp.results?.[0]?.id || null;
  }

  _propsForCreate(rel, title) {
    const p = {};
    p[this.propIds.titleId] = { title: [{ text: { content: title } }] };
    if (this.hasFilePath) p[this.propIds.filePathId] = { rich_text: [{ text: { content: rel } }] };
    if (this.hasUpdated) p[this.propIds.updatedId] = { date: { start: new Date().toISOString() } };
    return p;
  }
  _propsForUpdate(rel, title) {
    const p = {};
    p[this.propIds.titleId] = { title: [{ text: { content: title } }] };
    if (this.hasFilePath) p[this.propIds.filePathId] = { rich_text: [{ text: { content: rel } }] };
    if (this.hasUpdated) p[this.propIds.updatedId] = { date: { start: new Date().toISOString() } };
    return p;
  }

  async createPage(rel, title, content, hash) {
    const page = await withRetry(() => this.notion.pages.create({
      parent: { database_id: this.databaseId }, properties: this._propsForCreate(rel, title)
    }));
    const { blocks, origins } = this.markdownToBlocksWithOrigins(content);
    await this._appendBatches(page.id, blocks, origins, rel);
    this.pageMapping.mappings[rel] = page.id; this.pageMapping.hashes[rel] = hash;
    return { action: 'created', pageId: page.id };
  }

  async updatePage(pageId, rel, title, content, hash) {
    try {
      await withRetry(() => this.notion.pages.update({ page_id: pageId, properties: this._propsForUpdate(rel, title) }));
      await this._deleteAll(pageId);
      const { blocks, origins } = this.markdownToBlocksWithOrigins(content);
      await this._appendBatches(pageId, blocks, origins, rel);
      this.pageMapping.hashes[rel] = hash;
      return { action: 'updated', pageId };
    } catch (e) {
      if (String(e.message || '').includes('Invalid property identifier')) {
        console.warn(`⚠️ property invalid. Archive & recreate: ${rel}`);
        try { await withRetry(() => this.notion.pages.update({ page_id: pageId, archived: true })); } catch { }
        return await this.createPage(rel, title, content, hash);
      }
      throw e;
    }
  }

  _allMd() {
    const out = [];
    const walk = (dir, base) => {
      for (const item of fs.readdirSync(dir)) {
        const full = path.join(dir, item); const rel = path.relative(base, full); const st = fs.statSync(full);
        if (st.isDirectory()) {
          const top = rel.split(path.sep)[0];
          if (EXCLUDE_DIRS.includes(top)) continue;
          walk(full, base);
        } else if (full.toLowerCase().endsWith('.md')) out.push(rel);
      }
    };
    walk(this.docsPath, this.docsPath);
    return out;
  }
  _changed() {
    try {
      const d = execSync('git diff --name-only HEAD', { cwd: this.root, encoding: 'utf8' });
      const s = execSync('git diff --name-only --cached', { cwd: this.root, encoding: 'utf8' });
      const arr = [...d.split('\n'), ...s.split('\n')].filter(Boolean).filter(f => f.startsWith('docs/') && f.endsWith('.md')).map(f => f.replace('docs/', ''));
      return Array.from(new Set(arr.filter(rel => !EXCLUDE_DIRS.includes(rel.split('/')[0]))));
    } catch { return []; }
  }
  getTargets({ force = false, files = null }) {
    if (force) return this._allMd();
    if (files && files.length) return files.filter(rel => !EXCLUDE_DIRS.includes(rel.split('/')[0]));
    const changed = this._changed();
    const all = this._allMd();
    const mappings = (this.pageMapping && this.pageMapping.mappings) ? this.pageMapping.mappings : {};
    const unmapped = all.filter(f => !mappings[f] && !changed.includes(f));
    return Array.from(new Set([...changed, ...unmapped]));
  }

  async syncFile(rel) {
    const full = path.join(this.docsPath, rel);
    if (!fs.existsSync(full)) return { action: 'skipped', reason: 'file_not_found' };
    const text = fs.readFileSync(full, 'utf8');
    const hash = crypto.createHash('md5').update(text).digest('hex');
    const title = this._titleFromPath(rel);

    const mappedId = this.pageMapping.mappings?.[rel];
    if (mappedId) return await this.updatePage(mappedId, rel, title, text, hash);

    const byPathId = await this._queryByFilePath(rel); // hasFilePath=false면 내부에서 null 반환
    if (byPathId) {
      this.pageMapping.mappings[rel] = byPathId;
      return await this.updatePage(byPathId, rel, title, text, hash);
    }

    const byTitleId = await this._queryByTitle(title);
    if (byTitleId) {
      this.pageMapping.mappings[rel] = byTitleId;
      return await this.updatePage(byTitleId, rel, title, text, hash);
    }

    return await this.createPage(rel, title, text, hash);
  }

  async run(opts = {}) {
    console.log('🚀 Notion sync (Upsert + no-FilePath guard)');
    if (!this.pageMapping.mappings || Object.keys(this.pageMapping.mappings).length === 0) {
      console.log('⚠️ mapping is empty → 모든 문서를 새로 올립니다.');
    }
    await this.initPropertyIds();
    const targets = this.getTargets(opts);
    if (!targets.length) { console.log('📝 No files to sync.'); return; }

    const results = [];
    for (const f of targets) {
      try {
        const res = await this.syncFile(f);
        console.log(`✅ ${f}: ${res.action}`);
        results.push({ f, ok: true, action: res.action });
      } catch (e) {
        console.error(`❌ ${f}: ${e.message}`);
        results.push({ f, ok: false, error: e.message });
        if (!CONTINUE_ON_ERROR) { console.error('⛔ stop. set NOTION_CONTINUE_ON_ERROR=true to continue.'); break; }
      }
    }
    this._saveMap();
    const ok = results.filter(x => x.ok).length; const bad = results.length - ok;
    console.log(`\n📊 Done: success ${ok}, failed ${bad}`);
    if (bad) { console.log('❌ Failed:'); for (const r of results.filter(x => !x.ok)) console.log(` - ${r.f}: ${r.error}`); }
  }
}

async function main() {
  const args = process.argv.slice(2);
  const cmd = args[0];
  const app = new NotionSyncUpsertNoPathGuard();
  switch (cmd) {
    case 'force': await app.run({ force: true }); break;
    case 'file': if (!args[1]) { console.error('Usage: node local-notion-sync.upsert.fixmap.nopath.js file <relpath.md>'); process.exit(1); } await app.run({ files: [args[1]] }); break;
    default: await app.run(); break;
  }
}
if (require.main === module) main();
module.exports = NotionSyncUpsertNoPathGuard;