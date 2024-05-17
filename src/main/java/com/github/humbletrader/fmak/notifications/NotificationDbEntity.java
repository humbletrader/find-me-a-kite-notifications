package com.github.humbletrader.fmak.notifications;

public record NotificationDbEntity(int id, String email, String queryAsJson, int runCount) {
}
