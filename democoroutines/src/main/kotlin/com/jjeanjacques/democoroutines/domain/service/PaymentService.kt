package com.jjeanjacques.democoroutines.domain.service

import com.jjeanjacques.democoroutines.adapter.output.rest.paymentprocessor.PaymentProcessorService
import com.jjeanjacques.democoroutines.domain.enums.StatusPayment
import com.jjeanjacques.democoroutines.domain.enums.TypePayment
import com.jjeanjacques.democoroutines.domain.exceptions.AlreadyProcessedRuntimeException
import com.jjeanjacques.democoroutines.domain.models.DefaultDetails
import com.jjeanjacques.democoroutines.domain.models.FallbackDetails
import com.jjeanjacques.democoroutines.domain.models.Payment
import com.jjeanjacques.democoroutines.domain.models.PaymentSummary
import com.jjeanjacques.democoroutines.domain.port.output.CheckinPort
import com.jjeanjacques.democoroutines.domain.port.output.PaymentRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class PaymentService(
    private val paymentProcessorService: PaymentProcessorService,
    private val checkinPort: CheckinPort,
    private val paymentRepository: PaymentRepository
) {
    
    suspend fun processPayment(payment: Payment): StatusPayment {
        try {
            log.info("[${payment.correlationId}] Requesting payment, requested at: ${payment.requestedAt}")

            val (checkin, statusPayment) = coroutineScope {
                val checkinDeferred = async { checkinPort.checkin(payment.correlationId) }
                val statusDeferred = async { getStatusProcess(payment) }
                Pair(checkinDeferred.await(), statusDeferred.await())
            }

            log.info("[${payment.correlationId}] Check-in result: $checkin and Payment status: $statusPayment")
            when (statusPayment) {
                StatusPayment.SUCCESS -> paymentRepository.save(payment, statusPayment)
                else -> throw IllegalStateException("Error processing payment with status: $statusPayment")
            }
            return statusPayment
        } catch (ex: Exception) {
            log.error("[${payment.correlationId}] Failed to process pending payment", ex)
        }
        return StatusPayment.ERROR
    }

    private suspend fun getStatusProcess(payment: Payment) = try {
        paymentProcessorService.callPaymentProcessor(payment)
        log.info("[${payment.correlationId}] Payment processor call successful")
        StatusPayment.SUCCESS
    } catch (_: AlreadyProcessedRuntimeException) {
        log.info("[${payment.correlationId}] Payment with correlation ID: ${payment.correlationId} already processed, skipping.")
        StatusPayment.SUCCESS
    }

    suspend fun getSummary(from: String, to: String): PaymentSummary? {
        val fromInstant = Instant.parse(if (from.endsWith("Z")) from else "${from}Z")
        val toInstant = Instant.parse(if (to.endsWith("Z")) to else "${to}Z")

        val payments = paymentRepository.findByDateRange(fromInstant, toInstant)

        val paymentsDefault = payments.filter { it.type == TypePayment.DEFAULT }
        val paymentsFallback = payments.filter { it.type == TypePayment.FALLBACK }

        log.info(
            "Payments summary from $from to $to: Default requests: ${paymentsDefault.size}, " +
                    "Total amount: ${paymentsDefault.sumOf { it.amount }}, " +
                    "Fallback requests: ${paymentsFallback.size}, " +
                    "Total amount: ${paymentsFallback.sumOf { it.amount }}"
        )

        return PaymentSummary(
            default = DefaultDetails(
                totalRequests = paymentsDefault.size,
                totalAmount = paymentsDefault.sumOf { it.amount }
            ),
            fallback = FallbackDetails(
                totalRequests = paymentsFallback.size,
                totalAmount = paymentsFallback.sumOf { it.amount }
            )
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}