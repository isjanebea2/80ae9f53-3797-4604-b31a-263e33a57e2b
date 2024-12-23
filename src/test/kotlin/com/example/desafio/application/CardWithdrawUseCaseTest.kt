package com.example.desafio.application

import com.example.desafio.adapters.service.WithdrawEngineServiceImp
import com.example.desafio.application.ports.`in`.AccountService
import com.example.desafio.application.ports.`in`.StatementHistoryService
import com.example.desafio.domain.account.Account
import com.example.desafio.domain.account.AccountAmount
import com.example.desafio.domain.transaction.Transaction
import com.example.desafio.domain.transaction.Withdraw
import com.example.desafio.domain.transaction.enums.MerchantCategory
import com.example.desafio.domain.transaction.enums.TransactionCodesEnum
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.spyk
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal

class FakeAccountServiceImp : AccountService {
    override fun findAccountById(id: Long): Account? = null
    override fun withdraw(withdraw: Withdraw): Boolean = false
    override fun withdraw(withdrawList: List<Withdraw>): Boolean = false
}

class FakeStatementHistoryServiceImp : StatementHistoryService {
    override fun withdrawSave(accountAmountId: Long, transaction: Transaction) = Unit
}

private val withdrawEngineServiceImp = WithdrawEngineServiceImp()


class CardWithdrawUseCaseTest {

    @Test
    fun `should returns UNAVAILABLE_MAIN_ACCOUNT when unavailable account`() {
        val accountService = spyk(FakeAccountServiceImp())
        val statementHistoryService = spyk(FakeStatementHistoryServiceImp())
        val cardWithdrawUseCase = CardWithdrawUseCase(accountService, statementHistoryService, withdrawEngineServiceImp)
        val accountId = 0.toLong()
        every {
            accountService.findAccountById(accountId)
        } returns null

        val result = cardWithdrawUseCase.execute(generateTransaction())

        assertEquals(result.status, TransactionCodesEnum.UNAVAILABLE_MAIN_ACCOUNT)
    }

    @Test
    fun `should returns UNAVAILABLE_EMPTY_ACCOUNTS_AMOUNT when unavailable accountAmounts`() {
        val accountService = mockkClass(FakeAccountServiceImp::class)
        val statementHistoryService = spyk(FakeStatementHistoryServiceImp())
        val accountMock = mockk<Account>()
        val cardWithdrawUseCase = CardWithdrawUseCase(accountService, statementHistoryService, withdrawEngineServiceImp)
        every {
            accountService.findAccountById(any())
        } returns accountMock
        every {
            accountMock.notAvailableAccountsAmount()
        } returns true

        val result = cardWithdrawUseCase.execute(generateTransaction())

        assertEquals(result.status, TransactionCodesEnum.UNAVAILABLE_EMPTY_ACCOUNTS_AMOUNT)
    }

    @Test
    fun `should returns REJECT (processWithdrawList fallback) when not allowed for category`() {
        val accountService = mockkClass(FakeAccountServiceImp::class)
        val statementHistoryService = FakeStatementHistoryServiceImp()
        val accountMock = mockk<Account>()
        val accountAmount = generateAccountAmount()
        val cardWithdrawUseCase = CardWithdrawUseCase(accountService, statementHistoryService, withdrawEngineServiceImp)
        every {
            accountService.findAccountById(any())
        } returns accountMock

        every {
            accountMock.notAvailableAccountsAmount()
        } returns false

        every {
            accountMock.isAvailableBalance(any())
        } returns true

        every {
            accountMock.findAccountAmountByMcc(any())
        } returns accountAmount
        val transaction = generateTransaction().copy(
            merchantCategory = MerchantCategory.CASH
        )

        val result = cardWithdrawUseCase.execute(transaction)

        assertEquals(TransactionCodesEnum.REJECT, result.status)
    }


