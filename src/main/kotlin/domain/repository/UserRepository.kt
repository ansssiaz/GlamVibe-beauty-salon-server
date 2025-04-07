package com.glamvibe.domain.repository

import com.glamvibe.domain.model.User

interface UserRepository {
    suspend fun getUserById(id: Int): User?
    suspend fun deleteUser(id: Int): Boolean
    suspend fun getAllClients(): List<User?>
    suspend fun addClient(client: User): User
    suspend fun updateClient(id: Int, client: User): User
    suspend fun getAllAdministrators(): List<User?>
    suspend fun addAdministrator(administrator: User): User
    suspend fun updateAdministrator(id: Int, administrator: User): User
}