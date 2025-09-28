package com.glamvibe.domain.model

import com.glamvibe.utils.Constants
import kotlinx.datetime.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

enum class Status {
    IN_PROCESSING, WAITING, DONE, CANCELLATION_BY_THE_CLIENT, CANCELLATION_BY_THE_ADMINISTRATOR
}

fun String.getStatusByString(): Status {
    return when (this) {
        Constants.Status.IN_PROCESSING -> Status.IN_PROCESSING
        Constants.Status.WAITING -> Status.WAITING
        Constants.Status.DONE -> Status.DONE
        Constants.Status.CANCELLATION_BY_THE_CLIENT -> Status.CANCELLATION_BY_THE_CLIENT
        Constants.Status.CANCELLATION_BY_THE_ADMINISTRATOR -> Status.CANCELLATION_BY_THE_ADMINISTRATOR

        else -> Status.IN_PROCESSING
    }
}


fun Status.getStringByStatus(): String {
    return when (this) {
        Status.IN_PROCESSING -> Constants.Status.IN_PROCESSING
        Status.WAITING -> Constants.Status.WAITING
        Status.DONE -> Constants.Status.DONE
        Status.CANCELLATION_BY_THE_CLIENT -> Constants.Status.CANCELLATION_BY_THE_CLIENT
        Status.CANCELLATION_BY_THE_ADMINISTRATOR -> Constants.Status.CANCELLATION_BY_THE_ADMINISTRATOR
    }
}

@Serializable
data class Appointment(
    val id: Int = 0,
    val client: ClientData,
    val master: MasterData,
    val service: String = "",
    val promotionId: Int? = 0,
    @Contextual val date: LocalDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date,
    @Contextual val time: LocalTime = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).time,
    val weekDay: WeekDay = WeekDay.MONDAY,
    val status: Status = Status.IN_PROCESSING,
    val comment: String? = null,
)

@Serializable
data class ClientData(
    val id: Int = 0,
    val lastname: String = "",
    val name: String = "",
    val patronymic: String? = null,
)

@Serializable
data class MasterData(
    val id: Int = 0,
    val lastname: String = "",
    val name: String = "",
    val patronymic: String? = null,
)
