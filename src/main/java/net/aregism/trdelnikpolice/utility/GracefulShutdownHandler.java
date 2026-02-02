package net.aregism.trdelnikpolice.utility;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.aregism.trdelnikpolice.config.TelegramProperties;
import net.aregism.trdelnikpolice.service.TelegramService;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class GracefulShutdownHandler {

    public final TelegramService service;
    public final TelegramProperties properties;

    @PreDestroy
    public void onGracefulShutdown() {
        log.info("Initializing graceful shutdown...");
        log.info("Running final tasks...");
        service.sendMessage(properties.getChatId(), "Կպա գորձերիս ապերներ: Հեսա կգամ:");
        log.info("Done.");
    }
}
