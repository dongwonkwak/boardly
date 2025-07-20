package com.boardly.features.card.application.usecase;

import com.boardly.features.card.application.port.input.DeleteCardCommand;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

/**
 * 카드 삭제 유스케이스 인터페이스
 * 
 * <p>
 * 카드를 영구적으로 삭제하는 비즈니스 로직을 정의합니다.
 * 
 * @since 1.0.0
 */
public interface DeleteCardUseCase {
  /**
   * 카드를 삭제합니다.
   * 
   * <p>
   * 이 메서드는 카드 삭제 프로세스를 처리하며, 다음과 같은 검증과 처리를 수행합니다:
   * <ul>
   * <li>카드 존재 여부 확인</li>
   * <li>카드 삭제 권한 검증</li>
   * <li>카드 삭제 실행</li>
   * <li>남은 카드들의 위치 조정</li>
   * </ul>
   * 
   * @param command 카드 삭제에 필요한 정보를 담은 커맨드 객체
   * @return 삭제 결과 (성공 시 null, 실패 시 실패 정보)
   */
  Either<Failure, Void> deleteCard(DeleteCardCommand command);
}
