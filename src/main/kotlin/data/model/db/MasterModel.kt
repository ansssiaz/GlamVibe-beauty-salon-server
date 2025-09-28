package com.glamvibe.data.model.db

import com.glamvibe.domain.model.Master
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

object MastersTable : IntIdTable("masters") {
    val lastname = varchar("lastname", 30)
    val name = varchar("name", 30)
    val patronymic = varchar("patronymic", 30).nullable()
    val birthDate = date("birth_date")
    val photoUrl = varchar("photo_url", 150)
    val phone = varchar("phone", 15)
    val email = varchar("email", 50)
    val specialty = varchar("specialty", 50)
    val dateOfEmployment = date("date_of_employment")
    val workExperience = integer("work_experience")
}

class MasterEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MasterEntity>(MastersTable)

    var lastname by MastersTable.lastname
    var name by MastersTable.name
    var patronymic by MastersTable.patronymic
    var birthDate by MastersTable.birthDate
    var photoUrl by MastersTable.photoUrl
    var phone by MastersTable.phone
    var email by MastersTable.email
    var specialty by MastersTable.specialty
    var dateOfEmployment by MastersTable.dateOfEmployment
    var workExperience by MastersTable.workExperience
    var categories by CategoryEntity via MastersCategoryTable
    val schedule by ScheduleEntity referrersOn SchedulesTable.masterId
    val appointments by AppointmentEntity referrersOn AppointmentsTable.masterId
}

fun masterEntityToMaster(masterEntity: MasterEntity) = Master(
    id = masterEntity.id.value,
    lastname = masterEntity.lastname,
    name = masterEntity.name,
    patronymic = masterEntity.patronymic,
    birthDate = masterEntity.birthDate.toKotlinLocalDate(),
    photoUrl = masterEntity.photoUrl,
    phone = masterEntity.phone,
    email = masterEntity.email,
    specialty = masterEntity.specialty,
    categories = masterEntity.categories.map { it.name },
    schedule = masterEntity.schedule.map { scheduleEntityToWorkingDay(it) },
    dateOfEmployment = masterEntity.dateOfEmployment.toKotlinLocalDate(),
    workExperience = masterEntity.workExperience
)