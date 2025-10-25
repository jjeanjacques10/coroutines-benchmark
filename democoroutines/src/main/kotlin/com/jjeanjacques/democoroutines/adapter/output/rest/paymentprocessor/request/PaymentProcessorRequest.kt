package com.jjeanjacques.democoroutines.adapter.output.rest.paymentprocessor.request

import java.math.BigDecimal

data class PaymentProcessorRequest(
    val correlationId: String,
    val amount: BigDecimal,
    var requestedAt: String
)