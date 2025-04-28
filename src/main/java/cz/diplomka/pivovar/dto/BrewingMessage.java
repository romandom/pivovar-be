package cz.diplomka.pivovar.dto;

import cz.diplomka.pivovar.constant.MessageType;

public record BrewingMessage(MessageType messageType, String message, String nextStep) {
}
