package me.hyunlee.laundry.common.adapter.`in`.web

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.ResponseStatusException

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE) // 마지막 폴백
class CommonGlobalExceptionHandler : CommonGlobalExceptionSupport() {

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun typeMismatch(ex: MethodArgumentTypeMismatchException) =
        badRequest(
            code = "TYPE_MISMATCH",
            message = "경로/쿼리 파라미터 타입이 올바르지 않습니다.",
            detail = mapOf("name" to ex.name, "value" to ex.value, "requiredType" to ex.requiredType?.simpleName)
        ).also { log.warn("TypeMismatch: {}", ex.message) }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun notReadable(ex: HttpMessageNotReadableException) =
        badRequest(
            code = "JSON_PARSE_ERROR",
            message = "요청 본문을 읽을 수 없습니다.",
            detail = ex.mostSpecificCause.message ?: ex.message
        ).also { log.warn("JSON parse: {}", ex.message) }

    @ExceptionHandler(MethodArgumentNotValidException::class, BindException::class)
    fun validation(ex: Exception): ResponseEntity<ErrorResponse> {
        val errors = when (ex) {
            is MethodArgumentNotValidException -> ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "invalid") }
            is BindException -> ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "invalid") }
            else -> emptyMap()
        }
        log.warn("Validation: {}", errors)
        return badRequest("VALIDATION_ERROR", "요청 값이 올바르지 않습니다.", errors)
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun status(ex: ResponseStatusException) =
        ResponseEntity.status(ex.statusCode).body(ErrorResponse(code = ex.statusCode.value().toString(), message = ex.reason ?: ""))
            .also { log.warn("Status: {}", ex.message) }

    @ExceptionHandler(Exception::class)
    fun any(ex: Exception) =
        internal("예상치 못한 오류가 발생했습니다.", ex.message).also { log.error("Unhandled", ex) }
}