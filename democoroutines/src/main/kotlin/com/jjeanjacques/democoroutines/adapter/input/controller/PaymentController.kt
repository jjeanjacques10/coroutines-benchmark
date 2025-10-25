package com.jjeanjacques.democoroutines.adapter.input.controller

import com.jjeanjacques.democoroutines.adapter.input.controller.response.PaymentResponse
import com.jjeanjacques.democoroutines.domain.enums.Strategy
import com.jjeanjacques.democoroutines.domain.models.Payment
import com.jjeanjacques.democoroutines.domain.models.PaymentSummary
import com.jjeanjacques.democoroutines.domain.service.PaymentService
import com.jjeanjacques.democoroutines.domain.service.SummaryService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
class PaymentController(
    private val paymentService: PaymentService,
    private val summaryService: SummaryService
) {

    @PostMapping("/payments")
    suspend fun processPayment(
        @RequestBody request: Payment,
        @RequestHeader ("strategy", defaultValue = "ASYNC_COROUTINE") strategy: Strategy
    ): PaymentResponse {
        log.info("[${request.correlationId}] Received payment request, request: $request")
        val response = paymentService.processPayment(request, strategy)

        return PaymentResponse(status = response.toString())
    }

    @GetMapping("/payments-summary")
    suspend fun summaryPayments(
        @RequestParam(required = false) from: String?,
        @RequestParam(required = false) to: String?
    ): PaymentSummary? {
        return summaryService.getSummary(
            from ?: Instant.now().minusSeconds(3600).toString(),
            to ?: Instant.now().toString()
        )
    }

    companion object {
        private const val SUCCESS = "success"
        private val log = LoggerFactory.getLogger(this::class.java)
    }

}