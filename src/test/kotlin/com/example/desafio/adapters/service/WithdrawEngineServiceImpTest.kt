package com.example.desafio.adapters.service

import com.example.desafio.adapters.jpa_database.entity.AccountTypeEntity
import com.example.desafio.domain.account.Account
import com.example.desafio.domain.account.AccountAmount
import com.example.desafio.domain.transaction.Transaction
import com.example.desafio.domain.transaction.Withdraw
import com.example.desafio.domain.transaction.enums.MerchantCategory
import com.example.desafio.domain.transaction.enums.TransactionCodesEnum
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class WithdrawEngineServiceImpTest {
    @Test
    fun `should approved withdraw when totalAmount is equals accountAmount`() {
        val accountAmount = spyk(AccountAmount(1, 100.toBigDecimal(), mockk<AccountTypeEntity>()))
        val transaction = Transaction(
            100.toBigDecimal(),
            MerchantCategory.FOOD,
            "padaria",
            "1",
            TransactionCodesEnum.REJECT
        )
        val expectedWithdraw = Withdraw(
            status = TransactionCodesEnum.APPROVED,
            totalAmount = 0.toBigDecimal(),
            accountAmount = accountAmount,
            merchantCategory = MerchantCategory.FOOD,
            pendingValue = 0.toBigDecimal(),
        )

        val result = WithdrawEngineServiceImp().process(transaction, accountAmount)

        assertEquals(expectedWithdraw, result)
    }

    @Test
    fun `should approved withdraw when totalAmount is less than accountAmount`() {
        val accountAmount = spyk(AccountAmount(1, 100.toBigDecimal(), mockk<AccountTypeEntity>()))
        val transactionValue = 50.toBigDecimal()
        val transaction = Transaction(
            transactionValue,
            MerchantCategory.FOOD,
            "padaria",
            "1",
            TransactionCodesEnum.REJECT
        )
        val expectedWithdraw = Withdraw(
            status = TransactionCodesEnum.APPROVED,
            totalAmount = transactionValue,
            accountAmount = accountAmount,
            merchantCategory = MerchantCategory.FOOD,
            pendingValue = BigDecimal.ZERO,
        )

        val result = WithdrawEngineServiceImp().process(transaction, accountAmount)

        assertEquals(expectedWithdraw, result)
    }

    @Test
    fun `should reject withdraw when totalAmount is greater  than accountAmount`() {
        val accountAmount = spyk(AccountAmount(1, 100.toBigDecimal(), mockk<AccountTypeEntity>()))
        val transactionValue = 150.toBigDecimal()
        val transaction = Transaction(
            transactionValue,
            MerchantCategory.FOOD,
            "padaria",
            "1",
            TransactionCodesEnum.REJECT
        )
        val expectedWithdraw = Withdraw(
            status = TransactionCodesEnum.REJECT,
            totalAmount = (-50).toBigDecimal(),
            accountAmount = accountAmount,
            merchantCategory = MerchantCategory.FOOD,
            pendingValue = 50.toBigDecimal(),
        )

        val result = WithdrawEngineServiceImp().process(transaction, accountAmount)

        assertEquals(expectedWithdraw, result)
    }

    @Test
    fun `should has pendingValue and use total of account amount when withdraw when totalAmount is greater than accountAmount`() {
        val transactionValue = 100.toBigDecimal()
        val accountMock = Account(
            id = 1,
            status = "",
            availableAccountsAmount = hashMapOf(
                Pair("food", generateAccountAmount(balance = 50.toBigDecimal(), pendingBalance = 50.toBigDecimal())),
                Pair("meal", generateAccountAmount(balance = 50.toBigDecimal(), pendingBalance = 100.toBigDecimal())),
                Pair("cash", generateAccountAmount(balance = 200.toBigDecimal())),
            )
        )
        val transaction = Transaction(
            transactionValue,
            MerchantCategory.FOOD,
            "padaria",
            "1",
            TransactionCodesEnum.REJECT
        )
        val expectedWithdraw = Withdraw(
            status = TransactionCodesEnum.REJECT,
            totalAmount = 50.toBigDecimal(),
            accountAmount = accountMock.availableAccountsAmount["food"]!!,
            merchantCategory = MerchantCategory.FOOD,
            pendingValue = 50.toBigDecimal(),
        )
        val withDrawList = mutableListOf<Withdraw>()

        val result = WithdrawEngineServiceImp().processForFallback(
            transaction,
            accountMock,
            MerchantCategory.FOOD,
            withDrawList
        )

        assertEquals(expectedWithdraw, result)
    }

    @Test
    fun `should returns 100,00 of totalAmount when pendingValue is 50 and totalAmount is 150`() {
        val transactionValue = 100.toBigDecimal()
        val accountMock = Account(
            id = 1,
            status = "",
            availableAccountsAmount = hashMapOf(
                Pair("food", generateAccountAmount(balance = 50.toBigDecimal(), pendingBalance = 50.toBigDecimal())),
                Pair("meal", generateAccountAmount(balance = 50.toBigDecimal(), pendingBalance = 100.toBigDecimal())),
                Pair("cash", generateAccountAmount(balance = 200.toBigDecimal(), authorizeWithdraw = true)),
            )
        )
        val transaction = Transaction(
            transactionValue,
            MerchantCategory.FOOD,
            "padaria",
            "1",
            TransactionCodesEnum.REJECT
        )
        val expectedWithdraw = Withdraw(
            status = TransactionCodesEnum.REJECT,
            totalAmount = 100.toBigDecimal(),
            accountAmount = accountMock.availableAccountsAmount["cash"]!!,
            merchantCategory = MerchantCategory.CASH,
            pendingValue = BigDecimal.ZERO,
        )
        val withDrawList = mutableListOf(
            Withdraw(
                status = TransactionCodesEnum.REJECT,
                totalAmount = 50.toBigDecimal(),
                accountAmount = accountMock.availableAccountsAmount["food"]!!,
                merchantCategory = MerchantCategory.FOOD,
                pendingValue = 100.toBigDecimal(),
            )
        )

        val result = WithdrawEngineServiceImp().processForFallback(
            transaction,
            accountMock,
            MerchantCategory.CASH,
            withDrawList
        )

        assertEquals(expectedWithdraw, result)
    }


    @Test
    fun `should returns null of accountAmount is empty balance`() {
        val transactionValue = 100.toBigDecimal()
        val accountMock = Account(
            id = 1,
            status = "",
            availableAccountsAmount = hashMapOf(
                Pair("food", generateAccountAmount(balance = 50.toBigDecimal(), pendingBalance = 50.toBigDecimal())),
                Pair("meal", generateAccountAmount(balance = 50.toBigDecimal(), pendingBalance = 100.toBigDecimal())),
                Pair("cash", generateAccountAmount(balance = BigDecimal.ZERO)),
            )
        )
        val transaction = Transaction(
            transactionValue,
            MerchantCategory.FOOD,
            "padaria",
            "1",
            TransactionCodesEnum.REJECT
        )
        val withDrawList = mutableListOf(
            Withdraw(
                status = TransactionCodesEnum.REJECT,
                totalAmount = 50.toBigDecimal(),
                accountAmount = accountMock.availableAccountsAmount["food"]!!,
                merchantCategory = MerchantCategory.FOOD,
                pendingValue = 100.toBigDecimal(),
            )
        )

        val result = WithdrawEngineServiceImp().processForFallback(
            transaction,
            accountMock,
            MerchantCategory.CASH,
            withDrawList
        )

        assertNull(result)
    }

    @Test
    fun `should returns null when not has pending value`() {
        val transactionValue = 100.toBigDecimal()
        val accountMock = Account(
            id = 1,
            status = "",
            availableAccountsAmount = hashMapOf(
                Pair("food", generateAccountAmount(balance = 50.toBigDecimal(), pendingBalance = 50.toBigDecimal())),
                Pair("meal", generateAccountAmount(balance = 50.toBigDecimal(), pendingBalance = 100.toBigDecimal())),
                Pair("cash", generateAccountAmount(balance = 50.toBigDecimal())),
            )
        )
        val transaction = Transaction(
            transactionValue,
            MerchantCategory.FOOD,
            "padaria",
            "1",
            TransactionCodesEnum.REJECT
        )
        val withDrawList = mutableListOf(
            Withdraw(
                status = TransactionCodesEnum.REJECT,
                totalAmount = 50.toBigDecimal(),
                accountAmount = accountMock.availableAccountsAmount["food"]!!,
                merchantCategory = MerchantCategory.FOOD,
                pendingValue = BigDecimal.ZERO,
            )
        )

        val result = WithdrawEngineServiceImp().processForFallback(
            transaction,
            accountMock,
            MerchantCategory.CASH,
            withDrawList
        )

        assertNull(result)
    }

    private fun generateAccountAmount(
        mcc: MerchantCategory = MerchantCategory.CASH,
        balance: BigDecimal = BigDecimal.ZERO,
        pendingBalance: BigDecimal = BigDecimal.ZERO,
        authorizeWithdraw: Boolean = false,
    ): AccountAmount {
        val accountAmount = mockk<AccountAmount>()

        with(accountAmount) {
            every {
                authorizeWithdraw(any())
            } returns authorizeWithdraw

            every {
                toWithdraw(any())
                value
            } returns balance

            every {
                toPendingBalance(any())
            } returns pendingBalance
        }

        return accountAmount
    }
}