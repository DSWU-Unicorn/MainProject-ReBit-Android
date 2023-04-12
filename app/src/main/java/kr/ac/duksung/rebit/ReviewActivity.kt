package kr.ac.duksung.rebit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import androidx.databinding.DataBindingUtil
import androidx.databinding.DataBindingUtil.setContentView
import kotlinx.android.synthetic.main.activity_main.*
import kr.ac.duksung.rebit.databinding.ActivityReviewBinding
import kr.ac.duksung.rebit.databinding.ActivityTogoBinding

class ReviewActivity : AppCompatActivity() {
    private lateinit var binding : ActivityReviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_review)

        val showAllReviewBtn = findViewById<Button>(R.id.showAllReviewBtn)


        var reviewList = arrayListOf<Review>(
            Review(
                R.drawable.review_0,
                "홍길동",
                "date",
                "간편하게 배달이나 포장으로 음식을 먹고 나면 쓰레기가 쌓입니다. 쓰레기를 만드는 대신 가지고 있는 용기를 가져가 음식을 받아오는 ‘용기내’ 챌린지, 요즘 많이들 하죠."
            ),
            Review(
                R.drawable.review_1,
                "심청이",
                "date",
                "연희동에선 두부도 플라스틱 통 대신 용기에 담아올 수 있었습니다. 플라스틱 쓰레기가 많이 생기는 밀키트를 사는 대신 직접 가져간 용기에 내용물만 담아왔고, 쌀가게에선 가져간 용기에 필요한 만큼만 쌀을 담아왔습니다."
            ),
        )

        showAllReviewBtn.setOnClickListener{
            val intent = Intent(this, ReviewDetailActivity::class.java)
            startActivity(intent)
        }
//        binding.showAllReviewBtn.setOnClickListener {
//            val intent = Intent(this, ReviewDetailActivity::class.java)
//            startActivity(intent)
//        }
        val Adapter = ListViewReviewAdapter(this, reviewList)
        val listView = findViewById<ListView>(R.id.listView)

        listView.adapter=Adapter
    }
}