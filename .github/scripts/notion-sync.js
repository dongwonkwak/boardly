#!/usr/bin/env node

const { Client } = require('@notionhq/client');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

/**
 * GitHub Actions용 Notion 동기화 스크립트
 */
class GitHubNotionSync {
  constructor() {
    // 환경 변수 확인
    this.validateEnvironment();
    
    this.notion = new Client({
      auth: process.env.NOTION_API_TOKEN,
    });
    
    this.databaseId = process.env.NOTION_DATABASE_ID;
    this.workspace = process.env.GITHUB_WORKSPACE;
    this.docsPath = path.join(this.workspace, 'docs');
    this.mappingFile = path.join(this.workspace, '.notion-sync/page-mapping.json');
    
    // 페이지 매핑 로드
    this.pageMapping = this.loadPageMapping();
  }

  validateEnvironment() {
    if (!process.env.NOTION_API_TOKEN) {
      throw new Error('NOTION_API_TOKEN 환경 변수가 필요합니다. GitHub Secrets에 설정해주세요.');
    }
    if (!process.env.NOTION_DATABASE_ID) {
      throw new Error('NOTION_DATABASE_ID 환경 변수가 필요합니다. GitHub Secrets에 설정해주세요.');
    }
    if (!process.env.GITHUB_WORKSPACE) {
      throw new Error('GITHUB_WORKSPACE 환경 변수가 설정되지 않았습니다.');
    }
  }

  loadPageMapping() {
    try {
      if (fs.existsSync(this.mappingFile)) {
        const content = fs.readFileSync(this.mappingFile, 'utf8');
        const mapping = JSON.parse(content);
        
        // 기존 형식이 단순 key-value라면 우리 형식으로 변환
        if (!mapping.mappings && !mapping.hashes) {
          return {
            mappings: mapping,
            hashes: {},
            lastSync: null
          };
        }
        return mapping;
      }
    } catch (error) {
      console.warn('페이지 매핑 파일을 읽을 수 없습니다:', error.message);
    }
    return { mappings: {}, hashes: {}, lastSync: null };
  }

