package net.aregism.trdelnikpolice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.aregism.trdelnikpolice.config.TelegramProperties;
import net.aregism.trdelnikpolice.model.common.MemberReport;
import net.aregism.trdelnikpolice.model.entity.ChatMember;
import net.aregism.trdelnikpolice.model.entity.KeywordUsage;
import net.aregism.trdelnikpolice.repository.ChatMemberRepository;
import net.aregism.trdelnikpolice.repository.KeywordUsageRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class ReportScheduler {

    private final TelegramProperties properties;
    private final TelegramService service;
    private final ChatMemberRepository chatMemberRepository;
    private final KeywordUsageRepository keywordUsageRepository;

    @Scheduled(cron = "0 0 12 * * *")
    public void sendReport() {
        log.info("Starting scheduled keyword usage report...");

        List<ChatMember> members = chatMemberRepository.findAll();
        if (members.isEmpty()) {
            return;
        }

        List<MemberReport> reports = new ArrayList<>();

        for (ChatMember member : members) {
            List<KeywordUsage> usages = keywordUsageRepository.findByChatMember(member);
            if (usages.isEmpty()) continue;

            int totalCount = usages.stream()
                    .mapToInt(KeywordUsage::getCount)
                    .sum();

            reports.add(new MemberReport(member, usages, totalCount));
        }

        reports.sort(Comparator.comparingInt(MemberReport::totalCount).reversed());

        for (MemberReport r : reports) {
            StringBuilder report = new StringBuilder();
            report.append(r.member().getUsername())
                    .append(" -n havayi od a brdel:\n");

            for (KeywordUsage usage : r.usages()) {
                report.append(usage.getKeyword())
                        .append(": ")
                        .append(usage.getCount())
                        .append("\n");
            }

            report.append("\n")
                    .append(r.totalCount())
                    .append(" angam.");

            appendWindTerm(report, r.totalCount());

            service.sendMessage(properties.getChatId(), report.toString());

            log.info(
                    "Sent keyword usage report for {} (total={})",
                    r.member().getUsername(),
                    r.totalCount()
            );
        }

        log.info("Scheduled keyword usage report completed.");
    }

    private void appendWindTerm(StringBuilder report, int totalCount) {
        String term;
        if (totalCount < 25) {
            term = "\nUshadir chyngnes, qunac dodik";
        } else if (totalCount < 50) {
            term = "\nhavayi demq 2011";
        } else if (totalCount < 100) {
            term = "\npuchiknery ur a";
        } else if (totalCount < 150) {
            term = "\ntetev briz";
        } else if (totalCount < 200) {
            term = "\nmayis er... irigva qami.";
        } else if (totalCount < 400) {
            term = "\ncuxy cuxy cuxy cuxy, qashi tooo";
        } else if (totalCount < 800) {
            term = "\nlav e mors arev. anapati hujku qami";
        } else if (totalCount < 1500) {
            term = "\nuragan";
        } else {
            term = "\nlegendar gandon";
        }
        report.append(term);
    }
}
