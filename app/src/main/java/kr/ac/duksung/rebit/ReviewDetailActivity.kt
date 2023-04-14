package kr.ac.duksung.rebit

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class ReviewDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_detail)


        var ReviewLists = arrayListOf<Review>(
            Review(
                R.drawable.review_0,
                "user1",
                "2023.04.10",
                "굿"
            ),
            Review(
                R.drawable.review_1,
                "u__j",
                "2023.04.09",
                "용기 내기 좋네요."
            ),
            Review(
                R.drawable.review_2,
                "silver_rain",
                "2023.04.09",
                "용기내 챌린지는 음식 배달 및 포장으로 발생하는 불필요한 쓰레기를 줄이자는 취지에서 시작된 운동인거 아시죠? 용기내 챌린지는 이름대로 용기가 필요한거 같네요 "
            ),
            Review(
                R.drawable.review_3,
                "oownoey",
                "2023.04.08",
                "좋아요"
            ),
            Review(
                R.drawable.review_4,
                "allzeroyou",
                "2023.04.08",
                "오 생각보다 용기내기 쉽네요"
            ),
            Review(
                R.drawable.review_0,
                "izeho",
                "2023.04.07",
                "굿잡"
            )
        )
        val Adapter = ListViewReviewAdapter(this, ReviewLists)
        val listView = findViewById<ListView>(R.id.listView)

        listView.adapter = Adapter
    }
}