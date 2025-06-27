package com.sg.obs.base.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DatetimeUtils {

    private DatetimeUtils() {
    }

    public static String formatDateShort(LocalDateTime dateTime) {
        return Optional.ofNullable(dateTime)
                .map(date -> date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")))
                .orElse("");
    }
}
