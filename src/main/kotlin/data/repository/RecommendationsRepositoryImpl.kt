package com.glamvibe.data.repository

import com.glamvibe.data.model.db.*
import com.glamvibe.domain.model.FormServiceRelation
import com.glamvibe.domain.model.Service
import com.glamvibe.domain.model.ServiceRelation
import com.glamvibe.domain.repository.RecommendationsRepository
import com.glamvibe.plugins.DatabaseConfiguration.suspendTransaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object RandomOrder : Expression<Double>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append("RANDOM()")
    }
}

class RecommendationsRepositoryImpl : RecommendationsRepository {
    override suspend fun saveServiceRelations(serviceRelations: List<ServiceRelation>) = suspendTransaction {
        ServicesRelationsTable.deleteAll()

        serviceRelations.forEach { relation ->
            ServicesRelationsTable.insert {
                it[serviceId1] = relation.serviceId1
                it[serviceId2] = relation.serviceId2
                it[cosineSimilarity] = relation.cosineSimilarity.toBigDecimal()
            }
        }
    }

    override suspend fun saveFormServiceRelations(formServiceRelations: List<FormServiceRelation>) =
        suspendTransaction {
            formServiceRelations.forEach { relation ->
                FormsServicesRelationsTable.insert {
                    it[formId] = relation.formId
                    it[serviceId] = relation.serviceId
                    it[cosineSimilarity] = relation.cosineSimilarity.toBigDecimal()
                }
            }
        }

    override suspend fun getRecommendations(clientId: Int): List<Service> = suspendTransaction {
        val appointments = AppointmentEntity.find(AppointmentsTable.clientId eq clientId)
            .map { appointmentEntityToAppointment(it) }

        if (appointments.isNotEmpty()) {
            val lastAppointment = appointments.maxBy { it.date }

            val lastServiceId = ServiceEntity.find { ServicesTable.serviceName eq lastAppointment.service }
                .limit(1)
                .map { serviceEntityToService(it) }
                .first()
                .id

            ServicesRelationsTable
                .select(ServicesRelationsTable.columns)
                .where { ServicesRelationsTable.serviceId1 eq lastServiceId }
                .orderBy(ServicesRelationsTable.cosineSimilarity, SortOrder.DESC)
                .limit(5)
                .mapNotNull { row ->
                    ServiceEntity.findById(row[ServicesRelationsTable.serviceId2])
                        ?.let { serviceEntityToService(it) }
                }
        } else {
            val form = FormEntity.find(FormsTable.clientId eq clientId)
                .limit(1)
                .map { formEntityToForm(it) }
                .firstOrNull()

            if (form != null) {
                FormsServicesRelationsTable
                    .select(FormsServicesRelationsTable.columns)
                    .where { FormsServicesRelationsTable.formId eq form.id }
                    .orderBy(FormsServicesRelationsTable.cosineSimilarity, SortOrder.DESC)
                    .limit(5)
                    .mapNotNull { row ->
                        ServiceEntity.findById(row[FormsServicesRelationsTable.serviceId])
                            ?.let { serviceEntityToService(it) }
                    }
            } else {
                ServiceEntity.all()
                    .orderBy(RandomOrder to SortOrder.ASC)
                    .limit(5)
                    .map { serviceEntityToService(it) }
            }
        }
    }
}