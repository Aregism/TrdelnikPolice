package net.aregism.trdelnikpolice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "keyword_usage",
        uniqueConstraints = @UniqueConstraint(columnNames = {"chat_member_id", "keyword"}))
public class KeywordUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chat_member_id", nullable = false)
    private ChatMember chatMember;

    @Column(nullable = false)
    private String keyword;

    @Column(nullable = false)
    private int count = 0;
}
