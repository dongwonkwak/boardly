package com.boardly.features.card.application.usecase;

import com.boardly.features.card.application.port.input.CloneCardCommand;
import com.boardly.features.card.domain.model.Card;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

/**
 * 카드 복제 유스케이스 인터페이스
 * 
 * <p>
 * 기존 카드의 내용을 복사하여 새로운 카드를 생성하는 비즈니스 로직을 정의합니다.
 * 
 * @since 1.0.0
 */
public interface CloneCardUseCase {
  /**
   * 카드를 복제합니다.
   * 
   * <p>
   * 이 메서드는 카드 복제 프로세스를 처리하며, 다음과 같은 검증과 처리를 수행합니다:
   * <ul>
   * <li>원본 카드 존재 여부 확인</li>
   * <li>카드 접근 권한 검증</li>
   * <li>대상 리스트 접근 권한 검증 (다른 리스트로 복제 시)</li>
   * <li>대상 리스트 카드 개수 제한 확인</li>
   * <li>새 카드 제목 유효성 검증</li>
   * <li>카드 복제 및 저장</li>
   * </ul>
   * 
   * @param command 카드 복제에 필요한 정보를 담은 커맨드 객체
   * @return 복제 결과 (성공 시 복제된 카드, 실패 시 실패 정보)
   */
  Either<Failure, Card> cloneCard(CloneCardCommand command);
}
