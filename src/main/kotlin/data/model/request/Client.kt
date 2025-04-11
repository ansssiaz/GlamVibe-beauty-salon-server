package com.glamvibe.data.model.request

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Client(
    val id: Int,
    val lastname: String,
    val name: String,
    val patronymic: String?,
    @Contextual val birthDate: LocalDate,
    val phone: String,
    val email: String,
)
