package com.glamvibe.domain.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime

enum class Weekday{
    SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
}

@Serializable
data class WorkingDay(
    val weekDay: Weekday = Weekday.MONDAY,
    @Contextual val startTime: LocalTime = LocalTime.now(),
    @Contextual val endTime: LocalTime = LocalTime.now(),
)

@Serializable
data class Master(
    val id: Long = 0L,
    val lastname: String = "",
    val name: String = "",
    val patronymic: String = "",
    @Contextual val birthDate: LocalDate = LocalDate.now(),
    val photoUrl: String = "",
    val phone: String = "",
    val email: String = "",
    val specialty: String = "",
    val categoryIds: List<Long> = emptyList(),
    val schedule: List<WorkingDay?> = emptyList<WorkingDay>(),
    @Contextual val dateOfEmployment: LocalDate = LocalDate.now(),
    val workExperience: Int = 0,
)


