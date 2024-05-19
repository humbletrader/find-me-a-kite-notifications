package com.github.humbletrader.fmak.notifications.notifications;

import java.nio.file.Path;

public record NotificationEmailFile(Path emailFilePath, String emailRecipient) {
}
