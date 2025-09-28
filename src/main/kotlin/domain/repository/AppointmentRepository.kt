package com.glamvibe.domain.repository

import com.glamvibe.data.model.request.NewAppointment
import com.glamvibe.data.model.request.NewAppointmentDateTime
import com.glamvibe.data.model.request.CommentForCancellation
import com.glamvibe.domain.model.Appointment
import com.glamvibe.domain.model.Role
import java.time.LocalDate

interface AppointmentRepository {
    suspend fun getAllAppointmentsForDay(date: LocalDate): List<Appointment>
    suspend fun getAppointmentsOfClient(clientId: Int): List<Appointment>
    suspend fun getAppointmentById(id: Int): Appointment?
    suspend fun makeAppointment(newAppointment: NewAppointment): Appointment
    suspend fun confirmAppointment(id: Int): Appointment
    suspend fun markCompletion(id: Int): Appointment
    suspend fun rescheduleAppointment(id: Int, newAppointment: NewAppointmentDateTime): Appointment
    suspend fun cancelAppointment(appointmentId: Int, commentForCancellation: CommentForCancellation?, role: Role): Appointment
    suspend fun deleteAppointment(id: Int): Boolean
}