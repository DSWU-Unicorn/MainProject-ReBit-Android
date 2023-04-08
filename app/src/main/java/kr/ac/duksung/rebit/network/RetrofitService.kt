package kr.ac.duksung.rebit.network

import kr.ac.duksung.rebit.network.dto.ApiResponse
import kr.ac.duksung.rebit.network.dto.CardNewsVO
import kr.ac.duksung.rebit.network.dto.RecycleDetailVO
import kr.ac.duksung.rebit.network.dto.RecycleVO
import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.Path

interface RetrofitService {
    @GET("/cardnews/{date}")
    fun getCardNews(@Path("date") date : String) : Call<ApiResponse<ArrayList<CardNewsVO>>>

    @GET("/recycle/{data-label}")
    fun getRecycle(@Path("data-label") dataLabel : String) : Call<ApiResponse<RecycleVO>>

    @GET("/recycle/region/{region}")
    fun getRecycleDetailByRegion(@Path("region") region : String) : Call<ApiResponse<RecycleDetailVO>>



}