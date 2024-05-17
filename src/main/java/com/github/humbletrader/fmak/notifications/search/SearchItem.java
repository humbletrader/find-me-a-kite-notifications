package com.github.humbletrader.fmak.notifications.search;

public record SearchItem(
        String link,
        String brandNameVersion,
        double price,
        String size,
        String condition) {
}
