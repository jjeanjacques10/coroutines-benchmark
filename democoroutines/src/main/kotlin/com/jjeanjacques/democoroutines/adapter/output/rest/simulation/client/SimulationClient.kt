package com.jjeanjacques.democoroutines.adapter.output.rest.simulation.client

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import kotlinx.coroutines.reactor.awaitSingle
import com.jjeanjacques.democoroutines.adapter.output.rest.checkin.response.CheckinResponse
import com.jjeanjacques.democoroutines.adapter.output.rest.simulation.response.SimulationResponse

@Component
class SimulationClient(
    @Qualifier("checkin")
    private val webClient: WebClient,
) {
    suspend fun request(correlationId: String): Boolean {
        val response = webClient.post()
            .uri { builder -> builder.path("/simulation").build() }
            .bodyValue(correlationId)
            .header("X-Correlation-Id", correlationId)
            .retrieve()
            .bodyToMono(SimulationResponse::class.java)
            .awaitSingle()

        return response.authorized
    }
}