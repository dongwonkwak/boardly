package com.boardly.application.user.command;

import com.boardly.shared.annotation.UseCase;
import lombok.Builder;
import lombok.Getter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 명령
 */
@Getter
@Builder
@UseCase
public class SignUpCommand {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private final String email;

    @NotBlank(message = "사용자명은 필수입니다.")
    @Size(min = 3, max = 20, message = "사용자명은 3자 이상 20자 이하여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "사용자명은 영문, 숫자, 언더스코어(_), 하이픈(-)만 사용할 수 있습니다.")
    private final String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다.")
    private final String password;

    @Size(max = 50, message = "표시명은 50자 이하여야 합니다.")
    private final String displayName;

    private final String profileImageUrl;
}
