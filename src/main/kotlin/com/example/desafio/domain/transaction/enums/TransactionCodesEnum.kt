package com.example.desafio.domain.transaction.enums

const val defaultClientErrorCode = "07"

enum class TransactionCodesEnum(val externalCodes: String) {
    APPROVED("00"),
    UNAVAILABLE_MAIN_ACCOUNT(defaultClientErrorCode),
    UNAVAILABLE_ACCOUNT_AMOUNT(defaultClientErrorCode),
    UNAVAILABLE_EMPTY_ACCOUNTS_AMOUNT(defaultClientErrorCode),
    REJECT("51"),
    INTERNAL_ERROR(defaultClientErrorCode);

    fun isRejected() = this == REJECT

    fun isError(): Boolean {
        return when (this) {
            INTERNAL_ERROR -> false
            UNAVAILABLE_MAIN_ACCOUNT -> false
            UNAVAILABLE_EMPTY_ACCOUNTS_AMOUNT -> false
            else -> true
        }
    }

    fun isApproved(): Boolean {
        return this == APPROVED
    }
}