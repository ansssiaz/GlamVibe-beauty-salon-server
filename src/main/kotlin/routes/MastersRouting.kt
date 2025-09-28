package com.glamvibe.routes

import com.glamvibe.domain.model.Master
import com.glamvibe.domain.model.getRoleByString
import com.glamvibe.domain.repository.MasterRepository
import io.ktor.http.*
import com.glamvibe.domain.model.Role
import com.glamvibe.domain.model.WorkingDay
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import kotlinx.serialization.builtins.serializer

fun Route.mastersRouting(repository: MasterRepository) {
    get("/static/masters/{filename}") {
        val filename = call.parameters["filename"] ?: throw NotFoundException()
        val mediaPath = System.getenv("MASTERS_MEDIA_PATH")
        val file = File("$mediaPath/$filename")
        if (file.exists()) {
            call.response.cacheControl(CacheControl.MaxAge(3600))
            call.respondFile(file)
        } else {
            throw NotFoundException("File not found")
        }
    }
    authenticate {
        route("/masters") {
            get {
                val baseUrl = "${call.request.origin.scheme}://${call.request.host()}:${call.request.port()}"
                val masters = repository.getAllMasters()
                    .map {
                        it.copy(
                            photoUrl = "$baseUrl${it.photoUrl}"
                        )
                    }
                call.respond(masters)
            }

            get("/{id}") {
                val baseUrl = "${call.request.origin.scheme}://${call.request.host()}:${call.request.port()}"
                val masterId = call.parameters["id"]?.toInt()

                if (masterId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                val master = repository.getMasterInformation(masterId)?.let {
                    it.copy(
                        photoUrl = "$baseUrl${it.photoUrl}"
                    )
                }

                if (master == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }
                call.respond(master)
            }

            /* get("/services/{serviceId}") {
                 try {
                     val serviceId = call.parameters["serviceId"]?.toInt()

                     if (serviceId == null) {
                         call.respond(HttpStatusCode.BadRequest)
                         return@get
                     }

                     val masters = repository.getMastersWithCurrentAppointments(serviceId)
                     call.respond(masters)
                 } catch (e: IllegalArgumentException) {
                     call.respond(HttpStatusCode.NotFound, e.message ?: "Service is not found")
                 }
             }*/

            get("/appointments") {
                try {
                    val master = repository.getMastersWithCurrentAppointments()
                    call.respond(master)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Master is not found")
                }
            }

            get("/{masterId}/appointments") {
                try {
                    val masterId = call.parameters["masterId"]?.toInt()

                    if (masterId == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@get
                    }

                    val master = repository.getMasterWithCurrentAppointments(masterId)
                    call.respond(master)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Master is not found")
                }
            }

            post {
                try {
                    val principal = call.principal<JWTPrincipal>()

                    if (principal != null) {
                        val currentUserRole = principal.payload.getClaim("role").asString()

                        if (currentUserRole.getRoleByString() == Role.ADMINISTRATOR) {
                            val multipartData = call.receiveMultipart()
                            var master: Master? = null
                            var photoUrl: String? = null

                            multipartData.forEachPart { part ->
                                when (part) {
                                    is PartData.FormItem -> {
                                        when (part.name) {
                                            "lastname" -> master =
                                                master?.copy(lastname = part.value) ?: Master(lastname = part.value)

                                            "name" -> master =
                                                master?.copy(name = part.value) ?: Master(name = part.value)

                                            "patronymic" -> master =
                                                master?.copy(patronymic = part.value) ?: Master(patronymic = part.value)

                                            "birthDate" -> {
                                                val date = LocalDate.parse(part.value)
                                                master =
                                                    master?.copy(birthDate = date) ?: Master(birthDate = date)
                                            }

                                            "phone" -> master =
                                                master?.copy(phone = part.value) ?: Master(phone = part.value)

                                            "email" -> master =
                                                master?.copy(email = part.value) ?: Master(email = part.value)

                                            "specialty" -> master =
                                                master?.copy(specialty = part.value) ?: Master(specialty = part.value)

                                            "categories" -> {
                                                val categories =
                                                    Json.decodeFromString(
                                                        ListSerializer(String.serializer()),
                                                        part.value
                                                    )
                                                master = master?.copy(categories = categories)
                                                    ?: Master(categories = categories)
                                            }

                                            "schedule" -> {
                                                val schedule =
                                                    Json.decodeFromString(
                                                        ListSerializer(WorkingDay.serializer()),
                                                        part.value
                                                    )

                                                master = master?.copy(schedule = schedule)
                                                    ?: Master(schedule = schedule)
                                            }

                                            "dateOfEmployment" -> {
                                                val date = LocalDate.parse(part.value)
                                                master =
                                                    master?.copy(dateOfEmployment = date)
                                                        ?: Master(dateOfEmployment = date)
                                            }

                                            "workExperience" -> master =
                                                master?.copy(workExperience = part.value.toInt()) ?: Master(
                                                    workExperience = part.value.toInt()
                                                )

                                        }
                                    }

                                    is PartData.FileItem -> {
                                        when (part.name) {
                                            "photo" -> {
                                                val filename = part.originalFileName ?: return@forEachPart
                                                val mediaPath = System.getenv("MASTERS_MEDIA_PATH")
                                                val file = File("$mediaPath/$filename")
                                                part.provider().copyAndClose(file.writeChannel())
                                                photoUrl = "/static/masters/$filename"
                                            }
                                        }
                                    }

                                    is PartData.BinaryChannelItem -> {}

                                    is PartData.BinaryItem -> {}
                                }
                                part.dispose()
                            }

                            if (master != null && photoUrl != null) {
                                val savedMaster =
                                    repository.addMaster(
                                        requireNotNull(master).copy(
                                            photoUrl = requireNotNull(
                                                photoUrl
                                            )
                                        )
                                    )
                                call.respond(HttpStatusCode.Created, savedMaster)
                            } else {
                                call.respond(HttpStatusCode.BadRequest, "Master data or photo is missing.")
                            }
                        } else {
                            call.respond(HttpStatusCode.Forbidden, "Access denied")
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Service category is not found")
                }
            }

            put("/{id}") {
                try {
                    val id = call.parameters["id"]?.toInt() ?: return@put call.respondText(
                        "Missing id",
                        status = HttpStatusCode.BadRequest
                    )
                    val principal = call.principal<JWTPrincipal>()

                    if (principal != null) {
                        val currentUserRole = principal.payload.getClaim("role").asString()

                        if (currentUserRole.getRoleByString() == Role.ADMINISTRATOR) {

                            val existingMaster = repository.getMasterInformation(id) ?: return@put call.respondText(
                                "No master with id $id",
                                status = HttpStatusCode.NotFound
                            )

                            val multipartData = call.receiveMultipart()
                            var master: Master? = null
                            var photoUrl: String? = null

                            multipartData.forEachPart { part ->
                                when (part) {
                                    is PartData.FormItem -> {
                                        when (part.name) {
                                            "lastname" -> master =
                                                master?.copy(lastname = part.value) ?: Master(lastname = part.value)

                                            "name" -> master =
                                                master?.copy(name = part.value) ?: Master(name = part.value)

                                            "patronymic" -> master =
                                                master?.copy(patronymic = part.value) ?: Master(patronymic = part.value)

                                            "birthDate" -> {
                                                val date = LocalDate.parse(part.value)
                                                master =
                                                    master?.copy(birthDate = date) ?: Master(birthDate = date)
                                            }

                                            "phone" -> master =
                                                master?.copy(phone = part.value) ?: Master(phone = part.value)

                                            "email" -> master =
                                                master?.copy(email = part.value) ?: Master(email = part.value)

                                            "specialty" -> master =
                                                master?.copy(specialty = part.value) ?: Master(specialty = part.value)

                                            "categories" -> {
                                                val categories =
                                                    Json.decodeFromString(
                                                        ListSerializer(String.serializer()),
                                                        part.value
                                                    )

                                                master = master?.copy(categories = categories)
                                                    ?: Master(categories = categories)
                                            }

                                            "schedule" -> {
                                                val schedule =
                                                    Json.decodeFromString(
                                                        ListSerializer(WorkingDay.serializer()),
                                                        part.value
                                                    )

                                                master = master?.copy(schedule = schedule)
                                                    ?: Master(schedule = schedule)
                                            }

                                            "dateOfEmployment" -> {
                                                val date = LocalDate.parse(part.value)
                                                master =
                                                    master?.copy(dateOfEmployment = date)
                                                        ?: Master(dateOfEmployment = date)
                                            }

                                            "workExperience" -> master =
                                                master?.copy(workExperience = part.value.toInt()) ?: Master(
                                                    workExperience = part.value.toInt()
                                                )

                                        }
                                    }

                                    is PartData.FileItem -> {
                                        when (part.name) {
                                            "photoUrl" -> {
                                                val filename = part.originalFileName
                                                if (filename != null) {
                                                    val mediaPath = System.getenv("MASTERS_MEDIA_PATH")
                                                    val file = File("$mediaPath/$filename")
                                                    part.provider().copyAndClose(file.writeChannel())
                                                    photoUrl = "/static/masters/$filename"
                                                }
                                            }
                                        }
                                    }

                                    is PartData.BinaryChannelItem -> {}

                                    is PartData.BinaryItem -> {}
                                }
                                part.dispose()
                            }

                            if (master != null) {
                                val finalPhotoUrl = photoUrl ?: existingMaster.photoUrl
                                val updatedMaster =
                                    repository.updateMaster(
                                        id,
                                        requireNotNull(master).copy(photoUrl = finalPhotoUrl)
                                    )
                                call.respond(HttpStatusCode.OK, updatedMaster)
                            } else {
                                call.respond(HttpStatusCode.BadRequest, "Master data or image is missing.")
                            }
                        } else {
                            call.respond(HttpStatusCode.Forbidden, "Access denied")
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Master is not found")
                }
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.toInt()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@delete
                }
                val principal = call.principal<JWTPrincipal>()

                if (principal != null) {
                    val currentUserRole = principal.payload.getClaim("role").asString()

                    if (currentUserRole.getRoleByString() == Role.ADMINISTRATOR) {
                        if (repository.deleteMaster(id)) {
                            //удалить фото с сервера
                            call.respond(HttpStatusCode.OK)
                        } else {
                            call.respondText(
                                "No master with id $id",
                                status = HttpStatusCode.NotFound
                            )
                        }
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "Access denied")
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                }
            }
        }
    }
}