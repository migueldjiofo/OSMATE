package com.osmate.app.data.api

import com.osmate.app.BuildConfig
import com.osmate.app.data.model.SearchPlan
import com.osmate.app.data.model.SearchResult
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

    suspend fun createSearchPlan(
        query: String,
        placeName: String,
        radiusM: Int,
    ): SearchPlan = withContext(Dispatchers.IO) {
        val requestBody = createSearchRequestBody(
            query = query,
            placeName = placeName,
            radiusM = radiusM,
        )

        val response = executeRequest(
            path = "/api/v1/search/plan",
            method = "POST",
            body = requestBody,
        )

        parseSearchPlan(JSONObject(response))
    }

    suspend fun search(
        query: String,
        placeName: String,
        radiusM: Int,
    ): SearchResult = withContext(Dispatchers.IO) {
        val requestBody = createSearchRequestBody(
            query = query,
            placeName = placeName,
            radiusM = radiusM,
        )

        val response = executeRequest(
            path = "/api/v1/search",
            method = "POST",
            body = requestBody,
        )

        parseSearchResult(JSONObject(response))
    }

    private fun createSearchRequestBody(
        query: String,
        placeName: String,
        radiusM: Int,
    ): JSONObject {
        return JSONObject()
            .put("query", query)
            .put("place_name", placeName)
            .put("radius_m", radiusM)
            .put("limit", 30)
            .put("language", "de")
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
                throw OsmateApiException(
                    "HTTP ${connection.responseCode}: $responseText",
                )
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
        )
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
