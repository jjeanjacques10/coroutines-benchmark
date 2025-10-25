package com.jjeanjacques.democoroutines.adapter.output.rest.checkin

import com.jjeanjacques.democoroutines.adapter.output.rest.checkin.client.CheckinClient
import com.jjeanjacques.democoroutines.domain.port.output.CheckinPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CheckinService(
    private val checkinClient: CheckinClient
) : CheckinPort {
    override suspend fun checkin(correlationId: String): Boolean {
        log.info("[${correlationId}] Request checkin")
        return checkinClient.request(correlationId)
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}