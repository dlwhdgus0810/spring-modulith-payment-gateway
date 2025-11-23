package me.hyunlee.laundry.payment.adapter.`in`.web.method

import me.hyunlee.laundry.common.adapter.`in`.web.ApiResponse
import me.hyunlee.laundry.payment.domain.exception.PaymentException
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.stereotype.Component
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.ResponseStatusException

@RestControllerAdvice(basePackages = ["me.hyunlee.laundry.payment"])
@Component("paymentExceptionHandler")
@Order(Ordered.HIGHEST_PRECEDENCE)
class PaymentExceptionHandler {

    private val logger = LoggerFactory.getLogger(PaymentExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun typeMismatch(ex: MethodArgumentTypeMismatchException, req: WebRequest): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("MethodArgumentTypeMismatchException: message: ${ex.message}, path: ${getPath(req)}")

        val response = ApiResponse.exceptionError<Nothing>(
            msg = ex.message,
            errCode = "ARGUMENT_TYPE_MISSMATCH",
            path = getPath(req)
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun notReadable(ex: HttpMessageNotReadableException, req: WebRequest): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("HttpMessageNotReadableException: message: ${ex.message}, path: ${getPath(req)}")

        val response = ApiResponse.exceptionError<Nothing>(
            msg = ex.message ?: "",
            errCode = "HTTP_MESSAGE_NOT_READABLE",
            path = getPath(req)
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class, BindException::class)
    fun validation(ex: Exception, req: WebRequest): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("MethodArgumentNotValidException: message: ${ex.message}, path: ${getPath(req)}")

        val response = ApiResponse.exceptionError<Nothing>(
            msg = ex.message ?: "",
            errCode = "ARGUMENT_NOT_VALID",
            path = getPath(req)
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun status(ex: ResponseStatusException, req: WebRequest): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("[PAYMENT] error occurred: ${ex.message}, path: ${getPath(req)}")

        val response = ApiResponse.exceptionError<Nothing>(
            msg = ex.message,
            errCode = "RESPONSE_STATUS_EXP",
            path = getPath(req)
        )

        return ResponseEntity.status(ex.statusCode).body(response)
    }

    @ExceptionHandler(PaymentException::class)
    fun handlePaymentException(e: PaymentException, req: WebRequest): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("[PAYMENT] error occurred: ${e.message}, path: ${getPath(req)}")

        val response = ApiResponse.exceptionError<Nothing>(
            msg = e.message ?: "[PAYMENT] error occurred",
            errCode = "PAYMENT_EXCEPTION",
            path = getPath(req)
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }


    @ExceptionHandler(Exception::class)
    fun handle(e: Exception, req: WebRequest): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("[PAYMENT] error occurred: ${e.message}, path: ${getPath(req)}")

        val response = ApiResponse.exceptionError<Nothing>(
            msg = e.message ?: "[PAYMENT] error occurred",
            errCode = "SERVER_EXCEPTION",
            path = getPath(req)
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

    private fun getPath(req: WebRequest): String? {
        return req.getDescription(false).removePrefix("uri=").takeIf { it.isNotBlank() }
    }
}