package com.jjeanjacques.democoroutines.adapter.output.rest.simulation

import com.jjeanjacques.democoroutines.adapter.output.rest.checkin.client.CheckinClient
import com.jjeanjacques.democoroutines.domain.port.output.CheckinPort
import com.jjeanjacques.democoroutines.domain.port.output.SimulationPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SimulationService(
    private val simulationClient: CheckinClient
) : SimulationPort {
    override suspend fun simulate(correlationId: String): Boolean {
        log.info("[${correlationId}] Request checkin")
        return simulationClient.request(correlationId)
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}