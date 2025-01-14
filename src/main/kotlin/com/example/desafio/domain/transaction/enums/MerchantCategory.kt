package com.example.desafio.domain.transaction.enums

val foodMccCodesCommons = arrayOf("5411", "5412", "5462", "5422", "5441", "5499", "5414", "5691")
val mealMccCodesCommons = arrayOf("5811", "5812", "4111", "4131")

enum class MerchantCategory(val codes: Array<String>, val internalName: String) {
    FOOD(foodMccCodesCommons, "food"),
    MEAL(mealMccCodesCommons, "meal"),
    CASH(arrayOf(), "cash");

    fun allowed(mcc: MerchantCategory): Boolean {
        return when {
            mcc == this -> true
            this == FOOD && mcc == MEAL -> true
            this == FOOD && mcc == CASH -> true
            this == MEAL && mcc == CASH -> true
            else -> false
        }
    }

    companion object {
        fun fromString(name: String): MerchantCategory {
            return when (name) {
                in FOOD.codes -> FOOD
                in MEAL.codes -> MEAL
                else -> CASH
            }
        }
    }
}