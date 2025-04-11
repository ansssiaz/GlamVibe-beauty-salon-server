package com.glamvibe.data.model.db

import com.glamvibe.domain.model.Category
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object CategoriesTable : IntIdTable("categories") {
    val name = varchar("name", 50)
}

class CategoryEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CategoryEntity>(CategoriesTable)

    var name by CategoriesTable.name
    val services by ServiceEntity referrersOn ServicesTable.categoryId
}

fun categoryEntityToCategory(categoryEntity: CategoryEntity) = Category(
    id = categoryEntity.id.value,
    name = categoryEntity.name,
    services = categoryEntity.services.map { serviceEntityToService(it) }
)