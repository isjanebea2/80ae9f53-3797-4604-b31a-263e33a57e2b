package com.example.desafio.domain.account

import com.example.desafio.adapters.jpa_database.entity.AccountTypeEntity
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AccountAmountTest {

@Test
 fun `should authorized amount of used total amount avalaible`() {
 val totalAmount = 100.toBigDecimal()
 val accountAmount = AccountAmount(
       1, totalAmount, mockk<AccountTypeEntity>(),
      )

     val value = accountAmount.toWithdraw(totalAmount)

    assertTrue(accountAmount.copy(value = accountAmount.value - value).authorizeWithdraw(totalAmount))
 }

 @Test
 fun `should returns 50,00 pendingValue when balance is 100,00 and requestAmount 50,00`() {
  val totalAmount = 100.toBigDecimal()
  val accountAmount = AccountAmount(
   1, 50.toBigDecimal(), mockk<AccountTypeEntity>(),
  )

  val result = accountAmount.toPendingBalance(totalAmount)

  assertEquals(50.toBigDecimal(), result)
 }

 @Test
 fun `should returns 0,00 pendingValue when balance is 100,00 and requestAmount 100,00`() {
  val totalAmount = 100.toBigDecimal()
  val accountAmount = AccountAmount(
   1, totalAmount, mockk<AccountTypeEntity>(),
  )

  val result = accountAmount.toPendingBalance(totalAmount)

  assertEquals(0.toBigDecimal(), result)
 }

 @Test
 fun `should returns 0,00 pendingValue when balance is 100,00 and requestAmount 50,00`() {
  val totalAmount = 100.toBigDecimal()
  val accountAmount = AccountAmount(
   1, totalAmount, mockk<AccountTypeEntity>(),
  )

  val result = accountAmount.toPendingBalance(50.toBigDecimal())

  assertEquals(0.toBigDecimal(), result)
 }


 @Test
 fun `should returns 50,00 when balance is 50,00 and requestAmount 100,00`() {
  val totalAmount = 100.toBigDecimal()
  val accountAmount = AccountAmount(
   1, 50.toBigDecimal(), mockk<AccountTypeEntity>(),
  )

  val result = accountAmount.toWithdraw(totalAmount)

  assertEquals((-50).toBigDecimal(), result)
 }

 @Test
 fun `should returns 50,00 when balance is 100,00 and requestAmount 50,00`() {
  val totalAmount = 50.toBigDecimal()
  val accountAmount = AccountAmount(
   1, 100.toBigDecimal(), mockk<AccountTypeEntity>(),
  )

  val result = accountAmount.toWithdraw(totalAmount)

  assertEquals(50.toBigDecimal(), result)
 }

 @Test
 fun `should returns 10,00 when balance is 10,00 and requestAmount 50,00`() {
  val totalAmount = 50.toBigDecimal()
  val accountAmount = AccountAmount(
   1, (-40).toBigDecimal(), mockk<AccountTypeEntity>(),
  )

  val result = accountAmount.toWithdraw(totalAmount)

  assertEquals(10.toBigDecimal(), result)
 }

}