package com.paytheory.lib.reactors

// Simplified test interfaces for testing
interface TestPayable {
    fun handleError(error: TestPTError)
}

enum class TestErrorCode {
    SocketError, TokenFailed
}

data class TestPTError(
    val code: TestErrorCode,
    val message: String
) 