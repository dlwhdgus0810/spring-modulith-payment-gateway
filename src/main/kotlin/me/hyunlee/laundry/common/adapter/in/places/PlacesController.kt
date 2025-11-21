package me.hyunlee.laundry.common.adapter.`in`.places

import me.hyunlee.laundry.common.adapter.`in`.web.ApiResponse
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
    fun autocomplete(
        @RequestParam("input") input: String,
        @RequestParam("sessiontoken", required = false) sessionToken: String?,
    ): ResponseEntity<ApiResponse<Any>> {
        if (input.isBlank() || input.trim().length < 3) {
            return ApiResponse.success(mapOf("predictions" to emptyList<Any>()))
        }
        val json = places.autocomplete(input.trim(), sessionToken)
        return ApiResponse.success(json)
    }

    // GET /api/places/details?place_id=...&sessiontoken=...
    @GetMapping("/details")
    fun details(
        @RequestParam("place_id") placeId: String,
        @RequestParam("sessiontoken", required = false) sessionToken: String?,
    ): ResponseEntity<ApiResponse<Any>> {
        val json = places.details(placeId, sessionToken)
        return ApiResponse.success(json)
    }
}