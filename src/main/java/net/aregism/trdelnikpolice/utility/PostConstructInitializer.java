package net.aregism.trdelnikpolice.utility;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.aregism.trdelnikpolice.service.TelegramService;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class PostConstructInitializer {

    private final TelegramService service;

    @PostConstruct
    public void init(){
        initMappings();
    }

    private void initMappings() {
        service.initMappings();
    }
}
