package me.hyunlee.laundry.common.adapter.`in`.web

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.time.Instant

open class CommonGlobalExceptionSupport {
    protected val log = LoggerFactory.getLogger(javaClass)

    data class ErrorResponse(
        val timestamp: Instant = Instant.now(),
        val code: String,
        val message: String,
        val detail: Any? = null
    )

    protected fun badRequest(code: String, message: String, detail: Any? = null) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(code = code, message = message, detail = detail))

    protected fun internal(message: String, detail: Any? = null) =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse(code = "INTERNAL_ERROR", message = message, detail = detail))
}