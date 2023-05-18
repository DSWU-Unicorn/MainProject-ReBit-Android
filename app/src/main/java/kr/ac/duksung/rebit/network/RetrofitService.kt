package kr.ac.duksung.rebit.network

import kr.ac.duksung.rebit.network.dto.*
import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface RetrofitService {
    @GET("/cardnews/{date}")
    fun getCardNews(@Path("date") date: String): Call<ApiResponse<ArrayList<CardNewsVO>>>

    @GET("/recycle/{data-label}")
    fun getRecycle(@Path("data-label") dataLabel: String): Call<ApiResponse<RecycleVO>>

    @GET("/recycle/region/{region}")
    fun getRecycleDetailByRegion(@Path("region") region: String): Call<ApiResponse<RecycleDetailVO>>

    @POST("/recycle/{id}")
    fun postUserPointByRecycle(@Path("id") id: Long): Call<ApiResponse<Int>>

    @POST("/tip/{id}")
    fun postUserWithPoint(@Path("id") id: Long): Call<ApiResponse<Int>>

    @GET("/store/mark/{search}")
    fun getStoreMarker(@Path("search") search: String): Call<ApiResponse<ArrayList<StoreMarkerVO>>>

    @POST("/comments") // 작성한 리뷰를 서버에 저장
    fun postReviewComments(@Body review: ReviewCommentsVO ): Call<ApiResponse<Int>>

    @GET("/store/info/{store_id}") // 가게 상세 정보 조회
    fun getStoreInfo(@Path("store_id") search: Long): Call<ApiResponse<StoreInfoVO>>


    @GET("/store/markInfo/{search}") // 가게 상세 정보 조회
    fun getMarkerInfo(@Path("search") search: String): Call<ApiResponse<MarkerInfoVO>>

    @GET("/store")
    fun getStoreAll() : Call<ApiResponse<ArrayList<StoreNameVO>>>

    // 가게 review 가져옴
    @GET("/store/reviews/{store_id}")
    fun getReviewComments(@Path("store_id") store_id: Long): Call<ApiResponse<ArrayList<GetReviewCommentsVO>>>
}