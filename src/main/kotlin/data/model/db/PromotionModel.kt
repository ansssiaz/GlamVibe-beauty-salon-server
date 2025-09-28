package com.glamvibe.data.model.db

import com.glamvibe.domain.model.Promotion
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date
import java.time.LocalDate

object PromotionsTable : IntIdTable("promotions") {
    val promotionName = varchar("promotion_name", 50)
    val startDate = date("start_date")
    val endDate = date("end_date")
    val imageUrl = varchar("image_url", 150)
    val description = text("description")
}

class PromotionEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PromotionEntity>(PromotionsTable)

    var promotionName by PromotionsTable.promotionName
    var startDate by PromotionsTable.startDate
    var endDate by PromotionsTable.endDate
    var imageUrl by PromotionsTable.imageUrl
    var description by PromotionsTable.description
    var services by ServiceEntity via PromotionServiceTable
    val appointments by AppointmentEntity optionalReferrersOn AppointmentsTable.promotionId
}

fun promotionEntityToPromotion(promotionEntity: PromotionEntity): Promotion {
    val currentDate = LocalDate.now()
    return Promotion(
        id = promotionEntity.id.value,
        name = promotionEntity.promotionName,
        startDate = promotionEntity.startDate.toKotlinLocalDate(),
        endDate = promotionEntity.endDate.toKotlinLocalDate(),
        imageUrl = promotionEntity.imageUrl,
        isCurrent = currentDate.isAfter(promotionEntity.startDate) && currentDate.isBefore(promotionEntity.endDate),
        description = promotionEntity.description,
        services = promotionEntity.services.map { serviceEntityToService(it) }
    )
}