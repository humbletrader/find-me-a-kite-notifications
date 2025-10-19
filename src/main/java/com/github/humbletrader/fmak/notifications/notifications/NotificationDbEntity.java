package com.github.humbletrader.fmak.notifications.notifications;

public record NotificationDbEntity(int id,
                                   String name,
                                   String email,
                                   String queryAsJson,
                                   int runId) {
}
