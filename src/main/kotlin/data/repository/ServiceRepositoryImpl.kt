package com.glamvibe.data.repository

import com.glamvibe.data.model.db.*
import com.glamvibe.data.model.response.ClientService
import com.glamvibe.domain.model.Service
import com.glamvibe.domain.repository.ServiceRepository
import com.glamvibe.plugins.DatabaseConfiguration.suspendTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

class ServiceRepositoryImpl : ServiceRepository {
    override suspend fun getServicesCategories(): List<String> = suspendTransaction {
        CategoryEntity
            .all()
            .map {
                it.name
            }
    }

    override suspend fun getAllServices(): List<Service> = suspendTransaction {
        ServiceEntity
            .all()
            .map(::serviceEntityToService)
    }

    override suspend fun getAllServicesToClient(clientId: Int): List<ClientService> = suspendTransaction {
        val client = UserEntity.findById(clientId) ?: throw IllegalArgumentException("Client is not found")

        ServiceEntity.all().map { serviceEntity ->
            val isFavouriteService = serviceEntity.favoringClients.contains(client)
            val service = serviceEntityToService(serviceEntity)

            ClientService(
                id = service.id,
                name = service.name,
                category = service.category,
                description = service.description,
                imageUrl = service.imageUrl,
                duration = service.duration,
                price = service.price,
                priceWithPromotion = service.priceWithPromotion,
                discountPercentage = service.discountPercentage,
                isFavourite = isFavouriteService
            )
        }
    }

    override suspend fun getService(serviceId: Int): Service? = suspendTransaction {
        ServiceEntity
            .find { (ServicesTable.id eq serviceId) }
            .limit(1)
            .map(::serviceEntityToService)
            .firstOrNull()
    }

    override suspend fun getServiceToClient(clientId: Int, serviceId: Int): ClientService = suspendTransaction {
        val client = UserEntity.findById(clientId) ?: throw IllegalArgumentException("Client is not found")
        val serviceEntity = ServiceEntity
            .find { (ServicesTable.id eq serviceId) }
            .limit(1)
            .firstOrNull() ?: throw IllegalArgumentException("Service is not found")

        val isFavouriteService = serviceEntity.favoringClients.contains(client)
        val service = serviceEntityToService(serviceEntity)
        ClientService(
            id = service.id,
            name = service.name,
            category = service.category,
            description = service.description,
            imageUrl = service.imageUrl,
            duration = service.duration,
            price = service.price,
            priceWithPromotion = service.priceWithPromotion,
            discountPercentage = service.discountPercentage,
            isFavourite = isFavouriteService
        )
    }

    override suspend fun updateService(serviceId: Int, updateService: Service): Service = suspendTransaction {
        val serviceCategory =
            CategoryEntity.find { CategoriesTable.name eq updateService.category }
                .limit(1)
                .firstOrNull()

        if (serviceCategory != null) {
            val updatedService = ServiceEntity.findByIdAndUpdate(serviceId) {
                it.serviceName = updateService.name
                it.category = serviceCategory
                it.description = updateService.description
                it.imageUrl = updateService.imageUrl
                it.duration = updateService.duration
                it.price = updateService.price
            } ?: throw IllegalArgumentException("Service is not found")
            serviceEntityToService(updatedService)
        } else throw IllegalArgumentException("Category is not found")
    }

    override suspend fun addService(service: Service): Service = suspendTransaction {
        val serviceCategory =
            CategoryEntity.find { CategoriesTable.name eq service.category }
                .limit(1)
                .firstOrNull()

        if (serviceCategory != null) {
            val newService = ServiceEntity.new {
                serviceName = service.name
                category = serviceCategory
                description = service.description
                imageUrl = service.imageUrl
                duration = service.duration
                price = service.price
            }
            serviceEntityToService(newService)
        } else throw IllegalArgumentException("Category is not found")
    }

    override suspend fun deleteService(serviceId: Int): Boolean = suspendTransaction {
        ServicesTable.deleteWhere { ServicesTable.id eq serviceId } > 0
    }
}