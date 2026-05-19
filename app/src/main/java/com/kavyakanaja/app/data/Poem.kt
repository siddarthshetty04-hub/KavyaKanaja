package com.kavyakanaja.app.data

import com.google.gson.annotations.SerializedName

data class Poem(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("poet") val poet: String,
    @SerializedName("lines") val lines: List<String>,
    @SerializedName("bhavartha_kannada") val bhavarthaKannada: String,
    @SerializedName("english_translation") val englishTranslation: String,
    @SerializedName("word_meanings") val wordMeanings: Map<String, String>,
    @SerializedName("audio_url") val audioUrl: String
)