    @Test
    fun `should returns REJECT when not has balance`() {
        val accountService = mockkClass(FakeAccountServiceImp::class)
        val statementHistoryService = FakeStatementHistoryServiceImp()
        val cardWithdrawUseCase = CardWithdrawUseCase(accountService, statementHistoryService, withdrawEngineServiceImp)
        val accountMock = Account(
            id = 1,
            status = "",
            availableAccountsAmount = hashMapOf(
                Pair("food", generateAccountAmount(balance = 50.toBigDecimal(), pendingBalance = 150.toBigDecimal())),
                Pair("meal", generateAccountAmount(balance = 50.toBigDecimal(), pendingBalance = 100.toBigDecimal())),
                Pair("cash", generateAccountAmount(balance = 50.toBigDecimal(), pendingBalance = 50.toBigDecimal())),
            )
        )
        every {
            accountService.findAccountById(any())
        } returns accountMock
        val transaction = generateTransaction().copy(
            totalWithdrawalAmount = 200.toBigDecimal(),
            merchantCategory = MerchantCategory.FOOD
        )

        val result = cardWithdrawUseCase.execute(transaction)

        assertEquals(TransactionCodesEnum.REJECT, result.status)
    }

    @Test
    fun `returns approved when without fallback`() {
        val accountService = mockkClass(FakeAccountServiceImp::class)
        val statementHistoryService = FakeStatementHistoryServiceImp()
        val cardWithdrawUseCase = CardWithdrawUseCase(accountService, statementHistoryService, withdrawEngineServiceImp)
        val accountMock = Account(
            id = 1,
            status = "",
            availableAccountsAmount = hashMapOf(
                Pair("food", generateAccountAmount(balance = 50.toBigDecimal(), pendingBalance = 150.toBigDecimal())),
                Pair(
                    "meal", generateAccountAmount(
                        balance = 100.toBigDecimal(),
                        pendingBalance = 0.toBigDecimal(),
                        authorizeWithdraw = true
                    )
                ),
                Pair("cash", generateAccountAmount(balance = 50.toBigDecimal(), pendingBalance = 50.toBigDecimal())),
            )
        )
        val transaction = generateTransaction().copy(
            totalWithdrawalAmount = 200.toBigDecimal(),
            merchantCategory = MerchantCategory.MEAL
        )
        every {
            accountService.findAccountById(any())
        } returns accountMock
        every {
            accountService.withdraw(withdraw = any())
        } returns true

        val result = cardWithdrawUseCase.execute(transaction)

        assertEquals(TransactionCodesEnum.APPROVED, result.status)
    }

    @Test
    fun `returns approved when with fallback for meal`() {
        val accountService = mockkClass(FakeAccountServiceImp::class)
        val statementHistoryService = FakeStatementHistoryServiceImp()
        val cardWithdrawUseCase = CardWithdrawUseCase(accountService, statementHistoryService, withdrawEngineServiceImp)
        val accountMock = Account(
            id = 1,
            status = "",
            availableAccountsAmount = hashMapOf(
                Pair("food", generateAccountAmount(balance = 1000.toBigDecimal())),
                Pair(
                    "meal", generateAccountAmount(
                        balance = 50.toBigDecimal(),
                        pendingBalance = 50.toBigDecimal()
                    )
                ),
                Pair(
                    "cash", generateAccountAmount(
                        balance = 100.toBigDecimal(),
                        pendingBalance = 0.toBigDecimal(),
                        authorizeWithdraw = true
                    )
                ),
            )
        )
        val transaction = generateTransaction().copy(
            totalWithdrawalAmount = 100.toBigDecimal(),
            merchantCategory = MerchantCategory.MEAL
        )
        every {
            accountService.findAccountById(any())
        } returns accountMock
        every {
            accountService.withdraw(withdrawList = any())
        } returns true

        val result = cardWithdrawUseCase.execute(transaction)

        assertEquals(TransactionCodesEnum.APPROVED, result.status)
    }

