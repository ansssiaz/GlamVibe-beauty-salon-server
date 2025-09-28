package com.glamvibe.domain.repository

import com.glamvibe.data.model.request.NewService
import com.glamvibe.data.model.response.CategoryResponse
import com.glamvibe.data.model.response.ClientService
import com.glamvibe.domain.model.Service

interface ServiceRepository {
    suspend fun getServicesCategories(): List<String>
    suspend fun getAllServices(): List<Service>
    suspend fun getAllServicesToClient(clientId: Int): List<ClientService>
    suspend fun getService(serviceId: Int): Service?
    suspend fun getServiceToClient(clientId: Int, serviceId: Int): ClientService?
    suspend fun addService(service: Service): Service
    suspend fun updateService(serviceId: Int, updateService: Service): Service
    suspend fun deleteService(serviceId: Int): Boolean
}