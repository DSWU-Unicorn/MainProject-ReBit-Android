package kr.ac.duksung.rebit.network.dto

import com.google.gson.annotations.SerializedName

data class ReviewCommentsVO(
    @SerializedName("storeId")
    var storeId: Long,
    @SerializedName("userId")
    var userId: Long,
    @SerializedName("star")
    var star: Int,
    @SerializedName("photo")
    var photo: String,
    @SerializedName("commentDetail")
    var comment: String
)