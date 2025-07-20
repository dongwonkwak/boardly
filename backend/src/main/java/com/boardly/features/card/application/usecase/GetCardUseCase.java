package com.boardly.features.card.application.usecase;

import com.boardly.features.card.application.port.input.GetCardCommand;
import com.boardly.features.card.domain.model.Card;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

/**
 * 카드 상세 조회 유스케이스 인터페이스
 * 
 * <p>
 * 특정 카드의 상세 정보를 조회하는 비즈니스 로직을 정의합니다.
 * 
 * @since 1.0.0
 */
public interface GetCardUseCase {

  /**
   * 카드의 상세 정보를 조회합니다.
   * 
   * <p>
   * 이 메서드는 카드 조회 프로세스를 처리하며, 다음과 같은 검증과 처리를 수행합니다:
   * <ul>
   * <li>카드 존재 여부 확인</li>
   * <li>카드 접근 권한 검증</li>
   * <li>카드 상세 정보 반환</li>
   * </ul>
   * 
   * @param command 카드 조회에 필요한 정보를 담은 커맨드 객체
   * @return 조회 결과 (성공 시 카드 정보, 실패 시 실패 정보)
   */
  Either<Failure, Card> getCard(GetCardCommand command);
}
