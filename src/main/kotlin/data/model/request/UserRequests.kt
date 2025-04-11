package com.glamvibe.data.model.request

import com.glamvibe.domain.model.Role
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class LogInUser(
    val login: String,
    val password: String,
)

@Serializable
data class RegisterUser(
    val lastname: String,
    val name: String,
    val patronymic: String?,
    @Contextual val birthDate: LocalDate,
    val phone: String,
    val email: String,
    val login: String,
    val password: String,
    val role: Role,
    val form: String?,
)

@Serializable
data class UpdateUser(
    val lastname: String,
    val name: String,
    val patronymic: String?,
    @Contextual val birthDate: LocalDate,
    val phone: String,
    val email: String,
    val login: String,
    val password: String,
)