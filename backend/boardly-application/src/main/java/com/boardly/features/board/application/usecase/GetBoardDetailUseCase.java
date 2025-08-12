package com.boardly.features.board.application.usecase;

import com.boardly.features.board.application.query.GetBoardDetailQuery;
import com.boardly.features.board.application.dto.BoardDetailDto;
import com.boardly.shared.common.error.Failure;

import io.vavr.control.Either;

/**
 * 보드 상세 조회 유스케이스
 * 
 * <p>
 * 보드의 상세 정보를 조회하는 유스케이스입니다.
 * 
 * @since 1.0.0
 */
public interface GetBoardDetailUseCase {

    /**
     * 보드 상세 정보를 조회합니다.
     * 
     * @param command 보드 상세 조회 커맨드
     * @return 보드 상세 DTO 또는 실패
     */
    Either<Failure, BoardDetailDto> getBoardDetail(GetBoardDetailQuery command);
}