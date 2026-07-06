package com.cineverse.affiliate;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/aff")
@RequiredArgsConstructor
public class AffiliateController {

    private final AffiliateLinkRepository linkRepository;
    private final AffiliateClickRepository clickRepository;

    /** Registra o clique e redireciona p/ o parceiro (público, indexável em botões "Onde assistir") */
    @GetMapping("/{linkId}")
    public ResponseEntity<Void> redirect(@PathVariable Long linkId,
                                         @RequestParam(required = false) Long profileId) {
        AffiliateLink link = linkRepository.findById(linkId)
                .filter(AffiliateLink::isActive)
                .orElseThrow(() -> new NoSuchElementException("Link não encontrado"));
        clickRepository.save(AffiliateClick.builder()
                .linkId(linkId)
                .profileId(profileId)
                .build());
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, link.getTrackedUrl())
                .build();
    }
}
