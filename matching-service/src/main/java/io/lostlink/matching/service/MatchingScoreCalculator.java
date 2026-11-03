package io.lostlink.matching.service;

import io.lostlink.common.dto.ItemSnapshot;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MatchingScoreCalculator {
    public int calculate(ItemSnapshot first, ItemSnapshot second) {
        int score = 0;
        if (first.category().equalsIgnoreCase(second.category())) score += 30;
        if (normalize(first.location()).equals(normalize(second.location()))) score += 25;
        long days = Math.abs(ChronoUnit.DAYS.between(first.itemDate(), second.itemDate()));
        score += days == 0 ? 20 : days <= 2 ? 15 : days <= 7 ? 10 : 0;
        score += (int) Math.round(tokenOverlap(first.title(), second.title()) * 15);
        score += (int) Math.round(tokenOverlap(first.description(), second.description()) * 10);
        return Math.min(100, score);
    }

    private double tokenOverlap(String first, String second) {
        Set<String> a = tokens(first);
        Set<String> b = tokens(second);
        if (a.isEmpty() || b.isEmpty()) return 0;
        long common = a.stream().filter(b::contains).count();
        return (double) common / Math.max(a.size(), b.size());
    }

    private Set<String> tokens(String text) {
        return Arrays.stream(normalize(text).split("\\s+"))
                .filter(token -> token.length() > 1)
                .collect(Collectors.toSet());
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
    }
}
