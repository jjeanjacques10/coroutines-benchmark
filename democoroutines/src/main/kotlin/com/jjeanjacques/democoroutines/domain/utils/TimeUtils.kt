package com.jjeanjacques.democoroutines.domain.utils

import java.time.Instant
import java.time.format.DateTimeFormatter


fun Instant.toString(isoInstant: DateTimeFormatter): String {
    return isoInstant.format(this)
}
