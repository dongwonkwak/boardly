package com.boardly.domain.user;

import com.boardly.domain.user.exception.UserAccessDeniedException;
import com.boardly.domain.user.exception.UserNotFoundException;
import com.boardly.domain.user.exception.UserSaveException;
import com.boardly.domain.user.exception.UserSearchException;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 저장소 인터페이스
 * 순수한 도메인 계층으로 외부 라이브러리 의존성 없음
 * 예외 처리는 도메인 예외를 통해 수행
 */
public interface UserRepository {

    // ======================== 생성/수정/삭제 - 도메인 예외 사용 ========================

    /**
     * 사용자 저장 (생성/수정)
     * 
     * @param user 저장할 사용자
     * @return 저장된 사용자
     * @throws UserSaveException 저장 실패 시 (제약 조건 위반, 중복 등)
     */
    User save(User user) throws UserSaveException;

    /**
     * 사용자 삭제
     * 
     * @param id 삭제할 사용자 ID
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    void deleteById(UserId id) throws UserNotFoundException;

    // ======================== 단순 조회 - Optional/List/boolean 사용
    // ========================

    /**
     * ID로 사용자 조회
     */
    Optional<User> findById(UserId id);

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);

    /**
     * 사용자명으로 사용자 조회
     */
    Optional<User> findByUsername(String username);

    /**
     * 모든 사용자 조회
     */
    List<User> findAll();

    /**
     * 상태별 사용자 조회
     */
    List<User> findByStatus(UserStatus status);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 사용자명 존재 여부 확인
     */
    boolean existsByUsername(String username);

    /**
     * ID 존재 여부 확인
     */
    boolean existsById(UserId id);

    // ======================== 복잡한 조회 (권한/비즈니스 룰 포함) - 도메인 예외 사용
    // ========================

    /**
     * 권한 체크를 포함한 사용자 조회
     * 
     * @param id          조회할 사용자 ID
     * @param requesterId 요청자 ID
     * @return 조회된 사용자
     * @throws UserNotFoundException     사용자를 찾을 수 없는 경우
     * @throws UserAccessDeniedException 권한이 없는 경우
     */
    User findByIdWithPermission(UserId id, UserId requesterId)
            throws UserNotFoundException, UserAccessDeniedException;

    /**
     * 검색 조건에 따른 사용자 조회
     * 
     * @param criteria 검색 조건
     * @return 검색된 사용자 목록
     * @throws UserSearchException 검색 실패 시
     */
    List<User> findBySearchCriteria(UserSearchCriteria criteria) throws UserSearchException;
}
