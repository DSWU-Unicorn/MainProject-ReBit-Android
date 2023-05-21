package kr.ac.duksung.rebit.network.dto

import com.google.gson.annotations.SerializedName

data class GetReviewCommentsVO(
    @SerializedName("storeId")
    var storeId: Long,
    @SerializedName("user_id") // get 할때
    var user:Long,
    @SerializedName("star")
    var star: Int,
    @SerializedName("photo")
    var photo: String,
    @SerializedName("commentDetail")
    var comment: String,
    @SerializedName("commentDate") // get 할때
    var date: String
)