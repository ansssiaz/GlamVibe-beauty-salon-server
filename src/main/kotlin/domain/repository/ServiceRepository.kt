package com.glamvibe.domain.repository

import com.glamvibe.domain.model.Service

interface ServiceRepository {
    suspend fun getAllServices(): List<Service>
    suspend fun getAllServicesToClient(clientId: Int): List<Service>
    suspend fun getAllCategoryServicesToClient(clientId: Int, categoryId: Int): List<Service>
    suspend fun getServiceToClient(clientId: Int): Service?
    suspend fun updateService(serviceId: Int, service: Service): Service
    suspend fun addService(service: Service): Service
    suspend fun deleteService(serviceId: Int)
}