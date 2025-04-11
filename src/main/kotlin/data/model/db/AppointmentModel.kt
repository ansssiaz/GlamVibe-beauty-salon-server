package com.glamvibe.data.model.db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.time

object AppointmentsTable : IntIdTable("appointments") {
    val clientId = reference(
        "client_id",
        UsersTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val serviceId = reference(
        "service_id",
        ServicesTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val masterId = reference(
        "master_id",
        MastersTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val promotionId = optReference(
        "promotion_id",
        PromotionsTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val appointmentDate = date("appointment_date")
    val weekDay = varchar("week_day", 15)
    val appointmentTime = time("appointment_time")
    val status = varchar("status", 50)
    val comment = varchar("comment", 150).nullable()
}

class AppointmentEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AppointmentEntity>(AppointmentsTable)

    var client by UserEntity referencedOn AppointmentsTable.clientId
    var service by ServiceEntity referencedOn AppointmentsTable.serviceId
    var master by MasterEntity referencedOn AppointmentsTable.masterId
    var promotion by PromotionEntity optionalReferencedOn AppointmentsTable.promotionId
    var appointmentDate by AppointmentsTable.appointmentDate
    var weekDay by AppointmentsTable.weekDay
    var appointmentTime by AppointmentsTable.appointmentTime
    var status by AppointmentsTable.status
    var comment by AppointmentsTable.comment
}