package com.boardly.features.board.application.usecase;

import com.boardly.features.board.application.port.input.ArchiveBoardCommand;
import com.boardly.features.board.domain.model.Board;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;

/**
 * 보드 아카이브 상태 변경 유스케이스 인터페이스
 * 
 * <p>이 인터페이스는 보드의 아카이브 상태를 변경하는 비즈니스 로직을 정의합니다.
 * 보드 내용 수정과 분리하여 단일 책임 원칙을 준수합니다.
 * 
 * <p>구현체는 다음과 같은 책임을 가집니다:
 * <ul>
 *   <li>보드 존재 여부 확인</li>
 *   <li>보드 수정 권한 검증 (요청자가 보드 소유자인지 확인)</li>
 *   <li>아카이브/언아카이브 상태 변경</li>
 *   <li>업데이트된 보드 저장</li>
 * </ul>
 * 
 * @since 1.0.0
 * @author Boardly Team
 */
public interface ArchiveBoardUseCase {

    /**
     * 보드를 아카이브합니다.
     * 
     * <p>아카이브된 보드는 읽기 전용이 되며, 내용 수정이 불가능합니다.
     * 
     * @param command 보드 아카이브에 필요한 정보를 담은 커맨드 객체
     * @return 아카이브 결과 (성공 시 아카이브된 보드, 실패 시 실패 정보)
     * 
     * @see ArchiveBoardCommand
     * @see Board
     * @see Failure
     */
    Either<Failure, Board> archiveBoard(ArchiveBoardCommand command);

    /**
     * 보드를 언아카이브합니다.
     * 
     * <p>언아카이브된 보드는 다시 활성 상태가 되며, 내용 수정이 가능합니다.
     * 
     * @param command 보드 언아카이브에 필요한 정보를 담은 커맨드 객체
     * @return 언아카이브 결과 (성공 시 활성화된 보드, 실패 시 실패 정보)
     * 
     * @see ArchiveBoardCommand
     * @see Board
     * @see Failure
     */
    Either<Failure, Board> unarchiveBoard(ArchiveBoardCommand command);
} 