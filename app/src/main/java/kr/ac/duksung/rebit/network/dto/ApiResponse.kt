package kr.ac.duksung.rebit.network.dto

import com.google.gson.annotations.SerializedName

class ApiResponse<T> {

    @SerializedName("isSuccess")
    private var isSuccess : Boolean = true

    @SerializedName("code")
    private var code : Int = 0

    @SerializedName("message")
    private lateinit var message : String

    @SerializedName("result")
    private val result: T? = null

    fun getIsSuccess() : Boolean {
        return isSuccess
    }

    fun getCode() : Int {
        return code
    }

    fun getMessage() : String {
        return message
    }

    fun getResult() : T {
        return result!!;
    }


    override fun toString() : String {
        return "isSuccess : " +  getIsSuccess() +
                "\ncode : " + getCode() +
                "\nmessage : " + getMessage() +
                "\ngetResult : " + getResult()
    }
}