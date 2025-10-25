package com.jjeanjacques.democoroutines.domain.exceptions

class TimeoutRuntimeException : RuntimeException {
    constructor(message: String) : super(message)
}