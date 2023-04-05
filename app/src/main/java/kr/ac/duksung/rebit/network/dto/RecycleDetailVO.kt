package kr.ac.duksung.rebit.network.dto

import com.google.gson.annotations.SerializedName

data class RecycleDetailVO (
    @SerializedName("id")
    val id : Long,

    @SerializedName("region")
    val region : String,

    @SerializedName("day")
    val day : String,

    @SerializedName("typicalDay")
    val typicalDay : String
    )