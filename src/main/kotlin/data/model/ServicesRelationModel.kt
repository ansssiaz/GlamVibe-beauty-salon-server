package com.glamvibe.data.model

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object ServicesRelationsTable : Table("services_relations") {
    val serviceId1 = reference(
        "service_id1",
        ServicesTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val serviceId2 = reference(
        "service_id2",
        ServicesTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val cosineSimilarity = decimal("cosine_similarity", 5, 4)
}