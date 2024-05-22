package com.example.moti.data.model

import com.google.gson.annotations.SerializedName

data class PlaceAutocompleteResponse(
    @SerializedName("predictions")
    val predictions: List<Prediction>
)

data class Prediction(
    @SerializedName("place_id")
    val placeId: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("structured_formatting")
    val structuredFormatting: StructuredFormatting
)

data class StructuredFormatting(
    @SerializedName("main_text")
    val mainText: String,
    @SerializedName("secondary_text")
    val secondaryText: String
)
