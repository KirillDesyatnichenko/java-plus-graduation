package ru.yandex.practicum.client.exception;

public class StatsServerUnavailable extends StatsClientException {
    public StatsServerUnavailable(String message, Throwable cause) {
        super(message, cause);
    }
}
