package io.lostlink.matching.service;

import io.lostlink.common.dto.ItemSnapshot;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatchingScoreCalculatorTest {
    private final MatchingScoreCalculator calculator = new MatchingScoreCalculator();

    @Test
    void awardsMaximumScoreForSameItemDetails() {
        ItemSnapshot lost = item("LOST", "Black wireless headphones", LocalDate.of(2026, 11, 1));
        ItemSnapshot found = item("FOUND", "Black wireless headphones", LocalDate.of(2026, 11, 1));

        assertEquals(100, calculator.calculate(lost, found));
    }

    @Test
    void appliesDateBucketsAndRequiresTextOverlapForTextPoints() {
        ItemSnapshot lost = item("LOST", "Blue wallet", LocalDate.of(2026, 11, 1));
        ItemSnapshot foundTwoDaysLater = item("FOUND", "Blue wallet", LocalDate.of(2026, 11, 3));
        ItemSnapshot foundEightDaysLater = item("FOUND", "Unrelated bag", LocalDate.of(2026, 11, 9));

        assertEquals(95, calculator.calculate(lost, foundTwoDaysLater));
        assertEquals(65, calculator.calculate(lost, foundEightDaysLater));
    }

    @Test
    void returnsZeroWhenCategoryLocationDateAndWordsDoNotOverlap() {
        ItemSnapshot lost = new ItemSnapshot(UUID.randomUUID(), "Laptop", "silver laptop",
                "ELECTRONICS", "LOST", "Library", LocalDate.of(2026, 1, 1), null, "OPEN", UUID.randomUUID());
        ItemSnapshot found = new ItemSnapshot(UUID.randomUUID(), "Umbrella", "red umbrella",
                "OTHER", "FOUND", "Station", LocalDate.of(2026, 1, 20), null, "OPEN", UUID.randomUUID());

        assertEquals(0, calculator.calculate(lost, found));
    }

    private ItemSnapshot item(String type, String title, LocalDate date) {
        return new ItemSnapshot(UUID.randomUUID(), title, "Item reported with identifying details",
                "ACCESSORIES", type, "Central Library", date, null, "OPEN", UUID.randomUUID());
    }
}
