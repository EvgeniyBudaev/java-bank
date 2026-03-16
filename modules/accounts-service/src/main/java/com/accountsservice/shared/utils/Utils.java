package com.accountsservice.shared.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class Utils {
    public static LocalDateTime getNowUtc() {
//        ZonedDateTime nowUTC = ZonedDateTime.now(ZoneOffset.UTC); // Получаем текущее время в формате UTC
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);
        // Получаем текущее время в формате UTC как Instant
        Instant instant = Instant.now();
        // Преобразуем Instant в ZonedDateTime, указывая, что время в UTC
        ZonedDateTime zonedDateTime = instant.atZone(ZoneOffset.UTC);
        // Из ZonedDateTime извлекаем LocalDateTime
        return zonedDateTime.toLocalDateTime();
    }
}
