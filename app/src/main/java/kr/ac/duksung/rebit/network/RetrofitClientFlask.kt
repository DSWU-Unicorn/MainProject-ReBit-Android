package kr.ac.duksung.rebit.network

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClientFlask {
    private var instance : Retrofit? = null
    private val gson = GsonBuilder().setLenient().create()
    //서버 주소
    private const val BASE_URL = "http://43.200.9.47:5000/"

    //SingleTon
    fun getInstance(): Retrofit {
        if (instance == null) {
            instance = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        return instance!!
    }
}