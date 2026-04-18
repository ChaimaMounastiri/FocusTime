package com.example.studysmart.util

import java.time.LocalDate
import java.time.ZoneId

fun localDayBoundsMillis(zone: ZoneId = ZoneId.systemDefault()): Pair<Long, Long> {
    val today = LocalDate.now(zone)
    val start = today.atStartOfDay(zone).toInstant().toEpochMilli()
    val end = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    return start to end
}
