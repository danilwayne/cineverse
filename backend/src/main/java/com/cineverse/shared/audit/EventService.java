package com.cineverse.shared.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventService {

    private final ProductEventRepository repository;

    @Async
    public void track(Long profileId, String event, Map<String, Object> metadata) {
        repository.save(ProductEvent.builder()
                .profileId(profileId)
                .event(event)
                .metadata(metadata)
                .build());
    }
}
