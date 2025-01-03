package cz.diplomka.pivovar.dto;

public record StepResponse(int timeInSeconds, boolean continueTimer, double targetTempMash, double targetTempWorth) {
}
