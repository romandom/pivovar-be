package cz.diplomka.pivovar.dto;

import cz.diplomka.pivovar.constant.BrewingStatus;

import java.time.LocalDate;

public record HistoryList(long id, LocalDate date, String recipeName, BrewingStatus brewingStatus) {
}
