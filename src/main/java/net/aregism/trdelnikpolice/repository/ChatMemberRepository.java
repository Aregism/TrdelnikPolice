package net.aregism.trdelnikpolice.repository;

import net.aregism.trdelnikpolice.model.entity.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
    ChatMember findByUsername(String username);
}
