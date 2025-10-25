package com.jjeanjacques.democoroutines.adapter.output.rest.checkin.client

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import kotlinx.coroutines.reactor.awaitSingle
import com.jjeanjacques.democoroutines.adapter.output.rest.checkin.response.CheckinResponse

@Component
class CheckinClient(
    @Qualifier("checkin")
    private val webClient: WebClient,
) {
    suspend fun request(correlationId: String): Boolean {
        val response = webClient.post()
            .uri { builder -> builder.path("/check-in").build() }
            .bodyValue(correlationId)
            .header("X-Correlation-Id", correlationId)
            .retrieve()
            .bodyToMono(CheckinResponse::class.java)
            .awaitSingle()

        return response.authorized
    }
}