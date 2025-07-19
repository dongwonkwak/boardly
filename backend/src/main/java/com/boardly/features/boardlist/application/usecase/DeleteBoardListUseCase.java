package com.boardly.features.boardlist.application.usecase;

import com.boardly.features.boardlist.application.port.input.DeleteBoardListCommand;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

/**
 * 보드 리스트 삭제 유스케이스 인터페이스
 * 
 * <p>보드에서 리스트를 삭제하는 비즈니스 로직을 정의합니다.
 * 리스트 삭제 시 포함된 모든 카드도 함께 삭제됩니다.
 * 
 * @since 1.0.0
 */
public interface DeleteBoardListUseCase {
   /**
   * 보드 리스트를 삭제합니다.
   * 
   * <p>이 메서드는 리스트 삭제 프로세스를 처리하며, 다음과 같은 검증과 처리를 수행합니다:
   * <ul>
   *   <li>리스트 존재 여부 확인</li>
   *   <li>리스트 삭제 권한 검증</li>
   *   <li>포함된 모든 카드 삭제</li>
   *   <li>리스트 삭제</li>
   *   <li>이후 리스트들의 position 재정렬</li>
   * </ul>
   * 
   * @param command 리스트 삭제에 필요한 정보를 담은 커맨드 객체
   * @return 삭제 결과 (성공 시 void, 실패 시 실패 정보)
   */
  Either<Failure, Void> deleteBoardList(DeleteBoardListCommand command);
}
