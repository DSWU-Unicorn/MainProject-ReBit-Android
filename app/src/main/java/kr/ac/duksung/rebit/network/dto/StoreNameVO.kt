package kr.ac.duksung.rebit.network.dto

import com.google.gson.annotations.SerializedName

data class StoreNameVO(
    @SerializedName("storeName")
    val storeName : String,

    @SerializedName("storeId")
    val storeId : Int
)
