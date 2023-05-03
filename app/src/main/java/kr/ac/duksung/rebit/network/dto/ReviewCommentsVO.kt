package kr.ac.duksung.rebit.network.dto

import com.google.gson.annotations.SerializedName
/*
작성 예시 (body에 전달)
{
"storeId" : 10,
"userId": 1,
"star" : 1,
"photo" : "aws.s3.dwmkdqkldmm",
"commentDetail" : "test..."
}
*/
data class ReviewCommentsVO(
    @SerializedName("storeId")
    val storeId: Long,
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("star")
    val star: Int,
    @SerializedName("photo")
    val photo: String,
    @SerializedName("commentDetail")
    val commentDetail: String,
)