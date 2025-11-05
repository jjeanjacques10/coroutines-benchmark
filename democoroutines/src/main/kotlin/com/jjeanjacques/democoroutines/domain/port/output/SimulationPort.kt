package com.jjeanjacques.democoroutines.domain.port.output

interface SimulationPort {
    suspend fun simulate(correlationId: String): Boolean
}