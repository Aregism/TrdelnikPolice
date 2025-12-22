package net.aregism.trdelnikpolice.controller;
import lombok.RequiredArgsConstructor;
import net.aregism.trdelnikpolice.model.dto.TelegramMessageDto;
import net.aregism.trdelnikpolice.service.TelegramService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Optional;

@RestController
@RequestMapping("/telegram")
@RequiredArgsConstructor
public class TelegramController {

    private final TelegramService telegramService;

    @PostMapping("/consume")
    public ResponseEntity<?> consume(@RequestBody Update update) {

        Message msg = null;

        if (update.hasMessage()) {
            msg = update.getMessage();
        } else if (update.hasEditedMessage()) {
            msg = update.getEditedMessage();
        }

        if (msg == null || !msg.hasText()) {
            return ResponseEntity.ok().build();
        }

        User from = msg.getFrom();
        String sender =
                from == null ? null
                        : Optional.ofNullable(from.getUserName())
                        .orElse(from.getFirstName());

        boolean replyToBot = false;

        Message reply = msg.getReplyToMessage();
        if (reply != null) {
            User repliedFrom = reply.getFrom();
            replyToBot = repliedFrom != null && Boolean.TRUE.equals(repliedFrom.getIsBot());
        }

        TelegramMessageDto dto = new TelegramMessageDto(
                msg.getChatId(),
                msg.getMessageId(),
                msg.getText(),
                sender,
                replyToBot
        );

        telegramService.handle(dto);

        return ResponseEntity.ok().build();
    }


}