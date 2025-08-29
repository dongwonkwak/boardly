#!/bin/bash

# Git 커밋 메시지 템플릿 설정 스크립트

echo "🔧 Git 커밋 메시지 템플릿 설정 중..."

# 현재 디렉토리가 git 저장소인지 확인
if [ ! -d ".git" ]; then
    echo "❌ 이 디렉토리는 Git 저장소가 아닙니다."
    echo "   프로젝트 루트 디렉토리에서 실행해주세요."
    exit 1
fi

# Git 커밋 메시지 템플릿 설정
git config commit.template .gitmessage

if [ $? -eq 0 ]; then
    echo "✅ Git 커밋 메시지 템플릿이 설정되었습니다."
    echo "   이제 'git commit' 명령어 실행 시 템플릿이 자동으로 로드됩니다."
else
    echo "❌ Git 커밋 메시지 템플릿 설정에 실패했습니다."
    exit 1
fi

# 현재 설정 확인
echo ""
echo "📋 현재 Git 설정:"
echo "   Commit Template: $(git config --get commit.template)"

echo ""
echo "🎯 사용법:"
echo "   1. 'git add <files>' 로 파일을 스테이징"
echo "   2. 'git commit' 실행 (에디터가 열리면서 템플릿 표시)"
echo "   3. 템플릿에 따라 커밋 메시지 작성"
echo ""
echo "💡 빠른 커밋을 위한 예시:"
echo "   git commit -m 'feat(backend:user): add password validation'"
echo "   git commit -m 'fix(frontend:board): resolve drag and drop bug'"
