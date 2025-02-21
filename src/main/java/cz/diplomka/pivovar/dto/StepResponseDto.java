package cz.diplomka.pivovar.dto;

public record StepResponseDto(int timeInSeconds, boolean continueTimer, double targetTempMash, double targetTempWorth) {
}