    @Test
    fun `returns approved when combines food and cash`() {
        val accountService = mockkClass(FakeAccountServiceImp::class)
        val statementHistoryService = FakeStatementHistoryServiceImp()
        val cardWithdrawUseCase = CardWithdrawUseCase(accountService, statementHistoryService, withdrawEngineServiceImp)
        val accountMock = Account(
            id = 1,
            status = "",
            availableAccountsAmount = hashMapOf(
                Pair("food", generateAccountAmount(
                    balance = 1000.toBigDecimal(),
                    pendingBalance = 2000.toBigDecimal()
                )),
                Pair(
                    "meal", generateAccountAmount(balance = BigDecimal.ZERO)
                ),
                Pair(
                    "cash", generateAccountAmount(
                        balance = 2000.toBigDecimal(),
                        pendingBalance = BigDecimal.ZERO,
                        authorizeWithdraw = true
                    )
                ),
            )
        )
        val transaction = generateTransaction().copy(
            totalWithdrawalAmount = 3000.toBigDecimal(),
            merchantCategory = MerchantCategory.FOOD
        )
        every {
            accountService.findAccountById(any())
        } returns accountMock
        every {
            accountService.withdraw(withdrawList = any())
        } returns true

        val result = cardWithdrawUseCase.execute(transaction)

        assertEquals(TransactionCodesEnum.APPROVED, result.status)
    }

    @Test
    fun `returns INTERNAL_ERROR with combines food and cash when throws exception`() {
        val accountService = mockkClass(FakeAccountServiceImp::class)
        val statementHistoryService = FakeStatementHistoryServiceImp()
        val cardWithdrawUseCase = CardWithdrawUseCase(accountService, statementHistoryService, withdrawEngineServiceImp)
        val accountMock = Account(
            id = 1,
            status = "",
            availableAccountsAmount = hashMapOf(
                Pair("food", generateAccountAmount(
                    balance = 1000.toBigDecimal(),
                    pendingBalance = 2000.toBigDecimal()
                )),
                Pair(
                    "meal", generateAccountAmount(balance = BigDecimal.ZERO)
                ),
                Pair(
                    "cash", generateAccountAmount(
                        balance = 2000.toBigDecimal(),
                        pendingBalance = BigDecimal.ZERO,
                        authorizeWithdraw = true
                    )
                ),
            )
        )
        val transaction = generateTransaction().copy(
            totalWithdrawalAmount = 3000.toBigDecimal(),
            merchantCategory = MerchantCategory.FOOD
        )
        every {
            accountService.findAccountById(any())
        } returns accountMock
        every {
            accountService.withdraw(withdrawList = any())
        }.throws(Exception())

        val result = cardWithdrawUseCase.execute(transaction)

        assertEquals(TransactionCodesEnum.INTERNAL_ERROR, result.status)
    }

    @Test
    fun `returns INTERNAL_ERROR without fallback when throws exception`() {
        val accountService = mockkClass(FakeAccountServiceImp::class)
        val statementHistoryService = FakeStatementHistoryServiceImp()
        val cardWithdrawUseCase = CardWithdrawUseCase(accountService, statementHistoryService, withdrawEngineServiceImp)
        val accountMock = Account(
            id = 1,
            status = "",
            availableAccountsAmount = hashMapOf(
                Pair("food", generateAccountAmount(
                    balance = 1000.toBigDecimal(),
                    pendingBalance = BigDecimal.ZERO
                )),
                Pair("meal", generateAccountAmount()),
                Pair("cash", generateAccountAmount()),
            )
        )
        val transaction = generateTransaction().copy(
            totalWithdrawalAmount = 500.toBigDecimal(),
            merchantCategory = MerchantCategory.FOOD
        )
        every {
            accountService.findAccountById(any())
        } returns accountMock
        every {
            accountService.withdraw(withdraw = any())
        }.throws(Exception())

        val result = cardWithdrawUseCase.execute(transaction)

        assertEquals(TransactionCodesEnum.INTERNAL_ERROR, result.status)
    }

    private fun generateAccountAmount(
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
                id
            } returns 1

            every {
                toPendingBalance(any())
            } returns pendingBalance
        }

        return accountAmount
    }

    private fun generateTransaction() = Transaction(
        100.toBigDecimal(),
        MerchantCategory.FOOD,
        "PADARIA DO ZE               SAO PAULO BR",
        "1",
        TransactionCodesEnum.REJECT
    )
}