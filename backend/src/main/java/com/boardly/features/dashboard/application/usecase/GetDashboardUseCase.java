package com.boardly.features.dashboard.application.usecase;

import com.boardly.features.dashboard.application.dto.DashboardResponse;
import com.boardly.features.dashboard.application.port.input.GetDashboardCommand;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;

/**
 * 대시보드 조회 유스케이스 인터페이스
 * 
 * <p>사용자의 대시보드 정보를 조회하는 비즈니스 로직을 정의합니다.
 * 클린 아키텍처의 Application Layer에서 도메인 로직의 진입점 역할을 합니다.
 * 
 * <p>구현체는 다음과 같은 책임을 가집니다:
 * <ul>
 *   <li>사용자 ID 유효성 검증</li>
 *   <li>사용자의 보드 목록 조회</li>
 *   <li>최근 활동 목록 조회</li>
 *   <li>통계 정보 계산</li>
 *   <li>오류 상황 처리 및 적절한 응답 반환</li>
 * </ul>
 * 
 * @since 1.0.0
 * @author Boardly Team
 */
public interface GetDashboardUseCase {

    /**
     * 사용자의 대시보드 정보를 조회합니다.
     * 
     * <p>이 메서드는 대시보드 조회 프로세스를 처리하며, 다음과 같은 검증과 처리를 수행합니다:
     * <ul>
     *   <li>사용자 ID 유효성 검증</li>
     *   <li>보드 목록 조회</li>
     *   <li>최근 활동 목록 조회</li>
     *   <li>통계 정보 계산</li>
     * </ul>
     * 
     * <p>조회 성공 시 {@link DashboardResponse}를 반환하고,
     * 실패 시 구체적인 실패 원인을 담은 {@link Failure} 객체를 반환합니다.
     * 
     * @param command 대시보드 조회에 필요한 정보를 담은 커맨드 객체
     *                <ul>
     *                  <li>userId: 사용자 ID (필수)</li>
     *                </ul>
     * 
     * @return {@link Either} 타입으로 래핑된 결과:
     *         <ul>
     *           <li>성공 시: {@code Either.right(DashboardResponse)} - 대시보드 정보</li>
     *           <li>실패 시: {@code Either.left(Failure)} - 실패 원인을 담은 객체</li>
     *         </ul>
     * 
     * @throws IllegalArgumentException command가 null인 경우
     * 
     * @see GetDashboardCommand
     * @see DashboardResponse
     * @see Failure
     * @see Failure.ValidationFailure 입력 데이터 유효성 검증 실패
     * @see Failure.NotFoundFailure 사용자가 존재하지 않는 경우
     * @see Failure.InternalServerError 시스템 내부 오류
     */
    Either<Failure, DashboardResponse> getDashboard(GetDashboardCommand command);
} 