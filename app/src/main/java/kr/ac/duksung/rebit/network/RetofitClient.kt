package kr.ac.duksung.rebit.network

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetofitClient {
    private var instance : Retrofit? = null
    private val gson = GsonBuilder().setLenient().create()
    //서버 주소
    private const val BASE_URL = "http://43.200.9.47:8080/"
    //private const val BASE_URL = "http://10.0.2.2:8080/"

    //SingleTon
    fun getInstance():Retrofit {
        if (instance == null) {
            instance = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        return instance!!
    }
}