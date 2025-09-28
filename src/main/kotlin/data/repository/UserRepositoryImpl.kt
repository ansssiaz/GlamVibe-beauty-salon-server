package com.glamvibe.data.repository

import com.glamvibe.data.model.db.*
import com.glamvibe.domain.model.User
import com.glamvibe.domain.model.getStringByRole
import com.glamvibe.domain.repository.UserRepository
import com.glamvibe.plugins.DatabaseConfiguration.suspendTransaction
import com.glamvibe.data.model.request.RegisterUser
import com.glamvibe.data.model.request.UpdateUser
import com.glamvibe.domain.model.Role
import kotlinx.datetime.toJavaLocalDate
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import com.toxicbakery.bcrypt.Bcrypt
import io.ktor.util.*

class UserRepositoryImpl : UserRepository {
    override suspend fun getUserById(id: Int): User? = suspendTransaction {
        UserEntity
            .find { (UsersTable.id eq id) }
            .limit(1)
            .map(::userEntityToUser)
            .firstOrNull()
    }

    override suspend fun getUserByLogin(login: String): User? = suspendTransaction {
        UserEntity
            .find { (UsersTable.login eq login) }
            .limit(1)
            .map(::userEntityToUser)
            .firstOrNull()
    }

    override suspend fun getUserByToken(refreshToken: String): User? = suspendTransaction {
        UserEntity
            .find { (UsersTable.refreshToken eq refreshToken) }
            .limit(1)
            .map(::userEntityToUser)
            .firstOrNull()
    }

    override suspend fun updateRefreshToken(id: Int, refreshToken: String, expirationTime: Long): User =
        suspendTransaction {
            val updatedUser = UserEntity.findByIdAndUpdate(id) {
                it.refreshToken = refreshToken
                it.refreshTokenExpirationTime = expirationTime
            } ?: throw IllegalArgumentException("User is not found")
            userEntityToUser(updatedUser)
        }

    override suspend fun logOut(id: Int): Boolean = suspendTransaction {
        UserEntity.findByIdAndUpdate(id) {
            it.refreshToken = null
            it.refreshTokenExpirationTime = null
        } ?: false
        true
    }

    override suspend fun getAllClients(): List<User?> = suspendTransaction {
        UserEntity
            .find { (UsersTable.role eq "Клиент") }
            .map(::userEntityToUser)
    }

    override suspend fun getClientById(id: Int): User? = suspendTransaction {
        UserEntity
            .find { (UsersTable.id eq id) }
            .limit(1)
            .map(::userEntityToUser)
            .firstOrNull()
    }

    override suspend fun registerUser(user: RegisterUser): User = suspendTransaction {
        val hash = Bcrypt.hash(user.password, 12)
        val newUserEntity = UserEntity.new {
            lastname = user.lastname
            name = user.name
            patronymic = user.patronymic
            birthDate = user.birthDate.toJavaLocalDate()
            phone = user.phone
            email = user.email
            login = user.login
            passwordHash = hash.encodeBase64()
            role = user.role.getStringByRole()
        }
        val newUser = userEntityToUser(newUserEntity)
        if (user.role == Role.CLIENT && user.formData != null) {
            newUser.form = formEntityToForm(FormEntity.new {
                client = newUserEntity
                clientData = user.formData
            })
        }
        newUser
    }

    override suspend fun updateUserData(id: Int, user: UpdateUser): User = suspendTransaction {
        val updatedClient = UserEntity.findByIdAndUpdate(id) {
            it.lastname = user.lastname
            it.name = user.name
            it.patronymic = user.patronymic
            it.birthDate = user.birthDate.toJavaLocalDate()
            it.phone = user.phone
            it.email = user.email

            if (user.login.isNotBlank()) {
                it.login = user.login
            }

            if (user.password.isNotBlank()) {
                val passwordHash = Bcrypt.hash(user.password, 12)
                it.passwordHash = passwordHash.encodeBase64()
            }
        } ?: throw IllegalArgumentException("User is not found")
        userEntityToUser(updatedClient)
    }

    override suspend fun deleteUser(id: Int): Boolean = suspendTransaction {
        UsersTable.deleteWhere { UsersTable.id eq id } > 0
    }

    override suspend fun getAllAdministrators(): List<User?> = suspendTransaction {
        UserEntity
            .find { (UsersTable.role eq "Администратор") }
            .map(::userEntityToUser)
    }
}