package com.cineverse.affiliate;

import org.springframework.data.jpa.repository.JpaRepository;

interface AffiliateLinkRepository extends JpaRepository<AffiliateLink, Long> {}

interface AffiliateClickRepository extends JpaRepository<AffiliateClick, Long> {}
