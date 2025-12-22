package net.aregism.trdelnikpolice.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TelegramMessageDto {
    private long chatId;
    private long messageId;
    private String text;
    private String sender;
    private boolean replyToBot;
}
