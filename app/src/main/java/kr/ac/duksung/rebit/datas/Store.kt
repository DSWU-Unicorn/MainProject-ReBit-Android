package kr.ac.duksung.rebit.datas

import android.os.Parcelable

//import android.os.Parcelable
import java.io.Serializable

class Store (
    val id:Int,
    val storeName: String,
    // val address: String, // 식품접객업
    val category1: String,
    val category2: String,
    val tel: String
    // 상속받는 것처럼 구현
   // ): Parcelable {
):Serializable