package com.glamvibe.data.repository

import com.glamvibe.data.model.db.*
import com.glamvibe.data.model.request.NewPromotion
import com.glamvibe.domain.model.Promotion
import com.glamvibe.domain.repository.PromotionRepository
import com.glamvibe.plugins.DatabaseConfiguration.suspendTransaction
import kotlinx.datetime.toJavaLocalDate
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import java.time.LocalDate

class PromotionRepositoryImpl : PromotionRepository {
    override suspend fun getCurrentPromotions(): List<Promotion> = suspendTransaction {
        val currentDate = LocalDate.now()
        PromotionEntity
            .find { (PromotionsTable.startDate less currentDate) and (PromotionsTable.endDate greater currentDate) }
            .map(::promotionEntityToPromotion)
    }

    override suspend fun getAllPromotions(): List<Promotion> = suspendTransaction {
        PromotionEntity
            .all()
            .map(::promotionEntityToPromotion)
    }

    override suspend fun getPromotion(promotionId: Int): Promotion? = suspendTransaction {
        PromotionEntity
            .find { (PromotionsTable.id eq promotionId) }
            .limit(1)
            .map(::promotionEntityToPromotion)
            .firstOrNull()
    }


    override suspend fun createPromotion(promotion: NewPromotion): Promotion = suspendTransaction {
        val promotionEntity = PromotionEntity.new {
            promotionName = promotion.name
            startDate = promotion.startDate.toJavaLocalDate()
            endDate = promotion.endDate.toJavaLocalDate()
            description = promotion.description
            imageUrl = promotion.image
        }

        for (service in promotion.services) {
            val existingService = ServiceEntity.findById(service.id)
                ?: throw IllegalArgumentException("Service with id '${service.id}' not found")

            PromotionServiceTable.insert {
                it[promotionId] = promotionEntity.id
                it[serviceId] = existingService.id
                it[priceWithPromotion] = service.priceWithPromotion
                it[discountPercentage] = service.discountPercentage
            }
        }

        promotionEntityToPromotion(promotionEntity)
    }

    override suspend fun updatePromotion(id: Int, promotion: NewPromotion): Promotion = suspendTransaction {
        val promotionEntity = PromotionEntity.findByIdAndUpdate(id) {
            it.promotionName = promotion.name
            it.startDate = promotion.startDate.toJavaLocalDate()
            it.endDate = promotion.endDate.toJavaLocalDate()
            it.description = promotion.description
            it.imageUrl = promotion.image
        } ?: throw IllegalArgumentException("Promotion is not found")

        val currentServices = PromotionServiceTable
            .select(PromotionServiceTable.columns)
            .where { PromotionServiceTable.promotionId eq id }
            .map { it[PromotionServiceTable.serviceId] }

        val newServiceIds = promotion.services.map { it.id }

        val servicesToRemove = currentServices.filter { it.value !in newServiceIds }

        for (serviceIdToRemove in servicesToRemove) {
            PromotionServiceTable.deleteWhere {
                (promotionId eq id) and (serviceId eq serviceIdToRemove)
            }
        }

        for (service in promotion.services) {
            val existingService = currentServices.firstOrNull { it.value == service.id }

            if (existingService != null) {
                PromotionServiceTable.update({
                    (PromotionServiceTable.promotionId eq id) and (PromotionServiceTable.serviceId eq service.id)
                }) {
                    it[priceWithPromotion] = service.priceWithPromotion
                    it[discountPercentage] = service.discountPercentage
                }
            } else {
                PromotionServiceTable.insert {
                    it[promotionId] = id
                    it[serviceId] = service.id
                    it[priceWithPromotion] = service.priceWithPromotion
                    it[discountPercentage] = service.discountPercentage
                }
            }
        }
        promotionEntityToPromotion(promotionEntity)
    }

    override suspend fun deletePromotion(promotionId: Int): Boolean = suspendTransaction {
        PromotionsTable.deleteWhere { PromotionsTable.id eq promotionId } > 0
    }
}