package com.jjeanjacques.democoroutines.domain.service

import com.jjeanjacques.democoroutines.adapter.output.rest.paymentprocessor.PaymentProcessorService
import com.jjeanjacques.democoroutines.domain.enums.StatusPayment
import com.jjeanjacques.democoroutines.domain.enums.Strategy
import com.jjeanjacques.democoroutines.domain.exceptions.AlreadyProcessedRuntimeException
import com.jjeanjacques.democoroutines.domain.models.Payment
import com.jjeanjacques.democoroutines.domain.port.output.CheckinPort
import com.jjeanjacques.democoroutines.domain.port.output.PaymentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PaymentService(
    private val paymentProcessorService: PaymentProcessorService,
    private val checkinPort: CheckinPort,
    private val paymentRepository: PaymentRepository
) {

    suspend fun processPayment(payment: Payment, strategy: Strategy): StatusPayment {
        try {
            log.info("[${payment.correlationId}] Requesting payment, requested at: ${payment.requestedAt}")

            val (checking, statusPayment) = getStrategyProcessor(strategy, payment)

            log.info("[${payment.correlationId}] Check-in result: $checking and Payment status: $statusPayment")
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

    suspend fun getStrategyProcessor(strategy: Strategy, payment: Payment): Pair<Boolean, StatusPayment> {
        return when (strategy) {
            Strategy.ASYNC_COROUTINE -> processAsyncCoroutineStrategy(payment)
            Strategy.SEQUENTIAL -> processSequentialStrategy(payment)
            Strategy.BLOCKING_THREAD -> blockingThreadStrategy(payment)
        }
    }

    suspend fun processAsyncCoroutineStrategy(payment: Payment): Pair<Boolean, StatusPayment> {
        return coroutineScope {
            val checking = async { checkinPort.checkin(payment.correlationId) }
            val status = async { getStatusProcess(payment) }
            Pair(checking.await(), status.await())
        }
    }

    suspend fun processSequentialStrategy(payment: Payment): Pair<Boolean, StatusPayment> {
        val checkin = checkinPort.checkin(payment.correlationId)
        val statusPayment = getStatusProcess(payment)
        return Pair(checkin, statusPayment)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun blockingThreadStrategy(payment: Payment): Pair<Boolean, StatusPayment> {
        var checking: Boolean
        var status: StatusPayment
        // Use runBlocking on Dispatchers.IO to avoid blocking the caller's event-loop thread.
        // Add a timeout so the blocking call won't hang forever.
        runBlocking(Dispatchers.IO) {
            withTimeout(10_000) {
                supervisorScope {
                    val checkingResponse = async { checkinPort.checkin(payment.correlationId) }
                    val statusResponse = async { getStatusProcess(payment) }

                    checking = checkingResponse.await()
                    status = statusResponse.await()
                }
            }
        }
        return  Pair(checking, status)
    }

    private suspend fun getStatusProcess(payment: Payment) = try {
        paymentProcessorService.callPaymentProcessor(payment)
        log.info("[${payment.correlationId}] Payment processor call successful")
        StatusPayment.SUCCESS
    } catch (_: AlreadyProcessedRuntimeException) {
        log.info("[${payment.correlationId}] Payment with correlation ID: ${payment.correlationId} already processed, skipping.")
        StatusPayment.SUCCESS
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}