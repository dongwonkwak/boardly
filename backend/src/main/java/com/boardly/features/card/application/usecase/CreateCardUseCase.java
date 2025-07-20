package com.boardly.features.card.application.usecase;

import com.boardly.features.card.application.port.input.CreateCardCommand;
import com.boardly.features.card.domain.model.Card;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

/**
 * 카드 생성 유스케이스 인터페이스
 * 
 * <p>
 * 리스트에 새로운 작업 카드를 추가하는 비즈니스 로직을 정의합니다.
 * 클린 아키텍처의 Application Layer에서 도메인 로직의 진입점 역할을 합니다.
 * 
 * @since 1.0.0
 */
public interface CreateCardUseCase {

  /**
   * 새로운 카드를 생성합니다.
   * 
   * <p>
   * 이 메서드는 카드 생성 프로세스를 처리하며, 다음과 같은 검증과 처리를 수행합니다:
   * <ul>
   * <li>리스트 존재 여부 확인</li>
   * <li>리스트 접근 권한 검증</li>
   * <li>리스트당 카드 개수 제한 확인 (최대 100개)</li>
   * <li>카드 제목 유효성 검증</li>
   * <li>새 카드의 position 계산</li>
   * <li>카드 생성 및 저장</li>
   * </ul>
   * 
   * @param command 카드 생성에 필요한 정보를 담은 커맨드 객체
   * @return 생성 결과 (성공 시 생성된 카드, 실패 시 실패 정보)
   */
  Either<Failure, Card> createCard(CreateCardCommand command);
}
