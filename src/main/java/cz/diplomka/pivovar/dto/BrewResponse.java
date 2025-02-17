package cz.diplomka.pivovar.dto;

public record BrewResponse(String message, int targetTemperature, int targetDuration,
                           Integer nextTemperature, Integer nextDuration) {
}
