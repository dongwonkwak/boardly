#!/usr/bin/env node
/**
 * notion-reset-db.js (with mapping clear)
 *
 * - DB 안의 모든 페이지를 아카이브
 * - --recreate 옵션을 주면 새 DB 생성
 * - 실행 후 script/notion-mapping.json 파일도 자동 초기화
 */

const { Client } = require('@notionhq/client');
const fs = require('fs');
const path = require('path');

try { require('dotenv').config(); } catch (_) { }

function requireEnv(key) {
  const v = process.env[key];
  if (!v) {
    console.error(`❌ Missing env: ${key}`);
    process.exit(1);
  }
  return v;
}

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

async function archiveAllPages(notion, databaseId) {
  let start_cursor = undefined;
  let total = 0, ok = 0, fail = 0;
  console.log(`🧹 Archiving all pages in DB: ${databaseId}`);
  do {
    const resp = await withRetry(() => notion.databases.query({ database_id: databaseId, start_cursor }));
    for (const page of resp.results) {
      total++;
      try {
        await withRetry(() => notion.pages.update({ page_id: page.id, archived: true }));
        ok++;
      } catch (e) {
        console.error(`❌ Archive failed: ${page.id} → ${e.message}`);
        fail++;
      }
    }
    if (!resp.has_more) break;
    start_cursor = resp.next_cursor;
  } while (true);
  console.log(`✅ Archive done. matched: ${total}, archived: ${ok}, failed: ${fail}`);
}

async function recreateDatabase(notion, parentPageId, title) {
  console.log(`🏗️ Creating new database under parent: ${parentPageId}`);
  const db = await withRetry(() => notion.databases.create({
    parent: { type: 'page_id', page_id: parentPageId },
    title: [{ type: 'text', text: { content: title } }],
    properties: {
      "Name": { title: {} },
      "File Path": { rich_text: {} },
      "Last Updated": { date: {} },
    }
  }));
  console.log(`✅ New database created: ${db.id}`);
  return db.id;
}

function clearMappingFile() {
  const mappingPath = path.resolve(__dirname, 'notion-mapping.json');
  try {
    fs.writeFileSync(mappingPath, JSON.stringify({}, null, 2));
    console.log(`🗑️ Cleared mapping file: ${mappingPath}`);
  } catch (e) {
    console.warn(`⚠️ Failed to clear mapping file: ${e.message}`);
  }
}

async function main() {
  const args = process.argv.slice(2);
  const shouldRecreate = args.includes('--recreate');

  const token = requireEnv('NOTION_API_TOKEN');
  const databaseId = requireEnv('NOTION_DATABASE_ID');
  const notion = new Client({ auth: token });

  await archiveAllPages(notion, databaseId);

  if (shouldRecreate) {
    const parentId = requireEnv('NOTION_PARENT_PAGE_ID');
    const title = process.env.NEW_DATABASE_TITLE || 'Docs';
    const newDbId = await recreateDatabase(notion, parentId, title);
    console.log('\n📌 새로 생성된 데이터베이스 ID를 .env에 업데이트하세요:');
    console.log(`NOTION_DATABASE_ID=${newDbId}`);
  }

  // Always clear mapping file after reset
  clearMappingFile();
}

if (require.main === module) {
  main().catch(e => { console.error('💥 Fatal:', e.message); process.exit(1); });
}