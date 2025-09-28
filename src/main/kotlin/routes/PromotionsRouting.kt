package com.glamvibe.routes

import com.glamvibe.data.model.request.NewPromotion
import com.glamvibe.data.model.request.PromotionService
import com.glamvibe.domain.model.Role
import com.glamvibe.domain.model.getRoleByString
import com.glamvibe.domain.repository.PromotionRepository
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

fun Route.promotionsRouting(repository: PromotionRepository) {
    get("/static/promotions/{filename}") {
        val filename = call.parameters["filename"] ?: throw NotFoundException()
        val mediaPath = System.getenv("PROMOTIONS_MEDIA_PATH")
        val file = File("$mediaPath/$filename")
        if (file.exists()) {
            call.response.cacheControl(CacheControl.MaxAge(3600))
            call.respondFile(file)
        } else {
            throw NotFoundException("File not found")
        }
    }
    authenticate {
        route("/promotions") {
            get {
                val baseUrl = "${call.request.origin.scheme}://${call.request.host()}:${call.request.port()}"
                val promotions = repository.getAllPromotions()
                    .map {
                        it.copy(
                            imageUrl = "$baseUrl${it.imageUrl}"
                        )
                    }
                call.respond(promotions)
            }

            get("/current") {
                val baseUrl = "${call.request.origin.scheme}://${call.request.host()}:${call.request.port()}"
                val promotions = repository.getCurrentPromotions()
                    .map {
                        it.copy(
                            imageUrl = "$baseUrl${it.imageUrl}"
                        )
                    }
                call.respond(promotions)
            }

            get("/{id}") {
                val baseUrl = "${call.request.origin.scheme}://${call.request.host()}:${call.request.port()}"
                val promotionId = call.parameters["id"]?.toInt()

                if (promotionId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                val promotion = repository.getPromotion(promotionId)?.let { promotion ->
                    promotion.copy(
                        imageUrl = "$baseUrl${promotion.imageUrl}",
                        services = promotion.services.map {
                            it.copy(
                                imageUrl = "$baseUrl${it.imageUrl}"
                            )
                        }
                    )
                }

                if (promotion == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }
                call.respond(promotion)
            }

            post {
                try {
                    val principal = call.principal<JWTPrincipal>()

                    if (principal != null) {
                        val currentUserRole = principal.payload.getClaim("role").asString()

                        if (currentUserRole.getRoleByString() == Role.ADMINISTRATOR) {
                            val multipartData = call.receiveMultipart()
                            var promotion: NewPromotion? = null
                            var imageUrl: String? = null

                            multipartData.forEachPart { part ->
                                when (part) {
                                    is PartData.FormItem -> {
                                        when (part.name) {
                                            "name" -> promotion =
                                                promotion?.copy(name = part.value) ?: NewPromotion(name = part.value)

                                            "startDate" -> {
                                                val date = LocalDate.parse(part.value)
                                                promotion =
                                                    promotion?.copy(startDate = date) ?: NewPromotion(startDate = date)
                                            }

                                            "endDate" -> {
                                                val date = LocalDate.parse(part.value)
                                                promotion =
                                                    promotion?.copy(endDate = date) ?: NewPromotion(endDate = date)
                                            }

                                            "description" -> promotion =
                                                promotion?.copy(description = part.value)
                                                    ?: NewPromotion(description = part.value)

                                            "services" -> {
                                                val promotionServices =
                                                    Json.decodeFromString(
                                                        ListSerializer(PromotionService.serializer()),
                                                        part.value
                                                    )
                                                promotion = promotion?.copy(services = promotionServices)
                                                    ?: NewPromotion(services = promotionServices)
                                            }
                                        }
                                    }

                                    is PartData.FileItem -> {
                                        when (part.name) {
                                            "image" -> {
                                                val filename = part.originalFileName ?: return@forEachPart
                                                val mediaPath = System.getenv("PROMOTIONS_MEDIA_PATH")
                                                val file = File("$mediaPath/$filename")
                                                part.provider().copyAndClose(file.writeChannel())
                                                imageUrl = "/static/promotions/$filename"
                                            }
                                        }
                                    }

                                    is PartData.BinaryChannelItem -> {}

                                    is PartData.BinaryItem -> {}
                                }
                                part.dispose()
                            }

                            if (promotion != null && imageUrl != null) {
                                val savedPromotion =
                                    repository.createPromotion(
                                        requireNotNull(promotion).copy(
                                            image = requireNotNull(
                                                imageUrl
                                            )
                                        )
                                    )
                                call.respond(HttpStatusCode.Created, savedPromotion)
                            } else {
                                call.respond(HttpStatusCode.BadRequest, "Promotion data or image is missing.")
                            }
                        } else {
                            call.respond(HttpStatusCode.Forbidden, "Access denied")
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Service is not found")
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

                            val existingPromotion = repository.getPromotion(id) ?: return@put call.respondText(
                                "No promotion with id $id",
                                status = HttpStatusCode.NotFound
                            )

                            val multipartData = call.receiveMultipart()
                            var promotion: NewPromotion? = null
                            var imageUrl: String? = null

                            multipartData.forEachPart { part ->
                                when (part) {
                                    is PartData.FormItem -> {
                                        when (part.name) {
                                            "name" -> promotion =
                                                promotion?.copy(name = part.value) ?: NewPromotion(name = part.value)

                                            "startDate" -> {
                                                val date = LocalDate.parse(part.value)
                                                promotion =
                                                    promotion?.copy(startDate = date) ?: NewPromotion(startDate = date)
                                            }

                                            "endDate" -> {
                                                val date = LocalDate.parse(part.value)
                                                promotion =
                                                    promotion?.copy(endDate = date) ?: NewPromotion(endDate = date)
                                            }

                                            "description" -> promotion =
                                                promotion?.copy(description = part.value)
                                                    ?: NewPromotion(description = part.value)

                                            "services" -> {
                                                val promotionServices =
                                                    Json.decodeFromString(
                                                        ListSerializer(PromotionService.serializer()),
                                                        part.value
                                                    )
                                                promotion = promotion?.copy(services = promotionServices)
                                                    ?: NewPromotion(services = promotionServices)
                                            }
                                        }
                                    }

                                    is PartData.FileItem -> {
                                        when (part.name) {
                                            "image" -> {
                                                val filename = part.originalFileName
                                                if (filename != null) {
                                                    val mediaPath = System.getenv("PROMOTIONS_MEDIA_PATH")
                                                    val file = File("$mediaPath/$filename")
                                                    part.provider().copyAndClose(file.writeChannel())
                                                    imageUrl = "/static/promotions/$filename"
                                                }
                                            }
                                        }
                                    }

                                    is PartData.BinaryChannelItem -> {}

                                    is PartData.BinaryItem -> {}
                                }
                                part.dispose()
                            }

                            if (promotion != null) {
                                val finalImageUrl = imageUrl ?: existingPromotion.imageUrl
                                val updatedPromotion =
                                    repository.updatePromotion(
                                        id,
                                        requireNotNull(promotion).copy(image = finalImageUrl)
                                    )
                                call.respond(HttpStatusCode.OK, updatedPromotion)
                            } else {
                                call.respond(HttpStatusCode.BadRequest, "Promotion data or image is missing.")
                            }
                        } else {
                            call.respond(HttpStatusCode.Forbidden, "Access denied")
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Promotion is not found")
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
                        if (repository.deletePromotion(id)) {
                            //удалить фото с сервера
                            call.respond(HttpStatusCode.OK)
                        } else {
                            call.respondText(
                                "No promotion with id $id",
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