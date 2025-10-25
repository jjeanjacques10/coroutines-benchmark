package com.jjeanjacques.democoroutines.adapter.output.rest.paymentprocessor

import com.jjeanjacques.democoroutines.adapter.output.rest.paymentprocessor.client.PaymentProcessorClient
import com.jjeanjacques.democoroutines.adapter.output.rest.paymentprocessor.request.PaymentProcessorRequest
import com.jjeanjacques.democoroutines.domain.enums.TypePayment
import com.jjeanjacques.democoroutines.domain.exceptions.AlreadyProcessedRuntimeException
import com.jjeanjacques.democoroutines.domain.exceptions.IntegrationException
import com.jjeanjacques.democoroutines.domain.exceptions.TimeoutRuntimeException
import com.jjeanjacques.democoroutines.domain.models.Payment
import com.jjeanjacques.democoroutines.domain.utils.toString
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.http.HttpStatusCode
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.format.DateTimeFormatter


@Component
class PaymentProcessorService(
    private val paymentProcessorClient: PaymentProcessorClient,
    private val paymentProcessorFallbackClient: PaymentProcessorClient
) {
    suspend fun callPaymentProcessor(payment: Payment) {
        try {
            when (payment.type) {
                TypePayment.DEFAULT -> requestPaymentDefault(payment)
                TypePayment.FALLBACK -> requestFallBackPayment(payment)
                TypePayment.TIMEOUT -> throw IllegalArgumentException("Unsupported payment type: ${payment.type}")
            }
        } catch (ex: WebClientResponseException) {
            log.error("[${payment.correlationId}] WebClientResponseException calling payment processor: ${ex.message}", ex)
            if (ex.statusCode == HttpStatusCode.valueOf(422)) {
                throw AlreadyProcessedRuntimeException("Payment with correlation ID ${payment.correlationId} has already been processed or is invalid.")
            }
            if (ex.statusCode == HttpStatusCode.valueOf(408)) {
                throw TimeoutRuntimeException("Payment request timed out for correlation ID ${payment.correlationId}.")
            }
            if (ex.statusCode == HttpStatusCode.valueOf(500)) {
                throw IntegrationException("Payment processor is unavailable")
            }
            throw ex.also {
                log.error("[${payment.correlationId}] Failed to process payment with correlation ID: ${payment.correlationId} and status: ${ex.statusCode}", it)
            }
        } catch (ex: Exception) {
            log.error("[${payment.correlationId}] Error calling payment processor correlation ID: ${ex.message}", ex)
            throw IntegrationException("Payment processor error for correlation ID ${payment.correlationId}: ${ex.message}")
        }
    }

    suspend fun requestPaymentDefault(payment: Payment) {
        paymentProcessorClient.requestPaymentProcessorDefault(
            PaymentProcessorRequest(
                correlationId = payment.correlationId,
                amount = payment.amount,
                requestedAt = payment.requestedAt!!.toString(
                    DateTimeFormatter.ISO_INSTANT
                )
            )
        )
    }

    suspend fun requestFallBackPayment(payment: Payment) {
        log.warn("[${payment.correlationId}] Falling back to fallback payment processor")
        paymentProcessorFallbackClient.requestPaymentProcessorFallback(
            PaymentProcessorRequest(
                correlationId = payment.correlationId,
                amount = payment.amount,
                requestedAt = payment.requestedAt!!.toString(
                    DateTimeFormatter.ISO_INSTANT
                )
            )
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
