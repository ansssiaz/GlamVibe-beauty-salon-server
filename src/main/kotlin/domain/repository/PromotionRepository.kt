package com.glamvibe.domain.repository

import com.glamvibe.data.model.request.NewPromotion
import com.glamvibe.domain.model.Promotion

interface PromotionRepository {
    suspend fun getAllPromotions(): List<Promotion>
    suspend fun getCurrentPromotions(): List<Promotion>
    suspend fun getPromotion(promotionId: Int): Promotion?
    suspend fun createPromotion(promotion: NewPromotion): Promotion
    suspend fun updatePromotion(id: Int, promotion: NewPromotion): Promotion
    suspend fun deletePromotion(promotionId: Int): Boolean
}