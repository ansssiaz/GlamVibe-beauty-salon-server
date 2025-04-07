package com.glamvibe.data.model

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object StatisticsServicesMastersTable : Table("statistics_services_masters") {
    val statisticsId = reference(
        "statistics_id",
        StatisticsTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val serviceId = optReference(
        "service_id",
        ServicesTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val masterId = optReference(
        "master_id",
        MastersTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val appointmentsCount = integer("appointments_count")
}