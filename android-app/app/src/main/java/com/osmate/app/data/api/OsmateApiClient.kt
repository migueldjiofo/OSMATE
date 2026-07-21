package com.osmate.app.data.api

import com.osmate.app.BuildConfig
import com.osmate.app.data.model.RoutingResult
import com.osmate.app.data.model.SearchPlan
import com.osmate.app.data.model.SearchResult
import com.osmate.app.data.model.SearchResultItem
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class OsmateApiClient(
    private val baseUrl: String = BuildConfig.OSMATE_API_BASE_URL,
) {
    suspend fun getBackendHealth(): String = withContext(Dispatchers.IO) {
        val response = executeRequest(
            path = "/api/v1/health",
            method = "GET",
        )

        val json = JSONObject(response)
        "${json.optString("status")} - ${json.optString("service")}"
    }

    suspend fun search(
        query: String,
		placeName: String?,
		latitude: Double?,
		longitude: Double?,
		radiusMeters: Int,
		limit: Int,
    ): SearchResult = withContext(Dispatchers.IO) {
        val requestBody = JSONObject().apply {
			put("query", query)
			put("radius_m", radiusMeters)
			put("limit", limit)
			put("language", "de")

			if (latitude != null && longitude != null) {
				put("lat", latitude)
				put("lon", longitude)
			} else if (!placeName.isNullOrBlank()) {
				put("place_name", placeName)
			}
		}

        val response = executeRequest(
            path = "/api/v1/search",
            method = "POST",
            body = requestBody,
        )

        parseSearchResult(JSONObject(response))
    }


    suspend fun calculateRoute(
        startLatitude: Double,
        startLongitude: Double,
        destinationLatitude: Double,
        destinationLongitude: Double,
        mode: String,
    ): RoutingResult = withContext(Dispatchers.IO) {
        val requestBody = JSONObject()
            .put("start_lat", startLatitude)
            .put("start_lon", startLongitude)
            .put("destination_lat", destinationLatitude)
            .put("destination_lon", destinationLongitude)
            .put("mode", mode)

        val response = executeRequest(
            path = "/api/v1/routing/route",
            method = "POST",
            body = requestBody,
        )

        parseRoutingResult(JSONObject(response))
    }
    private fun executeRequest(
        path: String,
        method: String,
        body: JSONObject? = null,
    ): String {
        val url = URL("${baseUrl.trimEnd('/')}$path")
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = method
            connection.connectTimeout = 10_000
            connection.readTimeout = 45_000
            connection.setRequestProperty("Accept", "application/json")

            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.outputStream.use { outputStream ->
                    outputStream.write(body.toString().toByteArray(Charsets.UTF_8))
                }
            }

            val responseText = readResponse(connection)

            if (connection.responseCode !in 200..299) {
                throw OsmateApiException("HTTP ${connection.responseCode}: $responseText")
            }

            return responseText
        } finally {
            connection.disconnect()
        }
    }

    private fun readResponse(connection: HttpURLConnection): String {
        val stream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        return stream?.bufferedReader(Charsets.UTF_8)?.use { reader ->
            reader.readText()
        }.orEmpty()
    }


    private fun parseRoutingResult(json: JSONObject): RoutingResult {
        val geoJsonValue = json.opt("geojson")

        val geoJson = when (geoJsonValue) {
            is JSONObject -> geoJsonValue.toString()
            is String -> geoJsonValue
            else -> emptyFeatureCollection()
        }

        return RoutingResult(
            mode = json.optString("mode"),
            provider = json.optString("provider"),
            distanceMeters = json.optInt("distance_m"),
            durationSeconds = json.optInt("duration_s"),
            distanceText = json.optString("distance_text"),
            durationText = json.optString("duration_text"),
            geoJson = geoJson,
            warnings = jsonArrayToList(json.optJSONArray("warnings")),
        )
    }
    private fun parseSearchResult(json: JSONObject): SearchResult {
        val geoJsonValue = json.opt("geojson")

        val geoJson = when (geoJsonValue) {
            is JSONObject -> geoJsonValue.toString()
            is String -> geoJsonValue
            else -> emptyFeatureCollection()
        }

        return SearchResult(
            plan = parseSearchPlan(json.getJSONObject("agent_plan")),
            resultCount = json.optInt("result_count"),
            geoJson = geoJson,
            items = parseResultItems(geoJson),
        )
    }

    private fun parseResultItems(geoJson: String): List<SearchResultItem> {
        val root = runCatching {
            JSONObject(geoJson)
        }.getOrNull() ?: return emptyList()

        val features = root.optJSONArray("features") ?: return emptyList()
        val items = mutableListOf<SearchResultItem>()

        for (index in 0 until features.length()) {
            val feature = features.optJSONObject(index) ?: continue
            val properties = feature.optJSONObject("properties") ?: JSONObject()
            val geometry = feature.optJSONObject("geometry") ?: JSONObject()

            val title = readTitle(
                properties = properties,
                fallbackIndex = index + 1,
            )

            val primaryTag = readPrimaryTag(properties)
            val subtitle = readSubtitle(properties)
            val openingHours = properties.optString("opening_hours").trim()
            val coordinate = readPointCoordinate(geometry)
            val allTags = jsonObjectToMap(properties)

            items += SearchResultItem(
                title = title,
                subtitle = subtitle,
                primaryTag = primaryTag,
                openingHours = openingHours,
                latitude = coordinate?.first,
                longitude = coordinate?.second,
                cuisine = properties.optString("cuisine").trim(),
                website = properties.optString("website").trim().ifBlank {
                    properties.optString("contact:website").trim()
                },
                phone = properties.optString("phone").trim().ifBlank {
                    properties.optString("contact:phone").trim()
                },
                brand = properties.optString("brand").trim(),
                operator = properties.optString("operator").trim(),
                outdoorSeating = properties.optString("outdoor_seating").trim(),
                internetAccess = properties.optString("internet_access").trim(),
                wheelchair = properties.optString("wheelchair").trim(),
                takeaway = properties.optString("takeaway").trim(),
                allTags = allTags,
            )
        }

        return items
    }

    private fun readTitle(
        properties: JSONObject,
        fallbackIndex: Int,
    ): String {
        val candidates = listOf(
            properties.optString("name").trim(),
            properties.optString("brand").trim(),
            properties.optString("operator").trim(),
        )

        candidates.forEach { value ->
            if (value.isNotBlank()) {
                return value
            }
        }

        return "OSM-Objekt $fallbackIndex"
    }

    private fun readPrimaryTag(properties: JSONObject): String {
        val tagKeys = listOf(
            "amenity",
            "shop",
            "leisure",
            "tourism",
            "public_transport",
            "highway",
            "cuisine",
        )

        tagKeys.forEach { key ->
            val value = properties.optString(key).trim()

            if (value.isNotBlank()) {
                return "$key=$value"
            }
        }

        return "OSM-Objekt"
    }

    private fun readSubtitle(properties: JSONObject): String {
        val parts = mutableListOf<String>()

        addTagIfPresent(properties, "amenity", parts)
        addTagIfPresent(properties, "shop", parts)
        addTagIfPresent(properties, "leisure", parts)
        addTagIfPresent(properties, "tourism", parts)
        addTagIfPresent(properties, "cuisine", parts)

        if (properties.optString("outdoor_seating").trim() == "yes") {
            parts += "Terrasse"
        }

        if (properties.optString("wheelchair").trim() == "yes") {
            parts += "barrierearm"
        }

        return if (parts.isEmpty()) {
            "OpenStreetMap-Objekt"
        } else {
            parts.joinToString(" | ")
        }
    }

    private fun addTagIfPresent(
        properties: JSONObject,
        key: String,
        parts: MutableList<String>,
    ) {
        val value = properties.optString(key).trim()

        if (value.isNotBlank()) {
            parts += "$key=$value"
        }
    }

    private fun readPointCoordinate(geometry: JSONObject): Pair<Double, Double>? {
        if (geometry.optString("type") != "Point") {
            return null
        }

        val coordinates = geometry.optJSONArray("coordinates") ?: return null
        val longitude = coordinates.optDouble(0, Double.NaN)
        val latitude = coordinates.optDouble(1, Double.NaN)

        if (latitude.isNaN() || longitude.isNaN()) {
            return null
        }

        return latitude to longitude
    }

    private fun parseSearchPlan(json: JSONObject): SearchPlan {
        val spatialFilter = json.optJSONObject("spatial_filter") ?: JSONObject()

        return SearchPlan(
            category = json.optString("category"),
            osmTags = jsonObjectToMap(json.optJSONObject("osm_tags") ?: JSONObject()),
            radiusM = spatialFilter.optInt("radius_m"),
            confidenceScore = json.optDouble("confidence_score"),
            plannerType = json.optString("planner_type"),
            summary = json.optString("summary_de"),
            warnings = jsonArrayToList(json.optJSONArray("warnings")),
        )
    }

    private fun jsonObjectToMap(json: JSONObject): Map<String, String> {
        val result = mutableMapOf<String, String>()

        json.keys().forEach { key ->
            result[key] = json.optString(key)
        }

        return result
    }

    private fun jsonArrayToList(jsonArray: org.json.JSONArray?): List<String> {
        if (jsonArray == null) {
            return emptyList()
        }

        return List(jsonArray.length()) { index ->
            jsonArray.optString(index)
        }
    }

    private fun emptyFeatureCollection(): String {
        return """{"type":"FeatureCollection","features":[]}"""
    }
}

class OsmateApiException(message: String) : RuntimeException(message)