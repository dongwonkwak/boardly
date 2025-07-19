package com.boardly.features.boardlist.application.usecase;

import com.boardly.features.boardlist.application.port.input.CreateBoardListCommand;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

/**
 * 보드 리스트 생성 유스케이스 인터페이스
 * 
 * <p>보드 내에 새로운 리스트를 생성하는 비즈니스 로직을 정의합니다.
 * 클린 아키텍처의 Application Layer에서 도메인 로직의 진입점 역할을 합니다.
 * 
 * @since 1.0.0
 */
public interface CreateBoardListUseCase {

  /**
   * 새로운 보드 리스트를 생성합니다.
   * 
   * <p>이 메서드는 리스트 생성 프로세스를 처리하며, 다음과 같은 검증과 처리를 수행합니다:
   * <ul>
   *   <li>보드 존재 여부 확인</li>
   *   <li>보드 접근 권한 검증</li>
   *   <li>보드별 리스트 개수 제한 확인 (최대 20개)</li>
   *   <li>리스트 제목 유효성 검증</li>
   *   <li>새 리스트의 position 계산</li>
   *   <li>리스트 생성 및 저장</li>
   * </ul>
   * 
   * @param command 리스트 생성에 필요한 정보를 담은 커맨드 객체
   * @return 생성 결과 (성공 시 생성된 리스트, 실패 시 실패 정보)
   */
  Either<Failure, BoardList> createBoardList(CreateBoardListCommand command);

} 