package kr.ac.duksung.rebit.network.dto

import com.google.gson.annotations.SerializedName

data class CardNewsVO (
    @SerializedName("id")
    val id : Long,

    @SerializedName("image_src")
    val image_src : String,

    @SerializedName("date")
    val date : String
)