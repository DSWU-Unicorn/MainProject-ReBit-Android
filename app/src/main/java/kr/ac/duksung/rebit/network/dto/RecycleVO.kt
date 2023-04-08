package kr.ac.duksung.rebit.network.dto

import com.google.gson.annotations.SerializedName

data class RecycleVO(
    @SerializedName("id")
    val id : Long,

    @SerializedName("dataLabel")
    val dataLabel : String,

    @SerializedName("content")
    val content : String
)
