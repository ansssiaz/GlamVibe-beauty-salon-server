package com.glamvibe.data.repository

import com.glamvibe.data.model.db.*
import com.glamvibe.data.model.response.CurrentAppointment
import com.glamvibe.data.model.response.MasterWithAppointments
import com.glamvibe.domain.model.*
import com.glamvibe.domain.repository.MasterRepository
import com.glamvibe.plugins.DatabaseConfiguration.suspendTransaction
import kotlinx.datetime.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDate

class MasterRepositoryImpl : MasterRepository {
    override suspend fun getAllMasters(): List<Master> = suspendTransaction {
        MasterEntity
            .all()
            .map(::masterEntityToMaster)
    }

    override suspend fun getMasterInformation(id: Int): Master? = suspendTransaction {
        MasterEntity
            .find(MastersTable.id eq id)
            .limit(1)
            .map { masterEntityToMaster(it) }
            .firstOrNull()
    }

    override suspend fun getMastersWithCurrentAppointments(): List<MasterWithAppointments> =
        suspendTransaction {
            val currentDate = LocalDate.now()

            val masters = MasterEntity.all()

            masters.map { master ->
                val appointments = AppointmentsTable
                    .select(AppointmentsTable.columns)
                    .where {
                        (AppointmentsTable.masterId eq master.id) and
                                ((AppointmentsTable.status eq Status.WAITING.getStringByStatus()) or
                                        (AppointmentsTable.status eq Status.IN_PROCESSING.getStringByStatus())) and
                                (AppointmentsTable.appointmentDate greater currentDate)
                    }
                    .map {
                        CurrentAppointment(
                            it[AppointmentsTable.appointmentDate].toKotlinLocalDate(),
                            it[AppointmentsTable.appointmentTime].toKotlinLocalTime(),
                            it[AppointmentsTable.weekDay].getWeekDayByString()
                        )
                    }

                val schedule = SchedulesTable.select(SchedulesTable.columns)
                    .where { (SchedulesTable.masterId eq master.id) }
                    .map {
                        WorkingDay(
                            it[SchedulesTable.weekDay].getWeekDayByString(),
                            it[SchedulesTable.startTime].toKotlinLocalTime(),
                            it[SchedulesTable.endTime].toKotlinLocalTime()
                        )
                    }

                MasterWithAppointments(
                    masterId = master.id.value,
                    lastname = master.lastname,
                    name = master.name,
                    categories = master.categories.map { it.name },
                    patronymic = master.patronymic,
                    schedule = schedule,
                    currentAppointments = appointments
                )
            }
        }

    override suspend fun getMasterWithCurrentAppointments(masterId: Int): MasterWithAppointments =
        suspendTransaction {
            val currentDate = LocalDate.now()

            val master = MasterEntity.findById(masterId)
                ?: throw IllegalArgumentException("Master with id '${masterId}' not found")

            val appointments = AppointmentsTable
                .select(AppointmentsTable.columns)
                .where {
                    (AppointmentsTable.masterId eq master.id) and
                            ((AppointmentsTable.status eq Status.WAITING.getStringByStatus()) or
                                    (AppointmentsTable.status eq Status.IN_PROCESSING.getStringByStatus())) and
                            (AppointmentsTable.appointmentDate greater currentDate)
                }
                .map {
                    CurrentAppointment(
                        it[AppointmentsTable.appointmentDate].toKotlinLocalDate(),
                        it[AppointmentsTable.appointmentTime].toKotlinLocalTime(),
                        it[AppointmentsTable.weekDay].getWeekDayByString()
                    )
                }

            val schedule = SchedulesTable.select(SchedulesTable.columns)
                .where { (SchedulesTable.masterId eq master.id) }
                .map {
                    WorkingDay(
                        it[SchedulesTable.weekDay].getWeekDayByString(),
                        it[SchedulesTable.startTime].toKotlinLocalTime(),
                        it[SchedulesTable.endTime].toKotlinLocalTime()
                    )
                }

            MasterWithAppointments(
                masterId = master.id.value,
                lastname = master.lastname,
                name = master.name,
                categories = master.categories.map { it.name },
                patronymic = master.patronymic,
                schedule = schedule,
                currentAppointments = appointments
            )
        }

