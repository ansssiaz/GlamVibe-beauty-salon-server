package com.glamvibe.data.repository

import com.glamvibe.data.model.db.*
import com.glamvibe.data.model.request.NewAppointment
import com.glamvibe.data.model.request.NewAppointmentDateTime
import com.glamvibe.data.model.request.CommentForCancellation
import com.glamvibe.domain.model.*
import com.glamvibe.domain.repository.AppointmentRepository
import com.glamvibe.plugins.DatabaseConfiguration.suspendTransaction
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalTime
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import java.time.LocalDate

class AppointmentRepositoryImpl : AppointmentRepository {
    override suspend fun getAllAppointmentsForDay(date: LocalDate): List<Appointment> =
        suspendTransaction {
            AppointmentEntity.find { AppointmentsTable.appointmentDate eq date }
                .map(::appointmentEntityToAppointment)
        }

    override suspend fun getAppointmentsOfClient(clientId: Int): List<Appointment> = suspendTransaction {
        AppointmentEntity.find { AppointmentsTable.clientId eq clientId }
            .map(::appointmentEntityToAppointment)
    }

    override suspend fun getAppointmentById(id: Int): Appointment? = suspendTransaction {
        AppointmentEntity.find { AppointmentsTable.id eq id }
            .limit(1)
            .map(::appointmentEntityToAppointment)
            .firstOrNull()
    }

    override suspend fun makeAppointment(newAppointment: NewAppointment): Appointment = suspendTransaction {
        val clientEntity = UserEntity.findById(newAppointment.clientId)
            ?: throw IllegalArgumentException("Client with id '${newAppointment.clientId}' not found")

        val masterEntity = MasterEntity.findById(newAppointment.masterId)
            ?: throw IllegalArgumentException("Master with id '${newAppointment.masterId}' not found")

        val serviceEntity =
            ServiceEntity.findById(newAppointment.serviceId)
                ?: throw IllegalArgumentException("Service with id '${newAppointment.serviceId}' not found")

        val appointmentService = serviceEntityToService(serviceEntity)

        val promotionEntity =
            if (appointmentService.promotionId != null) PromotionEntity.findById(appointmentService.promotionId) else null

        val appointmentEntity = AppointmentEntity.new {
            client = clientEntity
            master = masterEntity
            service = serviceEntity
            promotion = promotionEntity
            appointmentDate = newAppointment.date.toJavaLocalDate()
            appointmentTime = newAppointment.time.toJavaLocalTime()
            weekDay = newAppointment.weekDay.getStringByWeekDay()
            status = Status.IN_PROCESSING.getStringByStatus()
        }
        appointmentEntityToAppointment(appointmentEntity)
    }

    override suspend fun confirmAppointment(id: Int): Appointment = suspendTransaction {
        val appointmentEntity = AppointmentEntity.findByIdAndUpdate(id) {
            it.status = Status.WAITING.getStringByStatus()
        } ?: throw IllegalArgumentException("Appointment is not found")

        appointmentEntityToAppointment(appointmentEntity)
    }

    override suspend fun markCompletion(id: Int): Appointment = suspendTransaction {
        val appointmentEntity = AppointmentEntity.findByIdAndUpdate(id) {
            it.status = Status.DONE.getStringByStatus()
        } ?: throw IllegalArgumentException("Appointment is not found")

        appointmentEntityToAppointment(appointmentEntity)
    }

    override suspend fun rescheduleAppointment(id: Int, newAppointment: NewAppointmentDateTime): Appointment =
        suspendTransaction {
            val appointmentEntity = AppointmentEntity.findByIdAndUpdate(id) {
                it.comment = "Перенос записи с ${it.appointmentDate} ${it.appointmentTime}"
                it.weekDay = newAppointment.weekDay.getStringByWeekDay()
                it.appointmentDate = newAppointment.date.toJavaLocalDate()
                it.appointmentTime = newAppointment.time.toJavaLocalTime()
            } ?: throw IllegalArgumentException("Appointment is not found")

            appointmentEntityToAppointment(appointmentEntity)
        }

    override suspend fun cancelAppointment(
        appointmentId: Int,
        commentForCancellation: CommentForCancellation?,
        role: Role
    ): Appointment =
        suspendTransaction {
            val appointmentEntity = AppointmentEntity.findByIdAndUpdate(appointmentId) {
                if (role == Role.CLIENT) {
                    it.status = Status.CANCELLATION_BY_THE_CLIENT.getStringByStatus()
                } else if (role == Role.ADMINISTRATOR) {
                    it.status = Status.CANCELLATION_BY_THE_ADMINISTRATOR.getStringByStatus()
                }
                if (commentForCancellation != null) {
                    it.comment = commentForCancellation.text
                }
            } ?: throw IllegalArgumentException("Appointment is not found")

            appointmentEntityToAppointment(appointmentEntity)
        }

    override suspend fun deleteAppointment(id: Int): Boolean = suspendTransaction {
        AppointmentsTable.deleteWhere { AppointmentsTable.id eq id } > 0
    }
}