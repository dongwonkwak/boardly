package com.boardly.features.board.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.board.application.port.input.GetBoardDetailCommand;
import com.boardly.features.board.application.port.output.GetBoardDetailPort;
import com.boardly.features.board.application.port.output.GetBoardDetailPort.BoardDetailData;
import com.boardly.features.board.application.usecase.GetBoardDetailUseCase;
import com.boardly.features.board.application.dto.BoardDetailDto;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.application.validation.ValidationMessageResolver;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 보드 상세 조회 서비스
 * 
 * <p>
 * 보드의 상세 정보를 조회하는 서비스입니다.
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetBoardDetailService implements GetBoardDetailUseCase {

    private final GetBoardDetailPort getBoardDetailPort;
    private final ValidationMessageResolver validationMessageResolver;

    @Override
    public Either<Failure, BoardDetailDto> getBoardDetail(GetBoardDetailCommand command) {
        log.info("보드 상세 조회 시작: boardId={}, userId={}",
                command.boardId().getId(), command.userId().getId());

        try {
            // 보드 상세 데이터 조회
            Either<Failure, BoardDetailData> dataResult = getBoardDetailPort
                    .getBoardDetail(command.boardId(), command.userId());

            if (dataResult.isLeft()) {
                log.warn("보드 상세 데이터 조회 실패: boardId={}, error={}",
                        command.boardId().getId(), dataResult.getLeft().getMessage());
                return Either.left(dataResult.getLeft());
            }

            BoardDetailData data = dataResult.get();

            // BoardDetailDto 생성
            BoardDetailDto boardDetailDto = BoardDetailDto.of(
                    data.board(),
                    data.boardLists(),
                    data.boardMembers(),
                    data.labels(),
                    data.cards().values().stream().flatMap(List::stream).collect(Collectors.toList()),
                    data.users().values().stream().collect(Collectors.toList()));

            log.info("보드 상세 조회 완료: boardId={}", command.boardId().getId());
            return Either.right(boardDetailDto);

        } catch (Exception e) {
            log.error("보드 상세 조회 중 예외 발생: boardId={}, error={}",
                    command.boardId().getId(), e.getMessage(), e);

            String message = validationMessageResolver.getMessage("board.detail.get.error", "보드 상세 조회 중 오류가 발생했습니다.");
            return Either.left(Failure.ofInternalServerError(message));
        }
    }

}