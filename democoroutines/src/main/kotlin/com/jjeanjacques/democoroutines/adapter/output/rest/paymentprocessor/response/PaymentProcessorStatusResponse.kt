package com.jjeanjacques.democoroutines.adapter.output.rest.paymentprocessor.response

data class PaymentProcessorStatusResponse(
    val failing: Boolean,
    val minResponseTime: Long
)
