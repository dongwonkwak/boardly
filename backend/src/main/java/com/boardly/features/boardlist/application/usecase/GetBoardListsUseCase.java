package com.boardly.features.boardlist.application.usecase;

import com.boardly.features.boardlist.application.port.input.GetBoardListsCommand;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import java.util.List;


/**
 * 보드 리스트 목록 조회 유스케이스 인터페이스
 * 
 * <p>특정 보드에 속한 모든 리스트를 조회하는 비즈니스 로직을 정의합니다.
 * 리스트는 position 순서대로 정렬되어 반환됩니다.
 * 
 * @since 1.0.0
 */
public interface GetBoardListsUseCase {
  /**
   * 보드에 속한 모든 리스트를 조회합니다.
   * 
   * <p>이 메서드는 리스트 조회 프로세스를 처리하며, 다음과 같은 검증과 처리를 수행합니다:
   * <ul>
   *   <li>보드 존재 여부 확인</li>
   *   <li>보드 접근 권한 검증</li>
   *   <li>보드의 모든 리스트 조회</li>
   *   <li>position 순서대로 정렬</li>
   * </ul>
   * 
   * @param command 리스트 조회에 필요한 정보를 담은 커맨드 객체
   * @return 조회 결과 (성공 시 리스트 목록, 실패 시 실패 정보)
   */
  Either<Failure, List<BoardList>> getBoardLists(GetBoardListsCommand command);
}
