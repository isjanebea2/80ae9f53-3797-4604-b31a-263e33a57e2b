package com.example.desafio.adapters.rest

import com.example.desafio.adapters.rest.dto.TransactionRequest
import com.example.desafio.adapters.rest.dto.TransactionResponse
import com.example.desafio.application.CardWithdrawUseCase
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("api/v1/transactions", produces = ["application/json"])
class TransactionController(
    private val cardWithdrawUseCase: CardWithdrawUseCase
) {
    @PostMapping("authorize")
    @ResponseStatus(HttpStatus.OK)
    fun authorizeTransaction(@RequestBody request: TransactionRequest): TransactionResponse {

        val withdrawResult = cardWithdrawUseCase.execute(request.toDomain())

        return TransactionResponse(withdrawResult.status.externalCodes)
    }

}