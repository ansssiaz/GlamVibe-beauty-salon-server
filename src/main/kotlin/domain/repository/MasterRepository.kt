package com.glamvibe.domain.repository

import com.glamvibe.domain.model.Master
import com.glamvibe.data.model.response.MasterWithAppointments

interface MasterRepository {
    suspend fun getAllMasters(): List<Master>
    suspend fun getMasterInformation(id: Int): Master?
    suspend fun getMastersWithCurrentAppointments(): List<MasterWithAppointments>
    suspend fun getMasterWithCurrentAppointments(masterId: Int): MasterWithAppointments
    suspend fun addMaster(master: Master): Master
    suspend fun updateMaster(id: Int, master: Master): Master
    suspend fun deleteMaster(id: Int): Boolean
}