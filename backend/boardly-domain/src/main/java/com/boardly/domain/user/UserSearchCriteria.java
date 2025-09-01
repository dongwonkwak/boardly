package com.boardly.domain.user;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 사용자 검색 조건
 */
@Getter
@Builder
public class UserSearchCriteria {

    /**
     * 사용자명 검색어 (부분 일치)
     */
    private final String usernameKeyword;

    /**
     * 이메일 검색어 (부분 일치)
     */
    private final String emailKeyword;

    /**
     * 사용자 상태
     */
    private final UserStatus status;

    /**
     * 생성일 시작 범위
     */
    private final LocalDateTime createdAfter;

    /**
     * 생성일 종료 범위
     */
    private final LocalDateTime createdBefore;

    /**
     * 페이지 번호 (0부터 시작)
     */
    private final Integer page;

    /**
     * 페이지 크기
     */
    private final Integer size;

    /**
     * 정렬 필드
     */
    private final String sortField;

    /**
     * 정렬 방향 (ASC, DESC)
     */
    private final String sortDirection;
}
