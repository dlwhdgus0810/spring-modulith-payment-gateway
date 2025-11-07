package me.hyunlee.laundry.common.adapter

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success : Boolean,
    val message : String,
    val data : T? = null,
    val err : Error? = null
) {
    companion object {
        fun <T> success(data: T, msg : String = "Success"): ResponseEntity<ApiResponse<T>> {
            return ResponseEntity.ok(ApiResponse(true, msg, data))
        }

        fun <T> created(data: T, msg : String = "Created"): ResponseEntity<ApiResponse<T>> {
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse(true, msg, data))
        }

        fun <T> error(
            msg : String,
            errCode : String? = null,
            details : Any? = null,
            path : String? = null
        ) : ResponseEntity<ApiResponse<T>> {
            return ResponseEntity.badRequest().body(
                ApiResponse(false, msg, null, Error(errCode, details, path))
            )
        }

        fun <T> exceptionError(
            msg : String,
            errCode : String? = null,
            details : Any? = null,
            path : String? = null
        ) : ApiResponse<T> {
            return ApiResponse(false, msg, null, Error(errCode, details, path))
        }
    }
}

data class Error (
    val code : String? = null,
    val details : Any? = null,
    val path : String? = null
)