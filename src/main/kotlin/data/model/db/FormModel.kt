package com.glamvibe.data.model.db

import com.glamvibe.domain.model.Form
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object FormsTable : IntIdTable("forms") {
    val clientData = text("client_data")
    val clientId = reference(
        "client_id",
        UsersTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
}

class FormEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FormEntity>(FormsTable)

    var clientData by FormsTable.clientData
    var client by UserEntity referencedOn FormsTable.clientId
    var similarServices by ServiceEntity via FormsServicesRelationsTable
}

fun formEntityToForm(formEntity: FormEntity) = Form(
    id = formEntity.id.value,
    clientData = formEntity.clientData,
)
