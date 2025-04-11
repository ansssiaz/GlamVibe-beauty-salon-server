package com.glamvibe.data.model.db

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object PromotionServiceTable : Table("promotion_service") {
    val promotionId = reference(
        "promotion_id",
        PromotionsTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val serviceId = reference(
        "service_id",
        ServicesTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val priceWithPromotion = integer("price_with_promotion")
    val discountPercentage = integer("discount_percentage")
}