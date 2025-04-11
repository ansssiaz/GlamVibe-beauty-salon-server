package com.glamvibe.domain.repository

import com.glamvibe.domain.model.User
import com.glamvibe.data.model.request.RegisterUser
import com.glamvibe.data.model.request.UpdateUser

interface UserRepository {
    suspend fun registerUser(user: RegisterUser): User
    suspend fun getUserById(id: Int): User?
    suspend fun getUserByLogin(login: String): User?
    suspend fun getUserByToken(refreshToken: String): User?
    suspend fun updateRefreshToken(id: Int, refreshToken: String, expirationTime: Long): User
    suspend fun updateUserData(id: Int, user: UpdateUser): User
    suspend fun logOut(id: Int): Boolean
    suspend fun deleteUser(id: Int): Boolean
    suspend fun getAllClients(): List<User?>
    suspend fun getClientById(id: Int): User?
    suspend fun getAllAdministrators(): List<User?>
}