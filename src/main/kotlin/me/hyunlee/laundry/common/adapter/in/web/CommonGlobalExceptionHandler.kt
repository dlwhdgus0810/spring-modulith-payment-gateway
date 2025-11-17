package me.hyunlee.laundry.common.adapter.`in`.web

import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.ResponseStatusException

@RestControllerAdvice(basePackages = ["me.hyunlee.laundry"])
@Order(Ordered.LOWEST_PRECEDENCE) // 마지막 폴백
class CommonGlobalExceptionHandler : CommonGlobalExceptionSupport() {

    private val logger = LoggerFactory.getLogger(CommonGlobalExceptionHandler::class.java)

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
    fun handle(e: Exception, req: WebRequest): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("[COMMON] error occurred: ${e.message}, path: ${getPath(req)}")

        val response = ApiResponse.exceptionError<Nothing>(
            msg = e.message ?: "[COMMON] error occurred",
            errCode = "SERVER_EXCEPTION",
            path = getPath(req)
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

    private fun getPath(req: WebRequest): String? {
        return req.getDescription(false).removePrefix("uri=").takeIf { it.isNotBlank() }
    }
}