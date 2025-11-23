package me.hyunlee.laundry.common.adapter.out.places

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@ConfigurationProperties(prefix = "places")
data class PlacesConfigProps(
    var apiKey: String
)

@Component
@EnableConfigurationProperties(PlacesConfigProps::class)
class GooglePlacesClient(
    private val placesProps: PlacesConfigProps,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val rest = RestTemplate()

    private val base = "https://maps.googleapis.com/maps/api/place"

    fun autocomplete(input: String, sessionToken: String?): Map<String, Any?> {
        ensureConfigured()

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("input", input)
        params.add("key", placesProps.apiKey)
        params.add("language", "en")
        params.add("components", "country:us")
        params.add("types", "address")
        if (!sessionToken.isNullOrBlank()) params.add("sessiontoken", sessionToken)

        val uri = UriComponentsBuilder.fromUriString("$base/autocomplete/json")
            .queryParams(params)
            .build(true)
            .toUri()

        val res: ResponseEntity<Map<*, *>> = rest.getForEntity(uri, Map::class.java)
        @Suppress("UNCHECKED_CAST")
        return (res.body as Map<String, Any?>?) ?: emptyMap()
    }

    fun details(placeId: String, sessionToken: String?): Map<String, Any?> {
        ensureConfigured()

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("place_id", placeId)
        params.add("key", placesProps.apiKey)
        params.add("language", "en")
        params.add("fields", "address_component")
        if (!sessionToken.isNullOrBlank()) params.add("sessiontoken", sessionToken)

        val uri = UriComponentsBuilder.fromUriString("$base/details/json")
            .queryParams(params)
            .build(true)
            .toUri()

        val res: ResponseEntity<Map<*, *>> = rest.getForEntity(uri, Map::class.java)
        @Suppress("UNCHECKED_CAST")
        return (res.body as Map<String, Any?>?) ?: emptyMap()
    }

    private fun ensureConfigured() {
        if (placesProps.apiKey.isBlank()) {
            log.error("Google Places API key is not configured. Set property 'google.places.api-key'.")
            throw IllegalStateException("Google Places API key is not configured")
        }
    }
}