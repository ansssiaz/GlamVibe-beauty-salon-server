package com.glamvibe.domain.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Serializable
data class Form(
    val id: Int = 0,
    val clientData: String = "",
)

enum class Role {
    CLIENT, ADMINISTRATOR
}

@Serializable
data class User(
    val id: Int = 0,
    val lastname: String = "",
    val name: String = "",
    val patronymic: String? = "",
    @Contextual val birthDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val phone: String = "",
    val email: String = "",
    val login: String = "",
    val passwordHash: String = "",
    val refreshToken: String? = null,
    val role: Role = Role.CLIENT,
    val favouritesIds: List<Int?> = emptyList<Int>(),
    val form: Form? = null,
)