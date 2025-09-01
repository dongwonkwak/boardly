package com.boardly.application.user.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("회원가입 명령 테스트")
class SignUpCommandTest {

    @Test
    @DisplayName("정상적인 회원가입 명령을 생성할 수 있다")
    void createValidSignUpCommand() {
        // given
        String email = "test@example.com";
        String username = "testuser";
        String password = "password123";
        String displayName = "테스트 사용자";
        String profileImageUrl = "https://example.com/profile.jpg";

        // when
        SignUpCommand command = SignUpCommand.builder()
                .email(email)
                .username(username)
                .password(password)
                .displayName(displayName)
                .profileImageUrl(profileImageUrl)
                .build();

        // then
        assertThat(command.getEmail()).isEqualTo(email);
        assertThat(command.getUsername()).isEqualTo(username);
        assertThat(command.getPassword()).isEqualTo(password);
        assertThat(command.getDisplayName()).isEqualTo(displayName);
        assertThat(command.getProfileImageUrl()).isEqualTo(profileImageUrl);
    }

    @Test
    @DisplayName("최소 필수 정보로 회원가입 명령을 생성할 수 있다")
    void createMinimalSignUpCommand() {
        // given
        String email = "test@example.com";
        String username = "testuser";
        String password = "password123";

        // when
        SignUpCommand command = SignUpCommand.builder()
                .email(email)
                .username(username)
                .password(password)
                .build();

        // then
        assertThat(command.getEmail()).isEqualTo(email);
        assertThat(command.getUsername()).isEqualTo(username);
        assertThat(command.getPassword()).isEqualTo(password);
        assertThat(command.getDisplayName()).isNull();
        assertThat(command.getProfileImageUrl()).isNull();
    }
}
