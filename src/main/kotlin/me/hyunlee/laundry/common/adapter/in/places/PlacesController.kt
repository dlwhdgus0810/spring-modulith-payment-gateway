package me.hyunlee.laundry.common.adapter.`in`.places

import me.hyunlee.laundry.common.adapter.`in`.web.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import me.hyunlee.laundry.common.adapter.out.places.GooglePlacesClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/places")
class PlacesController(
    private val places: GooglePlacesClient,
) {

    // GET /api/places/autocomplete?input=...&sessiontoken=...
    @GetMapping("/autocomplete")
    @Operation(summary = "autocompletePlaces")
    fun autocomplete(
        @RequestParam("input") input: String,
        @RequestParam("sessiontoken", required = false) sessionToken: String?,
    ): ResponseEntity<ApiResponse<Map<String, Any?>>> {
        if (input.isBlank() || input.trim().length < 3) {
            return ApiResponse.success(mapOf("predictions" to emptyList<Any>()))
        }
        val json = places.autocomplete(input.trim(), sessionToken)
        return ApiResponse.success(json)
    }

    // GET /api/places/details?place_id=...&sessiontoken=...
    @GetMapping("/details")
    @Operation(summary = "getPlaceDetails")
    fun details(
        @RequestParam("place_id") placeId: String,
        @RequestParam("sessiontoken", required = false) sessionToken: String?,
    ): ResponseEntity<ApiResponse<Map<String, Any?>>> {
        val json = places.details(placeId, sessionToken)
        return ApiResponse.success(json)
    }
}