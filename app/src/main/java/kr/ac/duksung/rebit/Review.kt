package kr.ac.duksung.rebit

import java.text.SimpleDateFormat
import java.util.Arrays.toString

//var currentTime : Long = System.currentTimeMillis()
//val dataFormat = SimpleDateFormat("yy-MM-dd-E") // 년(20XX) 월 일 요일
//val date = dataFormat.format(currentTime).toString()

// 클래스 모델 객체
class Review(val image:Int, val name: String, val date: String, val review_text: String)