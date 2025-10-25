package com.jjeanjacques.democoroutines.domain.exceptions

class IntegrationException : RuntimeException {
    constructor(message: String) : super(message)
}