package com.boardly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.boardly.features.attachment.infrastructure.config.AttachmentPolicyProperties;
import com.boardly.features.boardlist.infrastructure.config.BoardListPolicyConfigImpl;
import com.boardly.features.card.infrastructure.config.CardPolicyProperties;
import com.boardly.shared.application.config.properties.AppProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        AppProperties.class,
        CardPolicyProperties.class,
        BoardListPolicyConfigImpl.class,
        AttachmentPolicyProperties.class
})
public class BoardlyApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoardlyApplication.class, args);
    }

}
