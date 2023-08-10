package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.exception.BadRequestException;

import java.util.Optional;

public enum State {
	// Все
	ALL,
	// Текущие
	CURRENT,
	// Будущие
	FUTURE,
	// Завершенные
	PAST,
	// Отклоненные
	REJECTED,
	// Ожидающие подтверждения
	WAITING;

	public static Optional<State> from(String stringState) {
		for (State state : values()) {
			if (state.name().equalsIgnoreCase(stringState)) {
				return Optional.of(state);
			}
		}
		throw new BadRequestException("Unknown state: UNSUPPORTED_STATUS");
	}
}
