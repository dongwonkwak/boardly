package com.boardly.features.auth.presentation;

import com.boardly.shared.application.config.properties.AppProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final MessageSource messageSource;
    private final AppProperties appProperties;

    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String errorMessage,
            Model model,
            Locale locale) {

        // 프론트엔드 URL을 모델에 추가
        model.addAttribute("frontendUrl", appProperties.getFrontend().getUrl());

        // 에러 메시지 처리
        if (errorMessage != null) {
            model.addAttribute("error",
                    messageSource.getMessage(errorMessage, null, locale));
        }

        return "login";
    }
}
