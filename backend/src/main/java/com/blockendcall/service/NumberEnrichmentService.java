package com.blockendcall.service;

import com.blockendcall.dto.response.NumberReportedNameResponse;
import com.blockendcall.dto.response.NumberTimelineResponse;
import com.blockendcall.entity.BlockedNumber;
import com.blockendcall.entity.NumberReportedName;
import com.blockendcall.entity.NumberTimelineEvent;
import com.blockendcall.exception.ResourceNotFoundException;
import com.blockendcall.repository.BlockedNumberRepository;
import com.blockendcall.repository.NumberReportedNameRepository;
import com.blockendcall.repository.NumberTimelineEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NumberEnrichmentService {

    private final NumberReportedNameRepository numberReportedNameRepository;
    private final NumberTimelineEventRepository numberTimelineEventRepository;
    private final BlockedNumberRepository blockedNumberRepository;

    public List<NumberReportedNameResponse> getReportedNames(Long numberId) {
        return numberReportedNameRepository.findByBlockedNumberIdOrderByReportCountDesc(numberId)
                .stream()
                .map(NumberReportedNameResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void submitReportedName(Long numberId, String name, String userEmail) {
        BlockedNumber number = blockedNumberRepository.findById(numberId)
                .orElseThrow(() -> new ResourceNotFoundException("Number not found: " + numberId));

        Optional<NumberReportedName> existing =
                numberReportedNameRepository.findByBlockedNumberIdAndReportedName(numberId, name);

        if (existing.isPresent()) {
            NumberReportedName entry = existing.get();
            entry.setReportCount(entry.getReportCount() + 1);
            numberReportedNameRepository.save(entry);
        } else {
            numberReportedNameRepository.save(NumberReportedName.builder()
                    .blockedNumber(number)
                    .reportedName(name)
                    .reportCount(1)
                    .build());
        }

        addTimelineEvent(numberId, "REPORTED_NAME", "Nome reportado: " + name);
    }

    public List<NumberTimelineResponse> getTimeline(Long numberId) {
        return numberTimelineEventRepository.findByBlockedNumberIdOrderByCreatedAtDesc(numberId)
                .stream()
                .map(NumberTimelineResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addTimelineEvent(Long numberId, String eventType, String details) {
        BlockedNumber number = blockedNumberRepository.findById(numberId)
                .orElseThrow(() -> new ResourceNotFoundException("Number not found: " + numberId));

        numberTimelineEventRepository.save(NumberTimelineEvent.builder()
                .blockedNumber(number)
                .eventType(eventType)
                .details(details)
                .build());
    }
}
