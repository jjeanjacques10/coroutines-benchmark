package com.jjeanjacques.democoroutines.adapter.output.rest.paymentprocessor.response

import java.math.BigDecimal
import java.time.Instant

data class PaymentDetailsResponse(
    val correlationId: String,
    val amount: BigDecimal,
    val requested_at: Instant
)