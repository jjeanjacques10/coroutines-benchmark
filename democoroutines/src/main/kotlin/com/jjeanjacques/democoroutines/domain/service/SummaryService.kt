package com.jjeanjacques.democoroutines.domain.service

import com.jjeanjacques.democoroutines.adapter.output.rest.paymentprocessor.PaymentProcessorService
import com.jjeanjacques.democoroutines.domain.enums.StatusPayment
import com.jjeanjacques.democoroutines.domain.enums.Strategy
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
class SummaryService(
    private val paymentRepository: PaymentRepository
) {

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