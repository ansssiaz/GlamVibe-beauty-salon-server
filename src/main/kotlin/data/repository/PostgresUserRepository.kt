package com.glamvibe.data.repository

import com.glamvibe.data.model.UserEntity
import com.glamvibe.data.model.UsersTable
import com.glamvibe.data.model.userEntityToUser
import com.glamvibe.domain.model.User
import com.glamvibe.domain.repository.UserRepository
import com.glamvibe.plugins.DatabaseConfiguration.suspendTransaction
import kotlinx.datetime.toJavaLocalDate
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

class PostgresUserRepository : UserRepository {
    override suspend fun getUserById(id: Int): User? = suspendTransaction {
        UserEntity
            .find { (UsersTable.id eq id) }
            .limit(1)
            .map(::userEntityToUser)
            .firstOrNull()
    }

    override suspend fun getAllClients(): List<User?> = suspendTransaction {
        UserEntity
            .find { (UsersTable.role eq "Клиент") }
            .map(::userEntityToUser)
    }

    override suspend fun addClient(client: User): User = suspendTransaction {
        //хеширование пароля
        userEntityToUser(
            UserEntity.new {
                lastname = client.lastname
                name = client.name
                patronymic = client.patronymic
                birthDate = client.birthDate.toJavaLocalDate()
                phone = client.phone
                email = client.email
                login = client.login
                passwordHash = client.passwordHash
                role = "Клиент"
            }
        )
    }

    override suspend fun updateClient(id: Int, client: User): User = suspendTransaction {
        //хеширование нового пароля
        val updatedClient = UserEntity.findByIdAndUpdate(id) {
            it.lastname = client.lastname
            it.name = client.name
            it.patronymic = client.patronymic
            it.birthDate = client.birthDate.toJavaLocalDate()
            it.phone = client.phone
            it.email = client.email
            it.passwordHash = client.passwordHash
        } ?: throw IllegalArgumentException("Client not found")
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

    override suspend fun addAdministrator(administrator: User): User = suspendTransaction {
        //хеширование пароля
        userEntityToUser(
            UserEntity.new {
                lastname = administrator.lastname
                name = administrator.name
                patronymic = administrator.patronymic
                birthDate = administrator.birthDate.toJavaLocalDate()
                phone = administrator.phone
                email = administrator.email
                login = administrator.login
                passwordHash = administrator.passwordHash
                role = "Администратор"
            }
        )
    }

    override suspend fun updateAdministrator(id: Int, administrator: User): User = suspendTransaction {
        //хеширование нового пароля
        val updatedAdministrator = UserEntity.findByIdAndUpdate(id) {
            it.lastname = administrator.lastname
            it.name = administrator.name
            it.patronymic = administrator.patronymic
            it.birthDate = administrator.birthDate.toJavaLocalDate()
            it.phone = administrator.phone
            it.email = administrator.email
            it.passwordHash = administrator.passwordHash
        } ?: throw IllegalArgumentException("Administrator not found")
        userEntityToUser(updatedAdministrator)
    }
}