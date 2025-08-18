package com.boardly.api.adapters.in.web.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @GetMapping("/login")
    public String loginPage(@RequestParam(name = "errorMessage", required = false) String errorMessage,
                            Model model,
                            Locale locale) {
        model.addAttribute("frontendUrl", frontendUrl);

        if (errorMessage != null) {
            model.addAttribute("error", messageSource.getMessage(errorMessage, null, errorMessage, locale));
        }

        return "login";
    }
}