  savePageMapping() {
    try {
      // 디렉토리가 없으면 생성
      const dir = path.dirname(this.mappingFile);
      if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
      }
      
      // 기존 형식과 호환성을 위해 mappings만 저장
      fs.writeFileSync(this.mappingFile, JSON.stringify(this.pageMapping.mappings, null, 2));
      console.log('✅ 페이지 매핑 정보가 저장되었습니다.');
    } catch (error) {
      console.error('❌ 페이지 매핑 저장 실패:', error.message);
    }
  }

  getFileHash(filePath) {
    try {
      const content = fs.readFileSync(filePath, 'utf8');
      return crypto.createHash('md5').update(content).digest('hex');
    } catch (error) {
      return null;
    }
  }

  generatePageTitle(relativePath) {
    const fileName = path.basename(relativePath, '.md');
    // kebab-case를 일반 제목으로 변환
    return fileName
      .split('-')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  markdownToNotionBlocks(content) {
    const lines = content.split('\n');
    const blocks = [];
    let inCodeBlock = false;
    let codeContent = [];
    let codeLanguage = '';
    
    for (const line of lines) {
      const trimmedLine = line.trim();
      
      // 코드 블록 처리
      if (trimmedLine.startsWith('```')) {
        if (!inCodeBlock) {
          // 코드 블록 시작
          inCodeBlock = true;
          codeLanguage = trimmedLine.replace('```', '').trim() || 'text';
          codeContent = [];
        } else {
          // 코드 블록 종료
          inCodeBlock = false;
          blocks.push({
            type: 'code',
            code: {
              rich_text: [{
                type: 'text',
                text: { content: codeContent.join('\n') }
              }],
              language: codeLanguage
            }
          });
          codeContent = [];
        }
        continue;
      }
      
      if (inCodeBlock) {
        codeContent.push(line);
        continue;
      }
      
      if (!trimmedLine) {
        continue; // 빈 줄 스킵
      }
      
      // 헤딩 처리
      if (trimmedLine.startsWith('#')) {
        const level = Math.min(trimmedLine.match(/^#+/)[0].length, 3);
        const text = trimmedLine.replace(/^#+\s*/, '');
        
        blocks.push({
          type: `heading_${level}`,
          [`heading_${level}`]: {
            rich_text: [{
              type: 'text',
              text: { content: text }
            }]
          }
        });
      }
      // 리스트 아이템 처리
      else if (trimmedLine.startsWith('- ') || trimmedLine.startsWith('* ')) {
        const text = trimmedLine.replace(/^[-*]\s*/, '');
        blocks.push({
          type: 'bulleted_list_item',
          bulleted_list_item: {
            rich_text: [{
              type: 'text',
              text: { content: text }
            }]
          }
        });
      }
      // 번호 리스트 처리
      else if (/^\d+\.\s/.test(trimmedLine)) {
        const text = trimmedLine.replace(/^\d+\.\s*/, '');
        blocks.push({
          type: 'numbered_list_item',
          numbered_list_item: {
            rich_text: [{
              type: 'text',
              text: { content: text }
            }]
          }
        });
      }
      // 인용구 처리
      else if (trimmedLine.startsWith('> ')) {
        const text = trimmedLine.replace(/^>\s*/, '');
        blocks.push({
          type: 'quote',
          quote: {
            rich_text: [{
              type: 'text',
              text: { content: text }
            }]
          }
        });
      }
      // 일반 텍스트
      else {
        blocks.push({
          type: 'paragraph',
          paragraph: {
            rich_text: [{
              type: 'text',
              text: { content: trimmedLine }
            }]
          }
        });
      }
    }
    
    return blocks;
  }

  async createPage(relativePath, title, content, fileHash) {
    console.log(`📝 새 페이지 생성: ${title}`);

    const blocks = this.markdownToNotionBlocks(content);
    
    try {
      const response = await this.notion.pages.create({
        parent: { database_id: this.databaseId },
        properties: {
          '제목': {
            title: [{ text: { content: title } }]
          },
          '파일 경로': {
            rich_text: [{ text: { content: relativePath } }]
          },
          '마지막 업데이트': {
            date: { start: new Date().toISOString() }
          }
        },
        children: blocks.slice(0, 100) // Notion API 제한
      });

      // 매핑 정보 업데이트 (기존 형식에 맞게 docs/ 포함)
      const fullPath = `docs/${relativePath}`;
      this.pageMapping.mappings[fullPath] = response.id;
      this.pageMapping.hashes[fullPath] = fileHash;

      return { action: 'created', pageId: response.id, title };
    } catch (error) {
      console.error(`❌ 페이지 생성 실패 (${title}):`, error.message);
      throw error;
    }
  }

  async updatePage(pageId, relativePath, title, content, fileHash) {
    console.log(`🔄 페이지 업데이트: ${title}`);

    try {
      // 페이지 속성 업데이트
      await this.notion.pages.update({
        page_id: pageId,
        properties: {
          '제목': {
            title: [{ text: { content: title } }]
          },
          '마지막 업데이트': {
            date: { start: new Date().toISOString() }
          }
        }
      });

      // 기존 블록 삭제 후 새 블록 추가
      await this.replacePageContent(pageId, content);

      // 해시 업데이트 (기존 형식에 맞게 docs/ 포함)
      const fullPath = `docs/${relativePath}`;
      this.pageMapping.hashes[fullPath] = fileHash;

      return { action: 'updated', pageId, title };
    } catch (error) {
      console.error(`❌ 페이지 업데이트 실패 (${title}):`, error.message);
      throw error;
    }
  }

  async replacePageContent(pageId, content) {
    try {
      // 기존 블록 조회
      const response = await this.notion.blocks.children.list({
        block_id: pageId
      });

      // 기존 블록 삭제
      for (const block of response.results) {
        await this.notion.blocks.delete({
          block_id: block.id
        });
      }

      // 새 블록 추가
      const blocks = this.markdownToNotionBlocks(content);
      if (blocks.length > 0) {
        // 100개씩 나누어 추가
        const batchSize = 100;
        for (let i = 0; i < blocks.length; i += batchSize) {
          const batch = blocks.slice(i, i + batchSize);
          await this.notion.blocks.children.append({
            block_id: pageId,
            children: batch
          });
        }
      }
    } catch (error) {
      console.error(`❌ 페이지 내용 교체 실패:`, error.message);
      throw error;
    }
  }

  async syncFile(relativePath) {
    const fullPath = path.join(this.docsPath, relativePath);
    
    if (!fs.existsSync(fullPath)) {
      console.log(`⚠️  파일이 존재하지 않습니다: ${relativePath}`);
      return { action: 'skipped', reason: 'file_not_found' };
    }

    const fileHash = this.getFileHash(fullPath);
    const fullPathKey = `docs/${relativePath}`;
    const existingPageId = this.pageMapping.mappings?.[fullPathKey];
    const lastHash = this.pageMapping.hashes?.[fullPathKey];

    // 파일이 변경되지 않은 경우 스킵
    if (fileHash === lastHash && existingPageId) {
      console.log(`⏭️  변경사항 없음: ${relativePath}`);
      return { action: 'skipped', reason: 'no_changes' };
    }

    const content = fs.readFileSync(fullPath, 'utf8');
    const title = this.generatePageTitle(relativePath);

    if (existingPageId) {
      return await this.updatePage(existingPageId, relativePath, title, content, fileHash);
    } else {
      return await this.createPage(relativePath, title, content, fileHash);
    }
  }

  async sync() {
    console.log('🚀 GitHub Actions Notion 동기화를 시작합니다...');
    
    try {
      let filesToSync = [];
      let unmappedFiles = [];

      // 강제 동기화 모드인지 확인
      if (process.env.FORCE_SYNC === 'true') {
        console.log('🔄 강제 동기화 모드입니다.');
        // 모든 마크다운 파일 동기화
        filesToSync = this.getAllMarkdownFiles();
      } else {
        // 변경된 파일만 동기화
        const changedFilesEnv = process.env.CHANGED_FILES;
        if (changedFilesEnv) {
          filesToSync = changedFilesEnv.split('\n')
            .map(file => file.trim())
            .filter(file => file)
            .map(file => file.replace('docs/', ''));
        }

        // 🔍 추가: 노션에 매핑되지 않은 기존 파일들 확인
        const allFiles = this.getAllMarkdownFiles();
        unmappedFiles = allFiles.filter(file => {
          const fullPathKey = `docs/${file}`;
          return !this.pageMapping.mappings[fullPathKey] && !filesToSync.includes(file);
        });

        if (unmappedFiles.length > 0) {
          console.log(`📋 노션에 매핑되지 않은 ${unmappedFiles.length}개 파일 발견:`);
          unmappedFiles.forEach(file => console.log(`  - ${file}`));
          filesToSync = [...filesToSync, ...unmappedFiles];
        }
      }

      if (filesToSync.length === 0) {
        console.log('📝 동기화할 파일이 없습니다.');
        return;
      }

      console.log(`📚 총 ${filesToSync.length}개 파일을 동기화합니다:`);
      const changedCount = filesToSync.filter(file => !unmappedFiles.includes(file)).length;
      if (changedCount > 0) {
        console.log(`🔄 변경된 파일: ${changedCount}개`);
      }
      if (unmappedFiles.length > 0) {
        console.log(`📋 새로 추가된 파일: ${unmappedFiles.length}개`);
      }
      filesToSync.forEach(file => {
        const isNew = unmappedFiles.includes(file);
        console.log(`  ${isNew ? '📄' : '🔄'} ${file}`);
      });

      // 각 파일 동기화
      const results = [];
      for (const file of filesToSync) {
        try {
          const result = await this.syncFile(file);
          results.push({ file, success: true, result });
          console.log(`✅ ${file}: ${result.action}`);
        } catch (error) {
          results.push({ file, success: false, error: error.message });
          console.error(`❌ ${file}: ${error.message}`);
        }
      }

      // 매핑 정보 저장
      this.savePageMapping();

      // 결과 요약
      const successCount = results.filter(r => r.success).length;
      const failCount = results.filter(r => !r.success).length;
      
      console.log(`\n📊 동기화 완료: 성공 ${successCount}개, 실패 ${failCount}개`);
      
      if (failCount > 0) {
        console.log('\n❌ 실패한 파일들:');
        results.filter(r => !r.success).forEach(f => {
          console.log(`  - ${f.file}: ${f.error}`);
        });
        process.exit(1);
      }

    } catch (error) {
      console.error('💥 동기화 중 오류 발생:', error.message);
      process.exit(1);
    }
  }

  getAllMarkdownFiles() {
    const files = [];
    
    function scanDirectory(currentPath, basePath) {
      const items = fs.readdirSync(currentPath);
      
      for (const item of items) {
        const fullPath = path.join(currentPath, item);
        const stat = fs.statSync(fullPath);
        
        if (stat.isDirectory()) {
          scanDirectory(fullPath, basePath);
        } else if (path.extname(fullPath).toLowerCase() === '.md') {
          const relativePath = path.relative(basePath, fullPath);
          files.push(relativePath);
        }
      }
    }
    
    scanDirectory(this.docsPath, this.docsPath);
    return files;
  }
}

// 스크립트 실행
async function main() {
  try {
    const sync = new GitHubNotionSync();
    await sync.sync();
  } catch (error) {
    console.error('💥 실행 실패:', error.message);
    process.exit(1);
  }
}

main();
