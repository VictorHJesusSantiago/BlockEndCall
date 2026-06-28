package com.blockendcall.service;

import com.blockendcall.dto.request.CreateAnnouncementRequest;
import com.blockendcall.dto.response.AnnouncementResponse;
import com.blockendcall.entity.Announcement;
import com.blockendcall.entity.User;
import com.blockendcall.exception.ResourceNotFoundException;
import com.blockendcall.repository.AnnouncementRepository;
import com.blockendcall.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

    public List<AnnouncementResponse> getActiveAnnouncements() {
        return announcementRepository.findByActiveTrueOrderByCreatedAtDesc().stream()
                .map(AnnouncementResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public AnnouncementResponse createAnnouncement(CreateAnnouncementRequest req, String authorEmail) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + authorEmail));

        Announcement announcement = Announcement.builder()
                .title(req.getTitle())
                .body(req.getBody())
                .author(author)
                .build();

        return AnnouncementResponse.from(announcementRepository.save(announcement));
    }

    @Transactional
    public void deactivateAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found: " + id));
        announcement.setActive(false);
        announcementRepository.save(announcement);
    }
}
