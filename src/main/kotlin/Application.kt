package com.glamvibe

import com.glamvibe.plugins.DatabaseConfiguration.configureDatabases
import com.glamvibe.data.repository.PostgresUserRepository
import com.glamvibe.plugins.configureRouting
import com.glamvibe.plugins.configureSerialization
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val repository = PostgresUserRepository()
    configureSerialization()
    //configureSecurity()
    configureDatabases()
    configureRouting(repository)
}
