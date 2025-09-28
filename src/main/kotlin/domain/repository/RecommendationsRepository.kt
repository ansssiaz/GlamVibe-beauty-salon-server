package com.glamvibe.domain.repository

import com.glamvibe.domain.model.FormServiceRelation
import com.glamvibe.domain.model.Service
import com.glamvibe.domain.model.ServiceRelation

interface RecommendationsRepository {
    suspend fun saveServiceRelations(serviceRelations: List<ServiceRelation>)
    suspend fun saveFormServiceRelations(formServiceRelations: List<FormServiceRelation>)
    suspend fun getRecommendations(clientId: Int): List<Service>
}