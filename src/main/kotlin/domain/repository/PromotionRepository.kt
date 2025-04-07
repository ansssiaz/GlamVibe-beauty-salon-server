package com.glamvibe.domain.repository

import com.glamvibe.domain.model.Promotion

interface PromotionRepository {
    suspend fun getAllPromotions(): List<Promotion>
    suspend fun getPromotion(promotionId: Int): Promotion?
    suspend fun createPromotion(promotion: Promotion): Promotion
    suspend fun updatePromotion(promotionId: Int, promotion: Promotion): Promotion
    suspend fun deletePromotion(promotionId: Int)
}