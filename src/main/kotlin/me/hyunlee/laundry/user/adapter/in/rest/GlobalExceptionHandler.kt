package me.hyunlee.laundry.user.adapter.`in`.rest

import me.hyunlee.laundry.common.ApiResponse
import me.hyunlee.laundry.user.domain.exception.UserNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(UserNotFoundException::class)
    fun handle(e: UserNotFoundException, req: WebRequest): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("user not found: ${e.message}")

        val response = ApiResponse.exceptionError<Nothing>(
            msg = e.message ?: "User not found",
            errCode = "USER_NOT_FOUND",
            path = getPath(req)
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    private fun getPath(req : WebRequest) : String? {
        return req.getDescription(false).removePrefix("uri=").takeIf { it.isNotBlank() }
    }
}