package com.example.desafio

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories(basePackages = ["com.example.desafio"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
