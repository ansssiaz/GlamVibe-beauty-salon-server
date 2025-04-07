package com.glamvibe.data.model

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object MastersCategoryTable : Table("master_category") {
    val masterId = reference(
        "master_id",
        MastersTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val categoryId = reference(
        "category_id",
        CategoriesTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
}