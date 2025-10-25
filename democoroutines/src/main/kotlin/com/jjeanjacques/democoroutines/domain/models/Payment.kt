package com.jjeanjacques.democoroutines.domain.models

import com.jjeanjacques.democoroutines.domain.enums.StatusPayment
import com.jjeanjacques.democoroutines.domain.enums.TypePayment
import java.math.BigDecimal
import java.time.Instant

data class Payment(
    val correlationId: String,
    val amount: BigDecimal,
    var requestedAt: Instant? = Instant.now(),
    var type: TypePayment = TypePayment.DEFAULT,
    var workerId: String = "",
    var status: StatusPayment = StatusPayment.PENDING
)