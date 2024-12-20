package com.example.desafio.domain.transaction.enums

enum class TransactionCode(val code: String) {
    AVAILABLE("00"),
    UNAVAILABLE_ACCOUNT("07"),
    REJECT("51"),
    INTERNAL_ERROR("07");

    fun isRejected(): Boolean {
        return isError() || this == REJECT
    }

    fun isError(): Boolean {
        return this == UNAVAILABLE_ACCOUNT || this == INTERNAL_ERROR
    }
}