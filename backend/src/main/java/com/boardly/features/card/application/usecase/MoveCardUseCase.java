package com.boardly.features.card.application.usecase;

import com.boardly.features.card.application.port.input.MoveCardCommand;
import com.boardly.features.card.domain.model.Card;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

/**
 * 카드 이동 유스케이스 인터페이스
 * 
 * <p>
 * 카드를 같은 리스트 내에서 이동하거나 다른 리스트로 이동하는 비즈니스 로직을 정의합니다.
 * 
 * @since 1.0.0
 */
public interface MoveCardUseCase {
  /**
   * 카드를 이동합니다.
   * 
   * <p>
   * 이 메서드는 카드 이동 프로세스를 처리하며, 다음과 같은 검증과 처리를 수행합니다:
   * <ul>
   * <li>카드 존재 여부 확인</li>
   * <li>카드 수정 권한 검증</li>
   * <li>대상 위치 유효성 검증</li>
   * <li>대상 리스트 접근 권한 검증 (다른 리스트로 이동 시)</li>
   * <li>대상 리스트 카드 개수 제한 확인 (다른 리스트로 이동 시)</li>
   * <li>관련 카드들의 위치 조정</li>
   * <li>카드 이동 완료</li>
   * </ul>
   * 
   * @param command 카드 이동에 필요한 정보를 담은 커맨드 객체
   * @return 이동 결과 (성공 시 이동된 카드, 실패 시 실패 정보)
   */
  Either<Failure, Card> moveCard(MoveCardCommand command);
}
