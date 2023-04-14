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