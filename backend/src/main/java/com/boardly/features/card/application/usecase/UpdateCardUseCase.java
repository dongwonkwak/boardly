package com.boardly.features.card.application.usecase;

import com.boardly.features.card.application.port.input.UpdateCardCommand;
import com.boardly.features.card.domain.model.Card;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

/**
 * 카드 내용 수정 유스케이스 인터페이스
 * 
 * <p>
 * 카드의 제목과 설명을 수정하는 비즈니스 로직을 정의합니다.
 * 
 * @since 1.0.0
 */
public interface UpdateCardUseCase {
  /**
   * 카드의 제목과 설명을 수정합니다.
   * 
   * <p>
   * 이 메서드는 카드 수정 프로세스를 처리하며, 다음과 같은 검증과 처리를 수행합니다:
   * <ul>
   * <li>카드 존재 여부 확인</li>
   * <li>카드 수정 권한 검증</li>
   * <li>입력 데이터 유효성 검증</li>
   * <li>카드 정보 업데이트</li>
   * </ul>
   * 
   * @param command 카드 수정에 필요한 정보를 담은 커맨드 객체
   * @return 수정 결과 (성공 시 수정된 카드, 실패 시 실패 정보)
   */
  Either<Failure, Card> updateCard(UpdateCardCommand command);
}
