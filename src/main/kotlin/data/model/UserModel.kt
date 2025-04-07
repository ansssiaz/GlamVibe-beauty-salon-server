package com.glamvibe.data.model

import com.glamvibe.domain.model.Role
import com.glamvibe.domain.model.User
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

object UsersTable : IntIdTable("users") {
    val lastname = varchar("lastname", 50)
    val name = varchar("name", 50)
    val patronymic = varchar("patronymic", 50).nullable()
    val birthDate = date("birth_date")
    val phone = varchar("phone", 15)
    val email = varchar("email", 50)
    val login = varchar("login", 50)
    val passwordHash = varchar("password_hash", 50)
    val refreshToken = varchar("refresh_token", 100).nullable()
    val role = varchar("role", 15)
}

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(UsersTable)

    var lastname by UsersTable.lastname
    var name by UsersTable.name
    var patronymic by UsersTable.patronymic
    var birthDate by UsersTable.birthDate
    var phone by UsersTable.phone
    var email by UsersTable.email
    var login by UsersTable.login
    var passwordHash by UsersTable.passwordHash
    val form by FormEntity optionalBackReferencedOn FormsTable.clientId
    var favourites by ServiceEntity via FavouritesTable
    var refreshToken by UsersTable.refreshToken
    var role by UsersTable.role
    val appointments by AppointmentEntity referrersOn AppointmentsTable.clientId
}

fun userEntityToUser(userEntity: UserEntity): User {
    return when (userEntity.role) {
        "Администратор" -> User(
            id = userEntity.id.value,
            lastname = userEntity.lastname,
            name = userEntity.name,
            patronymic = userEntity.patronymic ?: "",
            birthDate = userEntity.birthDate.toKotlinLocalDate(),
            phone = userEntity.phone,
            email = userEntity.email,
            login = userEntity.login,
            passwordHash = userEntity.passwordHash,
            refreshToken = userEntity.refreshToken,
            role = Role.valueOf("ADMINISTRATOR")
        )

        "Клиент" -> User(
            id = userEntity.id.value,
            lastname = userEntity.lastname,
            name = userEntity.name,
            patronymic = userEntity.patronymic,
            birthDate = userEntity.birthDate.toKotlinLocalDate(),
            phone = userEntity.phone,
            email = userEntity.email,
            login = userEntity.login,
            passwordHash = userEntity.passwordHash,
            refreshToken = userEntity.refreshToken,
            role = Role.valueOf("CLIENT"),
            form = userEntity.form?.let { formEntityToForm(it) },
            //favourites = userEntity.favourites
        )

        else -> throw IllegalArgumentException("Unknown role: $userEntity.role")
    }
}