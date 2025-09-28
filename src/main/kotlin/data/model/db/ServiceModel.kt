package com.glamvibe.data.model.db

import com.glamvibe.domain.model.Service
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import java.time.LocalDate

object ServicesTable : IntIdTable("services") {
    val serviceName = varchar("service_name", 50)
    val categoryId = reference(
        "category_id",
        CategoriesTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val description = text("description")
    val imageUrl = varchar("image_url", 150)
    val duration = integer("duration")
    val price = integer("price")
}

class ServiceEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ServiceEntity>(ServicesTable)

    var serviceName by ServicesTable.serviceName
    var category by CategoryEntity referencedOn ServicesTable.categoryId
    var description by ServicesTable.description
    var imageUrl by ServicesTable.imageUrl
    var duration by ServicesTable.duration
    var price by ServicesTable.price
    var favoringClients by UserEntity via FavouritesTable
    var similarForms by FormEntity via FormsServicesRelationsTable
    var promotions by PromotionEntity via PromotionServiceTable
}

fun serviceEntityToService(serviceEntity: ServiceEntity): Service {
    val promotionsServiceInfo = PromotionServiceTable.innerJoin(ServicesTable).innerJoin(PromotionsTable)
        .select(PromotionServiceTable.columns + PromotionsTable.columns)
        .where {
            (PromotionServiceTable.serviceId eq serviceEntity.id)
        }

    val currentDate = LocalDate.now()

    val promotionRow = promotionsServiceInfo.mapNotNull {
        val startDate = it[PromotionsTable.startDate]
        val endDate = it[PromotionsTable.endDate]
        if (currentDate.isAfter(startDate) && currentDate.isBefore(endDate)) {
            it
        } else {
            null
        }
    }.maxByOrNull { it[PromotionsTable.startDate] }

    val promotionId = promotionRow?.let {
        it[PromotionsTable.id]
    }

    val promotionName = promotionRow?.let {
        it[PromotionsTable.promotionName]
    }

    val priceWithPromotion = promotionRow?.let {
        it[PromotionServiceTable.priceWithPromotion]
    }

    val discountPercentage = promotionRow?.let {
        it[PromotionServiceTable.discountPercentage]
    }

    return Service(
        id = serviceEntity.id.value,
        name = serviceEntity.serviceName,
        category = serviceEntity.category.name,
        description = serviceEntity.description,
        imageUrl = serviceEntity.imageUrl,
        duration = serviceEntity.duration,
        price = serviceEntity.price,
        promotionId = promotionId?.value,
        promotionName = promotionName,
        priceWithPromotion = priceWithPromotion,
        discountPercentage = discountPercentage,
    )
}
