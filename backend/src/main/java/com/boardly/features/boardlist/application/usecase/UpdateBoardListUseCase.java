package com.boardly.features.boardlist.application.usecase;

import com.boardly.features.boardlist.application.port.input.UpdateBoardListCommand;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;


/**
 * 보드 리스트 수정 유스케이스 인터페이스
 * 
 * <p>기존 리스트의 정보를 수정하는 비즈니스 로직을 정의합니다.
 * 리스트의 제목, 설명, 색상 변경을 담당합니다.
 * 
 * @since 1.0.0
 */
public interface UpdateBoardListUseCase {
  /**
   * 보드 리스트 정보를 수정합니다.
   * 
   * <p>이 메서드는 리스트 수정 프로세스를 처리하며, 다음과 같은 검증과 처리를 수행합니다:
   * <ul>
   *   <li>리스트 존재 여부 확인</li>
   *   <li>리스트 수정 권한 검증</li>
   *   <li>수정할 데이터 유효성 검증</li>
   *   <li>리스트 정보 업데이트</li>
   *   <li>변경된 리스트 저장</li>
   * </ul>
   * 
   * @param command 리스트 수정에 필요한 정보를 담은 커맨드 객체
   * @return 수정 결과 (성공 시 수정된 리스트, 실패 시 실패 정보)
   */
  Either<Failure, BoardList> updateBoardList(UpdateBoardListCommand command);
}
