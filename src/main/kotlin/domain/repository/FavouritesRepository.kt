package com.glamvibe.domain.repository

import com.glamvibe.data.model.response.ClientService

interface FavouritesRepository {
    suspend fun getFavourites(clientId: Int): List<ClientService>
    suspend fun addToFavourites(favoringClientId: Int, favoredServiceId: Int): Boolean
    suspend fun deleteFromFavourites(favoringClientId: Int, favoredServiceId: Int): Boolean
}