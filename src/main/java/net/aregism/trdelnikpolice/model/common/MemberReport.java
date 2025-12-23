package net.aregism.trdelnikpolice.model.common;

import net.aregism.trdelnikpolice.model.entity.ChatMember;
import net.aregism.trdelnikpolice.model.entity.KeywordUsage;

import java.util.List;

public record MemberReport(ChatMember member, List<KeywordUsage> usages, int totalCount) {
}