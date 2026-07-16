from app.services.geojson_service import convert_overpass_to_geojson


def test_convert_overpass_payload_to_geojson_feature_collection() -> None:
    payload = {
        "elements": [
            {
                "type": "node",
                "id": 1,
                "lat": 52.52,
                "lon": 13.405,
                "tags": {
                    "amenity": "cafe",
                    "name": "Test Cafe",
                },
            },
            {
                "type": "way",
                "id": 2,
                "center": {
                    "lat": 52.521,
                    "lon": 13.406,
                },
                "tags": {
                    "amenity": "cafe",
                    "name": "Way Cafe",
                },
            },
        ]
    }

    geojson = convert_overpass_to_geojson(payload)

    assert geojson["type"] == "FeatureCollection"
    assert len(geojson["features"]) == 2
    assert geojson["features"][0]["geometry"]["type"] == "Point"
    assert geojson["features"][0]["geometry"]["coordinates"] == [13.405, 52.52]
    assert geojson["features"][0]["properties"]["name"] == "Test Cafe"
