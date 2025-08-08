package com.boardly.features.activity.application.port.input;

import java.time.Instant;
import java.util.Optional;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.user.domain.model.UserId;

/**
 * 활동 조회를 위한 쿼리 객체
 * 사용자 ID, 보드 ID, 시간 범위, 페이징 정보를 포함
 */
public record GetActivityQuery(
        UserId userId,
        BoardId boardId,
        Instant since,
        Instant until,
        int page,
        int size) {
    /**
     * 특정 보드의 활동을 조회하기 위한 쿼리 생성
     * 기본 페이지 크기(50)와 첫 번째 페이지(0)를 사용
     * 
     * @param boardId 조회할 보드 ID
     * @return GetActivityQuery 객체
     */
    public static GetActivityQuery forBoard(BoardId boardId) {
        return new GetActivityQuery(null, boardId, null, null, 0, 50);
    }

    /**
     * 특정 보드의 활동을 페이징과 함께 조회하기 위한 쿼리 생성
     * 
     * @param boardId 조회할 보드 ID
     * @param page    페이지 번호 (0부터 시작)
     * @param size    페이지 크기
     * @return GetActivityQuery 객체
     */
    public static GetActivityQuery forBoardWithPagination(
            BoardId boardId,
            int page,
            int size) {
        return new GetActivityQuery(null, boardId, null, null, page, size);
    }

    /**
     * 특정 사용자의 활동을 조회하기 위한 쿼리 생성
     * 기본 페이지 크기(50)와 첫 번째 페이지(0)를 사용
     * 
     * @param userId 조회할 사용자 ID
     * @return GetActivityQuery 객체
     */
    public static GetActivityQuery forUser(UserId userId) {
        return new GetActivityQuery(userId, null, null, null, 0, 50);
    }

    /**
     * 특정 사용자의 활동을 페이징과 함께 조회하기 위한 쿼리 생성
     * 
     * @param userId 조회할 사용자 ID
     * @param page   페이지 번호 (0부터 시작)
     * @param size   페이지 크기
     * @return GetActivityQuery 객체
     */
    public static GetActivityQuery forUserWithPagination(
            UserId userId,
            int page,
            int size) {
        return new GetActivityQuery(userId, null, null, null, page, size);
    }

    /**
     * 특정 보드의 특정 시점 이후 활동을 조회하기 위한 쿼리 생성
     * 기본 페이지 크기(50)와 첫 번째 페이지(0)를 사용
     * 
     * @param boardId 조회할 보드 ID
     * @param since   조회 시작 시점
     * @return GetActivityQuery 객체
     */
    public static GetActivityQuery forBoardSince(
            BoardId boardId,
            Instant since) {
        return new GetActivityQuery(null, boardId, since, null, 0, 50);
    }

    /**
     * 페이지 번호를 반환하거나 기본값(0)을 반환
     * 
     * @return 페이지 번호 (기본값: 0)
     */
    public int getPageOrDefault() {
        return Optional.ofNullable(page).orElse(0);
    }

    /**
     * 페이지 크기를 반환하거나 기본값(50)을 반환
     * 
     * @return 페이지 크기 (기본값: 50)
     */
    public int getSizeOrDefault() {
        return Optional.ofNullable(size).orElse(50);
    }
}
