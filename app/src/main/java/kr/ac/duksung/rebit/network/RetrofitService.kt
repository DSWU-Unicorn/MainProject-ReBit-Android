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


    @GET("/store/markInfo/{search}") // 가게 마커 클릭시 말풍선 정보 조회
    fun getMarkerInfo(@Path("search") search: String): Call<ApiResponse<MarkerInfoVO>>

    @GET("/store")
    fun getStoreAll() : Call<ApiResponse<ArrayList<StoreNameVO>>>

    @GET("/store/takeout/{store_id}")
    fun getStoreAddressTogo(@Path("store_id") search: Int) : Call<ApiResponse<StoreAddressVO>>

    @POST("/store/takeout/{user_id}")
    fun postUserWithPointAfterYonggi(@Path("user_id") id: Int): Call<ApiResponse<Int>>

    @GET("/store/searchName/{search}")
    fun searchStoreByName(@Path("search") search: String) : Call<ApiResponse<ArrayList<StoreNameVO2>>>

    // 가게 review 가져옴
    @GET("/store/reviews/{store_id}")
    fun getReviewComments(@Path("store_id") store_id: Long): Call<ApiResponse<ArrayList<GetReviewCommentsVO>>>

    // 가게 메뉴판 조회
    @GET("/menu/{store_id}")
    fun getStoreMenu(@Path("store_id") store_id: Long): Call<ApiResponse<StoreMenuVO>>

    // 플라스크 서버
    @GET("/yongginae/{file_name_1},{file_name_2}/{store_type}")
    fun getYongginaeResult(@Path("file_name_1") file_name_1: String, @Path("file_name_2") file_name_2: String, @Path("store_type") store_type: String)
        : Call<ApiResponse<String>>
}