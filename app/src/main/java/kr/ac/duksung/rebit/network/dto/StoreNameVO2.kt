package kr.ac.duksung.rebit.network.dto

import com.google.gson.annotations.SerializedName

data class StoreNameVO2(
    @SerializedName("storeName")
    val storeName : String,

    @SerializedName("storeIid")
    val storeId : Int
)
