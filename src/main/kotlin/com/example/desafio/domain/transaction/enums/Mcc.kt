package com.example.desafio.domain.transaction.enums

val foodMccCodesCommons = arrayOf("5411", "5412", "5462", "5422", "5441", "5499", "5414", "5691")
val mealMccCodesCommons = arrayOf("5811", "5812", "4111", "4131")

enum class Mcc(val codes: Array<String>, val internalName: String) {
    FOOD(foodMccCodesCommons, "food"),
    MEAL(mealMccCodesCommons, "meal"),
    CASH(arrayOf(), "cash");

    companion object {
        fun fromString(name: String): Mcc {
            return when (name) {
                in FOOD.codes -> FOOD
                in MEAL.codes -> MEAL
                else -> CASH
            }
        }
    }
}