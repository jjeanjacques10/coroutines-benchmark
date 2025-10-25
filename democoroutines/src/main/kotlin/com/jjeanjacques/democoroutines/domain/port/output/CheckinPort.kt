package com.jjeanjacques.democoroutines.domain.port.output

interface CheckinPort {
    suspend fun checkin(correlationId: String): Boolean
}