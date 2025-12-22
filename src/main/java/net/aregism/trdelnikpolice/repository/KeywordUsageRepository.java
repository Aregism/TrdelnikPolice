package net.aregism.trdelnikpolice.repository;

import net.aregism.trdelnikpolice.model.entity.ChatMember;
import net.aregism.trdelnikpolice.model.entity.KeywordUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KeywordUsageRepository extends JpaRepository<KeywordUsage, Long> {
    Optional<KeywordUsage> findByChatMemberAndKeyword(ChatMember member, String keyword);
    List<KeywordUsage> findByChatMember(ChatMember chatMember);
}
