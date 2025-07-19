package com.boardly.features.boardlist.application.usecase;

import com.boardly.features.boardlist.application.port.input.UpdateBoardListPositionCommand;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

import java.util.List;


/**
 * 보드 리스트 위치 변경 유스케이스 인터페이스
 * 
 * <p>드래그 앤 드롭을 통한 리스트 순서 변경을 처리하는 비즈니스 로직을 정의합니다.
 * 리스트 순서 변경 시 영향받는 다른 리스트들의 position도 함께 업데이트됩니다.
 * 
 * @since 1.0.0
 */
public interface UpdateBoardListPositionUseCase {
  
  /**
   * 리스트의 위치를 변경합니다.
   * 
   * <p>이 메서드는 리스트 위치 변경 프로세스를 처리하며, 다음과 같은 검증과 처리를 수행합니다:
   * <ul>
   *   <li>리스트 존재 여부 확인</li>
   *   <li>리스트 수정 권한 검증</li>
   *   <li>새로운 위치의 유효성 확인</li>
   *   <li>영향받는 다른 리스트들의 position 조정</li>
   *   <li>변경된 모든 리스트 저장</li>
   * </ul>
   * 
   * @param command 리스트 위치 변경에 필요한 정보를 담은 커맨드 객체
   * @return 변경 결과 (성공 시 업데이트된 리스트 목록, 실패 시 실패 정보)
   */
  Either<Failure, List<BoardList>> updateBoardListPosition(UpdateBoardListPositionCommand command);
}
