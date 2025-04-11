package com.glamvibe.domain.model

import com.glamvibe.utils.Constants
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

fun String.getRoleByString(): Role {
    return when (this) {
        Constants.Role.ADMINISTRATOR -> Role.ADMINISTRATOR
        else -> Role.CLIENT
    }
}

fun Role.getStringByRole(): String{
    return when (this) {
        Role.ADMINISTRATOR -> Constants.Role.ADMINISTRATOR
        else -> Constants.Role.CLIENT
    }
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
    val refreshTokenExpirationTime: Long? = null,
    val role: Role = Role.CLIENT,
    val favouritesIds: List<Int?> = emptyList<Int>(),
    val form: Form? = null,
)