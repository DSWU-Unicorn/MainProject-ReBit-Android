package kr.ac.duksung.rebit.network.dto

import com.google.gson.annotations.SerializedName

data class StoreMarkerVO(
    @SerializedName("id")
    val id : Long,

    @SerializedName("address")
    val address : String,
)
