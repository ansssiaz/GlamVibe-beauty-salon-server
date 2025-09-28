package com.glamvibe.data.model.response

import com.glamvibe.domain.model.WeekDay
import com.glamvibe.domain.model.WorkingDay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class MasterWithAppointments(
    val masterId: Int,
    val lastname: String,
    val name: String,
    val categories: List<String>,
    val patronymic: String?,
    val schedule: List<WorkingDay> = emptyList(),
    val currentAppointments: List<CurrentAppointment> = emptyList()
)

@Serializable
data class CurrentAppointment(
    @Contextual val date: LocalDate,
    @Contextual val time: LocalTime,
    val weekDay: WeekDay,
)