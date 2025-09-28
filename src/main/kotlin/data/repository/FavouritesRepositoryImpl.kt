package com.glamvibe.data.repository

import com.glamvibe.data.model.db.*
import com.glamvibe.data.model.response.ClientService
import com.glamvibe.domain.repository.FavouritesRepository
import com.glamvibe.plugins.DatabaseConfiguration.suspendTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

class FavouritesRepositoryImpl : FavouritesRepository {
    override suspend fun getFavourites(clientId: Int): List<ClientService> = suspendTransaction {
        val client = UserEntity.findById(clientId) ?: throw IllegalArgumentException("Client is not found")

        val favouriteServiceIds = FavouritesTable
            .select(FavouritesTable.columns)
            .where { FavouritesTable.clientId eq client.id }
            .map { it[FavouritesTable.serviceId] }

        val favouriteServices = ServiceEntity.find { ServicesTable.id inList favouriteServiceIds }

        favouriteServices
            .map { serviceEntity ->
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
                    isFavourite = true
                )
            }
    }

    override suspend fun addToFavourites(favoringClientId: Int, favoredServiceId: Int): Boolean =
        suspendTransaction {
            val service = ServiceEntity.findById(favoredServiceId)
            val client = UserEntity.findById(favoringClientId)

            if (service != null && client != null) {
                FavouritesTable.insert {
                    it[clientId] = client.id
                    it[serviceId] = service.id
                }
                true
            } else {
                false
            }
        }

    override suspend fun deleteFromFavourites(favoringClientId: Int, favoredServiceId: Int): Boolean =
        suspendTransaction {
            val service =
                ServiceEntity.findById(favoredServiceId) ?: throw IllegalArgumentException("Service is not found")
            val client = UserEntity.findById(favoringClientId) ?: throw IllegalArgumentException("Client is not found")

            FavouritesTable.deleteWhere {
                (clientId eq client.id) and (serviceId eq service.id)
            } > 0
        }
}