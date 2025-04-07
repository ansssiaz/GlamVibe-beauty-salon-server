package com.glamvibe.data.model

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object FormsServicesRelationsTable : Table("forms_services_relations") {
    val formId = reference(
        "form_id",
        FormsTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val serviceId = reference(
        "service_id",
        ServicesTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val cosineSimilarity = decimal("cosine_similarity", 5, 4)
}