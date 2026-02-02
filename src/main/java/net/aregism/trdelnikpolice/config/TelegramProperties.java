package net.aregism.trdelnikpolice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "telegram.bot")
public class TelegramProperties {
    private String token;
    private String username;
    private String webhookPath;
    private long chatId;
    private int deleteMessagesCount;
    private int deleteMessagesOutOf;
}