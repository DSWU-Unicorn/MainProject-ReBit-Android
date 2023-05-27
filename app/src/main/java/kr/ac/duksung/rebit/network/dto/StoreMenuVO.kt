package kr.ac.duksung.rebit.network.dto

import com.google.gson.annotations.SerializedName

data class StoreMenuVO(
    @SerializedName("menuList")
    val menu : String
)