    override suspend fun addMaster(master: Master): Master = suspendTransaction {
        val masterEntity = MasterEntity.new {
            lastname = master.lastname
            name = master.name
            patronymic = master.patronymic
            birthDate = master.birthDate.toJavaLocalDate()
            photoUrl = master.photoUrl
            phone = master.phone
            email = master.email
            specialty = master.specialty
            dateOfEmployment = master.dateOfEmployment.toJavaLocalDate()
            workExperience = master.workExperience
        }

        val categoryIds = CategoriesTable
            .select(CategoriesTable.columns)
            .where { CategoriesTable.name inList master.categories }
            .map { it[CategoriesTable.id] }

        for (masterCategoryId in categoryIds) {
            val existingCategory = CategoryEntity.findById(masterCategoryId)
                ?: throw IllegalArgumentException("Category with id '${masterCategoryId}' not found")

            MastersCategoryTable.insert {
                it[masterId] = masterEntity.id
                it[categoryId] = existingCategory.id
            }
        }

        for (workingDay in master.schedule) {
            SchedulesTable.insert {
                it[masterId] = masterEntity.id
                it[weekDay] = workingDay.weekDay.getStringByWeekDay()
                it[startTime] = workingDay.startTime.toJavaLocalTime()
                it[endTime] = workingDay.endTime.toJavaLocalTime()
            }
        }
        masterEntityToMaster(masterEntity)
    }

    override suspend fun updateMaster(id: Int, master: Master): Master = suspendTransaction {
        val masterEntity = MasterEntity.findByIdAndUpdate(id) {
            it.lastname = master.lastname
            it.name = master.name
            it.patronymic = master.patronymic
            it.birthDate = master.birthDate.toJavaLocalDate()
            it.photoUrl = master.photoUrl
            it.phone = master.phone
            it.email = master.email
            it.specialty = master.specialty
            it.dateOfEmployment = master.dateOfEmployment.toJavaLocalDate()
            it.workExperience = master.workExperience
        } ?: throw IllegalArgumentException("Master is not found")

        val currentCategories = MastersCategoryTable
            .select(MastersCategoryTable.columns)
            .where { MastersCategoryTable.masterId eq id }
            .map { it[MastersCategoryTable.categoryId] }

        val newCategoryIds = CategoriesTable
            .select(CategoriesTable.columns)
            .where { CategoriesTable.name inList master.categories }
            .map { it[CategoriesTable.id] }

        val categoriesToRemove = currentCategories.filter { it !in newCategoryIds }

        for (categoryIdToRemove in categoriesToRemove) {
            MastersCategoryTable.deleteWhere {
                (masterId eq id) and (categoryId eq categoryIdToRemove)
            }
        }

        for (masterCategoryId in newCategoryIds) {
            val existingCategory = currentCategories.firstOrNull { it == masterCategoryId }

            if (existingCategory == null) {
                MastersCategoryTable.insert {
                    it[masterId] = masterEntity.id
                    it[categoryId] = masterCategoryId
                }
            }
        }

        val currentSchedule = SchedulesTable
            .select(SchedulesTable.columns)
            .where { SchedulesTable.masterId eq id }
            .map {
                WorkingDay(
                    it[SchedulesTable.weekDay].getWeekDayByString(),
                    it[SchedulesTable.startTime].toKotlinLocalTime(),
                    it[SchedulesTable.endTime].toKotlinLocalTime()
                )
            }

        val newSchedule = master.schedule.map { it.weekDay }

        val workingDaysToRemove = currentSchedule.filter { it.weekDay !in newSchedule }

        for (workingDayToRemove in workingDaysToRemove) {
            SchedulesTable.deleteWhere {
                (masterId eq id) and (weekDay eq workingDayToRemove.weekDay.getStringByWeekDay())
            }
        }

        for (workingDay in master.schedule) {
            val existingWorkingDay =
                currentSchedule.firstOrNull { workingDay in currentSchedule }

            if (existingWorkingDay != null) {
                SchedulesTable.update({
                    (SchedulesTable.masterId eq id) and (SchedulesTable.weekDay eq workingDay.weekDay.getStringByWeekDay())
                })
                {
                    it[startTime] = workingDay.startTime.toJavaLocalTime()
                    it[endTime] = workingDay.endTime.toJavaLocalTime()
                }

            } else {
                SchedulesTable.insert {
                    it[masterId] = id
                    it[weekDay] = workingDay.weekDay.getStringByWeekDay()
                    it[startTime] = workingDay.startTime.toJavaLocalTime()
                    it[endTime] = workingDay.endTime.toJavaLocalTime()
                }
            }
        }
        masterEntityToMaster(masterEntity)
    }

    override suspend fun deleteMaster(id: Int): Boolean = suspendTransaction {
        MastersTable.deleteWhere { MastersTable.id eq id } > 0
    }
}