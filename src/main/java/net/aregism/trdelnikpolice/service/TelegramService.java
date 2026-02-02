package net.aregism.trdelnikpolice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.aregism.trdelnikpolice.config.TelegramProperties;
import net.aregism.trdelnikpolice.model.common.Language;
import net.aregism.trdelnikpolice.model.common.Mapping;
import net.aregism.trdelnikpolice.model.common.Position;
import net.aregism.trdelnikpolice.model.dto.TelegramMessageDto;
import net.aregism.trdelnikpolice.model.entity.ChatMember;
import net.aregism.trdelnikpolice.model.entity.KeywordUsage;
import net.aregism.trdelnikpolice.repository.ChatMemberRepository;
import net.aregism.trdelnikpolice.repository.KeywordUsageRepository;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class TelegramService {

    private final TelegramProperties properties;
    private final ChatMemberRepository chatMemberRepo;
    private final KeywordUsageRepository usageRepo;
    private final Random random = new Random();

    private List<Mapping> mappings;

    @Transactional
    public void handle(TelegramMessageDto dto) {
        log.info("Received message from chatId={} messageId={} text='{}'",
                dto.getChatId(), dto.getMessageId(), dto.getText());

        String messageText = dto.getText();
        if (dto.isReplyToBot()) {
            sendReply(properties.getChatId(), dto.getMessageId(), "siktir yli, faq yu");
            return;
        }
        if ("upgrade musri papka".equals(dto.getText())) {
            initMappings();
            return;
        }
        if (messageText == null || messageText.isEmpty()) return;

        String normalizedText = Normalizer.normalize(messageText, Normalizer.Form.NFC)
                .toLowerCase(Locale.ROOT);

        List<String> words =
                Arrays.stream(normalizedText.split("\\s+"))
                        .map(w -> w.replaceAll("[^\\p{L}\\p{N}]", ""))
                        .filter(w -> !w.isEmpty())
                        .distinct()
                        .collect(Collectors.toList()); // distinct to list instead of set, because order is important
        Language language = probe(words);
        StringBuilder combinedReplies = new StringBuilder();
        if (random.nextInt(properties.getDeleteMessagesOutOf()) <= properties.getDeleteMessagesCount()) {
            String msg = language.equals(Language.EN) ? "tazuc gri, blyat" : "թազուց գրի, բլյատ";
            deleteMessage(properties.getChatId(), dto.getMessageId());
            sendMessage(properties.getChatId(), msg);
            return;
        }

        for (Mapping mapping : mappings) {
            String key = mapping.getKeyword();
            if (key == null || key.isEmpty()) continue;

            if (correctPositionKeyFound(words, mapping)) {
                recordUsage(dto, mapping.getKeyword());
                if (!mapping.getPosition().equals(Position.REPLACE)) {
                    String reply = getRandomReply(mapping.getResponses());
                    log.info("Matched key='{}' in message. Adding reply='{}'", key, reply);

                    if (!combinedReplies.isEmpty()) {
                        combinedReplies.append("\n");
                    }
                    combinedReplies.append(reply);
                } else {
                    // replace logic
                }
            }
        }

        if (combinedReplies.length() > 0) {
            sendReply(dto.getChatId(), dto.getMessageId(), combinedReplies.toString());
        } else {
            log.debug("No matching key found for messageId={}", dto.getMessageId());
        }
    }

    private Language probe(List<String> words) {
        int am = 0;
        int en = 0;

        for (String word : words) {
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);

                if (c >= '\u0530' && c <= '\u058F') {
                    am++;
                } else if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
                    en++;
                }
            }
        }

        return am > en ? Language.AM : Language.EN;
    }


    private void deleteMessage(long chatId, int messageId) {
        log.info("Deleting...");
        RestTemplate template = new RestTemplate();
        String url = "https://api.telegram.org/bot" + properties.getToken() + "/deleteMessage";
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("message_id", messageId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        template.postForObject(url, request, String.class);
    }

    private boolean correctPositionKeyFound(List<String> words, Mapping mapping) {
        if (words.size() < 1) {
            return false;
        }
        String normalizedKey = Normalizer.normalize(mapping.getKeyword(), Normalizer.Form.NFC)
                .toLowerCase(Locale.ROOT);

        if (mapping.getPosition().equals(Position.ANY) && words.contains(normalizedKey)) {
            return true;
        } else if (mapping.getPosition().equals(Position.LAST) && words.get(words.size() - 1).equals(normalizedKey)) {
            return true;
        } else if ((mapping.getPosition().equals(Position.REGEX)) && matchesSequence(words, normalizedKey)) {
            return true;
        } else if ((mapping.getPosition().equals(Position.CONTAIN)) && containsSequence(words, normalizedKey)) {
            return true;
        } else if ((mapping.getPosition().equals(Position.REPLACE)) && containsSequence(words, normalizedKey)) {
            return true;
        }
        return false;
    }

    private boolean containsSequence(List<String> words, String normalizedKey) {
        String combined = String.join("", words);
        return combined.contains(normalizedKey);
    }

    private boolean matchesSequence(List<String> words, String normalizedKey) {
        String combined = String.join("", words);

        StringBuilder regex = new StringBuilder();
        for (char c : normalizedKey.toCharArray()) {
            regex.append(".*").append(Pattern.quote(String.valueOf(c)));
        }
        regex.append(".*"); // allow anything after the last char

        return Pattern.compile(regex.toString()).matcher(combined).matches();
    }

    public void sendMessage(long chatId, String text) {
        String telegramApiUrl = "https://api.telegram.org/bot" + properties.getToken() + "/sendMessage";
        RestTemplate template = new RestTemplate();
        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", chatId);
        payload.put("text", text);

        try {
            template.postForObject(telegramApiUrl, payload, String.class);
            log.info("Sent message to chatId={}", chatId);
        } catch (Exception e) {
            log.error("Failed to send message to chatId={}", chatId, e);
        }
    }


    private void sendReply(long chatId, long replyToMessageId, String text) {
        String telegramApiUrl = "https://api.telegram.org/bot" + properties.getToken() + "/sendMessage";
        RestTemplate template = new RestTemplate();
        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", chatId);
        payload.put("text", text);
        payload.put("reply_to_message_id", replyToMessageId);

        try {
            template.postForObject(telegramApiUrl, payload, String.class);
            log.info("Sent reply to chatId={} replying to messageId={}", chatId, replyToMessageId);
        } catch (Exception e) {
            log.error("Failed to send reply to chatId={} replying to messageId={}", chatId, replyToMessageId, e);
        }
    }


    public void initMappings() {
        String fileId = "15YE61MdzZDGpsJQhld8kip7jkJ0Q84FG26l7h5TIk5A";
        String exportUrl = "https://docs.google.com/document/d/" + fileId + "/export?format=docx";

        ObjectMapper mapper = new ObjectMapper();

        try {
            URL url = new URL(exportUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                log.error("Failed to download DOCX, response code: {}", responseCode);
                return;
            }

            try (InputStream in = connection.getInputStream()) {
                XWPFDocument doc = new XWPFDocument(in);

                StringBuilder textBuilder = new StringBuilder();
                for (XWPFParagraph p : doc.getParagraphs()) {
                    textBuilder.append(p.getText());
                }

                String jsonText = textBuilder.toString().trim();
                this.mappings = mapper.readValue(jsonText, new TypeReference<List<Mapping>>() {
                });

                log.info("Mappings loaded successfully. Total entries: {}", this.mappings.size());
                String message = this.mappings.size() + " hogi blatavat a anum";
                sendMessage(properties.getChatId(), message);
            }

        } catch (Exception e) {
            log.error("Error initializing mappings from Google Doc", e);
        }
    }


    public String getRandomReply(List<String> replies) {
        return replies.get(random.nextInt(replies.size()));
    }

    @Transactional
    public void recordUsage(TelegramMessageDto dto, String keyword) {
        String username = dto.getSender();
        ChatMember member = chatMemberRepo.findByUsername(username);
        if (member == null) {
            member = new ChatMember();
            member.setUsername(username);
            chatMemberRepo.save(member);
        }

        KeywordUsage usage = usageRepo.findByChatMemberAndKeyword(member, keyword)
                .orElse(null);

        if (usage == null) {
            usage = new KeywordUsage();
            usage.setChatMember(member);
            usage.setKeyword(keyword);
        }

        usage.setCount(usage.getCount() + 1);
        usageRepo.save(usage);
    }

}
