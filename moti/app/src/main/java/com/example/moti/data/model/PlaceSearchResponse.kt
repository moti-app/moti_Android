package com.example.moti.data.model

import com.google.gson.annotations.SerializedName

data class PlaceSearchResponse(
    val results: List<PlaceResult>,
    val status: String
)

data class PlaceResult(
    @SerializedName("formatted_address")
    val formattedAddress: String,
    val geometry: Geometry,
    val icon: String,
    @SerializedName("icon_background_color")
    val iconBackgroundColor: String,
    @SerializedName("icon_mask_base_uri")
    val iconMaskBaseUri: String,
    val name: String,
    @SerializedName("place_id")
    val placeId: String,
    @SerializedName("plus_code")
    val plusCode: PlusCode,
    val reference: String,
    val types: List<String>
)

data class Geometry(
    val location: Location,
    val viewport: Viewport
)

data class Location(
    val lat: Double,
    val lng: Double
)

data class Viewport(
    val northeast: Location,
    val southwest: Location
)

data class PlusCode(
    @SerializedName("compound_code")
    val compoundCode: String,
    @SerializedName("global_code")
    val globalCode: String
)
