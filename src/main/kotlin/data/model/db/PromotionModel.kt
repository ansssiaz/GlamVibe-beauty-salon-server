package com.glamvibe.data.model.db

import com.glamvibe.domain.model.Promotion
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
        startDate = promotionEntity.startDate,
        endDate = promotionEntity.endDate,
        imageUrl = promotionEntity.imageUrl,
        isCurrent = currentDate.isAfter(promotionEntity.startDate) && currentDate.isBefore(promotionEntity.endDate),
        description = promotionEntity.description,
        services = promotionEntity.services.map {
            serviceEntityToService(it)
            /*val promotionServiceInfo = PromotionServiceTable.innerJoin(ServicesTable).innerJoin(PromotionsTable)
                .select(PromotionServiceTable.columns)
                .where {
                    (PromotionServiceTable.serviceId eq service.id) and (PromotionServiceTable.promotionId eq promotionEntity.id)
                }.singleOrNull()

            Service(
                id = service.id.value,
                name = service.serviceName,
                description = service.description,
                imageUrl = service.imageUrl,
                duration = service.duration,
                price = service.price,
                priceWithPromotion = if (promotionServiceInfo != null && currentDate.isAfter(promotionEntity.startDate) && currentDate.isBefore(
                        promotionEntity.endDate
                    )
                ) {
                    promotionServiceInfo[PromotionServiceTable.priceWithPromotion]
                } else {
                    null
                },
                discountPercentage = if (promotionServiceInfo != null && currentDate.isAfter(promotionEntity.startDate) && currentDate.isBefore(
                        promotionEntity.endDate
                    )
                ) {
                    promotionServiceInfo[PromotionServiceTable.discountPercentage]
                } else {
                    null
                }
            )*/
        }
    )
}