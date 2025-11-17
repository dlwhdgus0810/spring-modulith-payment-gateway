package me.hyunlee.laundry.user.adapter.`in`.web

import me.hyunlee.laundry.common.adapter.`in`.web.ApiResponse
import me.hyunlee.laundry.user.domain.exception.UserException.UserNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = ["me.hyunlee.laundry.user"])
@Component("userGlobalExceptionHandler")
class UserExceptionHandler {

    private val logger = LoggerFactory.getLogger(UserExceptionHandler::class.java)

    @ExceptionHandler(UserNotFoundException::class)
    fun handle(e: UserNotFoundException, req: WebRequest): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("[USER] user not found: ${e.message}, path: ${getPath(req)}")

        val response = ApiResponse.exceptionError<Nothing>(
            msg = e.message ?: "User not found",
            errCode = "USER_NOT_FOUND",
            path = getPath(req)
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handle(e: Exception, req: WebRequest): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("error occurred: ${e.message}, path: ${getPath(req)}")

        val response = ApiResponse.exceptionError<Nothing>(
            msg = e.message ?: "[USER] error occurred",
            errCode = "SERVER_EXCEPTION",
            path = getPath(req)
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

    private fun getPath(req : WebRequest) : String? {
        return req.getDescription(false).removePrefix("uri=").takeIf { it.isNotBlank() }
    }
}