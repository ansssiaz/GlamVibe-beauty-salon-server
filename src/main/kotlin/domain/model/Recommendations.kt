package com.glamvibe.domain.model

data class ServiceRelation(
    val serviceId1: Int = 0,
    val serviceId2: Int = 0,
    val cosineSimilarity: Double = 0.0,
)

data class FormServiceRelation(
    val formId: Int = 0,
    val serviceId: Int = 0,
    val cosineSimilarity: Double = 0.0,
)