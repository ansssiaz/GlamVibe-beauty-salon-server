package com.glamvibe.data.model

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.date

object StatisticsTable : IntIdTable("statistics") {
    val creationDate = date("creation_date")
    val year = integer("year")
    val month = integer("month")
    val promotionId = optReference(
        "promotion_id",
        PromotionsTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val totalAppointmentsCount = integer("total_appointments_count")
    val newClientsAppointmentsCount = integer("new_clients_appointments_count")
    val returningClientsAppointmentsCount = integer("returning_clients_appointments_count")
    val cancellationsCount = integer("cancellations_count")
    val totalRevenue = integer("total_revenue")
    val averageTicket = decimal("average_ticket", 5, 0)
}

class StatisticsEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<StatisticsEntity>(StatisticsTable)

    var creationDate by StatisticsTable.creationDate
    var year by StatisticsTable.year
    var month by StatisticsTable.month
    var promotion by PromotionEntity optionalReferencedOn StatisticsTable.promotionId
    var totalAppointmentsCount by StatisticsTable.totalAppointmentsCount
    var newClientsAppointmentsCount by StatisticsTable.newClientsAppointmentsCount
    var returningClientsAppointmentsCount by StatisticsTable.returningClientsAppointmentsCount
    var cancellationsCount by StatisticsTable.cancellationsCount
    var totalRevenue by StatisticsTable.totalRevenue
    var averageTicket by StatisticsTable.averageTicket
    var popularServices by ServiceEntity via StatisticsServicesMastersTable
    var popularMasters by MasterEntity via StatisticsServicesMastersTable
}