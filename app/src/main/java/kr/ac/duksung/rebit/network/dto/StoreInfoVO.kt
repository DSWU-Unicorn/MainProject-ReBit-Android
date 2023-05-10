package kr.ac.duksung.rebit.network.dto

import com.google.gson.annotations.SerializedName
import kr.ac.duksung.rebit.Review

data class StoreInfoVO(
    @SerializedName("storeName")
    val storeName : String,
    @SerializedName("address")
    val address : String,
    @SerializedName("category2")
    val category : String,
    @SerializedName("tel")
    val tel : String,
    @SerializedName("store_photo")
    val store_photo : String,
    @SerializedName("reviewSearchResList")
    val reviewList : List<Review>
)

data class Review(
    @SerializedName("starAvg")
    val starAvg: Int,
    @SerializedName("reviewNum")
    val reviewNum: Int
)