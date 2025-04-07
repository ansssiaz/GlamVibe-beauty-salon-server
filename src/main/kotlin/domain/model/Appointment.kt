package com.glamvibe.domain.model

import io.ktor.util.date.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDate

enum class Status {
    InProcessing, Waiting, Done, CancellationByTheClient, CancellationByTheAdministrator
}

@Serializable
data class Appointment(
    val id: Long = 0L,
    val clientId: Long = 0L,
    val serviceId: Long = 0L,
    val masterId: Long = 0L,
    val promotionId: Long? = 0L,
    @Contextual val date: LocalDate = LocalDate.now(),
    val weekDay: WeekDay = WeekDay.MONDAY,
    val status: Status = Status.InProcessing,
    val comment: String? = null,
)
