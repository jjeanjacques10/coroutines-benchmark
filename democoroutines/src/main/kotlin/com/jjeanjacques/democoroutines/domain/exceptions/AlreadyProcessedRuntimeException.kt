package com.jjeanjacques.democoroutines.domain.exceptions

class AlreadyProcessedRuntimeException : RuntimeException {
    constructor(message: String) : super(message)
}