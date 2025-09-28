package com.glamvibe.routes

import com.glamvibe.data.model.request.NewAppointment
import com.glamvibe.data.model.request.NewAppointmentDateTime
import com.glamvibe.data.model.request.CommentForCancellation
import com.glamvibe.domain.model.Role
import com.glamvibe.domain.model.getRoleByString
import com.glamvibe.domain.repository.AppointmentRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.YearMonth

fun Route.appointmentsRouting(repository: AppointmentRepository) {
    authenticate {
        route("/appointments") {
            get("/{year}/{month}/{day}") {
                val year = call.parameters["year"]?.toInt()
                val month = call.parameters["month"]?.toInt()
                val day = call.parameters["day"]?.toInt()

                if (year == null || month == null || day == null || month < 1 || month > 12) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                val principal = call.principal<JWTPrincipal>()

                if (principal != null) {
                    val currentUserRole = principal.payload.getClaim("role").asString()

                    if (currentUserRole.getRoleByString() == Role.ADMINISTRATOR) {
                        val date = YearMonth.of(year, month).atDay(day)
                        val appointments = repository.getAllAppointmentsForDay(date)
                        call.respond(appointments)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "Access denied")
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                }
            }

            get("/{clientId}") {
                val clientId = call.parameters["clientId"]?.toInt()

                if (clientId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                val principal = call.principal<JWTPrincipal>()

                if (principal != null) {
                    val currentUserId = principal.payload.getClaim("id").asInt()
                    val currentUserRole = principal.payload.getClaim("role").asString()

                    if (currentUserId == clientId || currentUserRole.getRoleByString() == Role.ADMINISTRATOR) {
                        val appointments = repository.getAppointmentsOfClient(clientId)
                        call.respond(appointments)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "Access denied")
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                }
            }

            get("/{appointmentId}/clients/{clientId}") {
                val appointmentId = call.parameters["appointmentId"]?.toInt()
                val clientId = call.parameters["clientId"]?.toInt()

                if (appointmentId == null || clientId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                val principal = call.principal<JWTPrincipal>()

                if (principal != null) {
                    val currentUserId = principal.payload.getClaim("id").asInt()
                    val currentUserRole = principal.payload.getClaim("role").asString()

                    if (currentUserId == clientId || currentUserRole.getRoleByString() == Role.ADMINISTRATOR) {
                        val appointment = repository.getAppointmentById(appointmentId)
                        if (appointment == null) {
                            call.respond(HttpStatusCode.NotFound)
                            return@get
                        }
                        call.respond(appointment)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "Access denied")
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "User is not authenticated")
                }
            }

            get("/{appointmentId}") {
                val appointmentId = call.parameters["appointmentId"]?.toInt()

                if (appointmentId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                val principal = call.principal<JWTPrincipal>()

                if (principal != null) {
                    val currentUserRole = principal.payload.getClaim("role").asString()

                    if (currentUserRole.getRoleByString() == Role.ADMINISTRATOR) {
                        val appointment = repository.getAppointmentById(appointmentId)
                        if (appointment == null) {
                            call.respond(HttpStatusCode.NotFound)
                            return@get
                        }
                        call.respond(appointment)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "Access denied")
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "User is not authenticated")
                }
            }

            post {
                try {
                    val principal = call.principal<JWTPrincipal>()

                    if (principal != null) {
                        val currentUserRole = principal.payload.getClaim("role").asString()

                        if (currentUserRole.getRoleByString() == Role.ADMINISTRATOR) {
                            val newAppointment = call.receive<NewAppointment>()
                            val appointment = repository.makeAppointment(newAppointment)
                            call.respond(appointment)
                        } else {
                            call.respond(HttpStatusCode.Forbidden, "Access denied")
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Client, service or master are not found")
                }
            }

            post("/{clientId}") {
                try {
                    val clientId = call.parameters["clientId"]?.toInt()

                    if (clientId == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@post
                    }

                    val principal = call.principal<JWTPrincipal>()

                    if (principal != null) {
                        val currentUserId = principal.payload.getClaim("id").asInt()
                        val currentUserRole = principal.payload.getClaim("role").asString()

                        if (currentUserId == clientId || currentUserRole.getRoleByString() == Role.ADMINISTRATOR) {
                            val newAppointment = call.receive<NewAppointment>()
                            val appointment = repository.makeAppointment(newAppointment)
                            call.respond(appointment)
                        } else {
                            call.respond(HttpStatusCode.Forbidden, "Access denied")
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Client, service or master are not found")
                }
            }

            post("confirm/{appointmentId}") {
                try {
                    val appointmentId = call.parameters["appointmentId"]?.toInt()

                    if (appointmentId == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@post
                    }

                    val principal = call.principal<JWTPrincipal>()

                    if (principal != null) {
                        val currentUserRole = principal.payload.getClaim("role").asString()

                        if (currentUserRole.getRoleByString() == Role.ADMINISTRATOR) {
                            val appointment = repository.confirmAppointment(appointmentId)
                            call.respond(appointment)
                        } else {
                            call.respond(HttpStatusCode.Forbidden, "Access denied")
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Appointment is not found")
                }
            }

            post("complete/{appointmentId}") {
                try {
                    val appointmentId = call.parameters["appointmentId"]?.toInt()

                    if (appointmentId == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@post
                    }

                    val principal = call.principal<JWTPrincipal>()

                    if (principal != null) {
                        val currentUserRole = principal.payload.getClaim("role").asString()

                        if (currentUserRole.getRoleByString() == Role.ADMINISTRATOR) {
                            val appointment = repository.markCompletion(appointmentId)
                            call.respond(appointment)
                        } else {
                            call.respond(HttpStatusCode.Forbidden, "Access denied")
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Appointment is not found")
                }
            }

            post("cancel/{appointmentId}/clients/{clientId}") {
                try {
                    val appointmentId = call.parameters["appointmentId"]?.toInt()
                    val clientId = call.parameters["clientId"]?.toInt()

                    if (appointmentId == null || clientId == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@post
                    }

                    val principal = call.principal<JWTPrincipal>()

                    if (principal != null) {
                        val currentUserId = principal.payload.getClaim("id").asInt()
                        val currentUserRole = principal.payload.getClaim("role").asString()

                        if (currentUserId == clientId) {
                            val appointment = repository.cancelAppointment(
                                appointmentId,
                                null,
                                currentUserRole.getRoleByString()
                            )
                            call.respond(appointment)
                        } else {
                            call.respond(HttpStatusCode.Forbidden, "Access denied")
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Appointment is not found")
                }
            }

            post("admin/cancel/{appointmentId}") {
                try {
                    val appointmentId = call.parameters["appointmentId"]?.toInt()

                    if (appointmentId == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@post
                    }

                    val principal = call.principal<JWTPrincipal>()

                    if (principal != null) {
                        val currentUserRole = principal.payload.getClaim("role").asString()

                        if (currentUserRole.getRoleByString() == Role.ADMINISTRATOR) {
                            val commentForCancellation = call.receive<CommentForCancellation>()
                            val appointment = repository.cancelAppointment(
                                appointmentId,
                                commentForCancellation,
                                currentUserRole.getRoleByString()
                            )
                            call.respond(appointment)
                        } else {
                            call.respond(HttpStatusCode.Forbidden, "Access denied")
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Appointment is not found")
                }
            }

            post("reschedule/{appointmentId}/clients/{clientId}") {
                try {
                    val appointmentId = call.parameters["appointmentId"]?.toInt()
                    val clientId = call.parameters["clientId"]?.toInt()

                    if (appointmentId == null || clientId == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@post
                    }

                    val principal = call.principal<JWTPrincipal>()

                    if (principal != null) {
                        val currentUserId = principal.payload.getClaim("id").asInt()

                        if (currentUserId == clientId) {
                            val newAppointment = call.receive<NewAppointmentDateTime>()
                            val appointment = repository.rescheduleAppointment(appointmentId, newAppointment)
                            call.respond(appointment)
                        } else {
                            call.respond(HttpStatusCode.Forbidden, "Access denied")
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Appointment is not found")
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
                        if (repository.deleteAppointment(id)) {
                            call.respond(HttpStatusCode.OK)
                        } else {
                            call.respondText(
                                "No appointment with id $id",
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