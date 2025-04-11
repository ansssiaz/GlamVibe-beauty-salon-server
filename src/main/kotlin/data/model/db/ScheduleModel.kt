package com.glamvibe.data.model.db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.time

object SchedulesTable : IntIdTable("schedules") {
    val masterId = reference(
        "master_id",
        MastersTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val weekDay = varchar("week_day", 15)
    val startTime = time("start_time")
    val endTime = time("end_time")
}

class ScheduleEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ScheduleEntity>(SchedulesTable)

    var master by MasterEntity referencedOn SchedulesTable.masterId
    var weekDay by SchedulesTable.weekDay
    var startTime by SchedulesTable.startTime
    var endTime by SchedulesTable.endTime
}