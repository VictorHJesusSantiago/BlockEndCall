package com.blockendcall.event;

/**
 * Published after a BlockedNumber crosses the confirmation threshold.
 * Carries only primitive data so there is no risk of detached-entity access
 * when consumed outside the originating JPA session.
 */
public record NumberConfirmedEvent(
        Long numberId,
        String phoneNumber,
        String category,
        int reportCount
) {}
