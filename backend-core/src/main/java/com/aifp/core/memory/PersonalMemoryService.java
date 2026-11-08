package com.aifp.core.memory;

import com.aifp.core.security.SecurityAuditService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PersonalMemoryService {

    private final AiMemoryRepository memoryRepository;
    private final SecurityAuditService auditService;

    public PersonalMemoryService(AiMemoryRepository memoryRepository, SecurityAuditService auditService) {
        this.memoryRepository = memoryRepository;
        this.auditService = auditService;
    }

    @Transactional
    public AiMemoryEntity upsertMemory(UUID userId, String category, String topicKey, String content, boolean asCandidate) {
        String targetStatus = asCandidate ? "CANDIDATE" : "ACTIVE";
        // If saving an ACTIVE memory with a topicKey, supersede older conflicting facts on the same topicKey
        if ("ACTIVE".equals(targetStatus) && topicKey != null && !topicKey.isBlank()) {
            memoryRepository.supersedeActiveMemoriesByTopic(userId, topicKey.trim());
        }

        AiMemoryEntity entity = new AiMemoryEntity(
                UUID.randomUUID(),
                userId,
                category.trim().toUpperCase(),
                topicKey != null ? topicKey.trim() : null,
                content.trim(),
                targetStatus
        );
        return memoryRepository.save(entity);
    }

    @Transactional
    public AiMemoryEntity approveCandidateMemory(UUID userId, UUID memoryId) {
        AiMemoryEntity memory = memoryRepository.findByIdAndUserId(memoryId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Memory not found"));

        if (memory.getTopicKey() != null && !memory.getTopicKey().isBlank()) {
            memoryRepository.supersedeActiveMemoriesByTopic(userId, memory.getTopicKey());
        }
        memory.setStatus("ACTIVE");
        memory.setUpdatedAt(Instant.now());
        return memoryRepository.save(memory);
    }

    @Transactional
    public void deleteMemory(UUID userId, UUID memoryId) {
        AiMemoryEntity memory = memoryRepository.findByIdAndUserId(memoryId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Memory not found"));
        memoryRepository.delete(memory);
        auditService.logEvent(userId, "MEMORY_DELETED", "INFO", "AI_MEMORY", memoryId.toString(), "{}");
    }

    @Transactional(readOnly = true)
    public List<AiMemoryEntity> listVisibleMemories(UUID userId) {
        return memoryRepository.findByUserIdAndStatusInOrderByUpdatedAtDesc(userId, List.of("ACTIVE", "CANDIDATE"));
    }
}

