package kr.ac.duksung.rebit

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.review_detail_item.*
import kr.ac.duksung.rebit.R.drawable.sit_dagom_icon
import kr.ac.duksung.rebit.network.RetofitClient
import kr.ac.duksung.rebit.network.RetrofitService
import kr.ac.duksung.rebit.network.dto.ApiResponse
import kr.ac.duksung.rebit.network.dto.GetReviewCommentsVO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
// ReviewDetailActivity 클래스
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_create_review.view.*
import kotlinx.android.synthetic.main.multi_image_item.*
import kr.ac.duksung.rebit.R.layout.activity_review_detail
import kr.ac.duksung.rebit.databinding.ReviewDetailItemBinding

class ReviewDetailActivity : AppCompatActivity() {
    private lateinit var retrofit: Retrofit
    private lateinit var retrofitService: RetrofitService

    //리뷰를 여러 개 받아오고 모두 처리하기 위해
    private lateinit var recyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_review_detail) // xml change

        recyclerView = findViewById(R.id.reviewRecyclerView)

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        reviewAdapter = ReviewAdapter()
        recyclerView.adapter = reviewAdapter


        //서버 연결
        initRetrofit()

        //통신
        getReviewComments()

    } // OnCreate()

    //서버 연결
    private fun initRetrofit() {
        retrofit = RetofitClient.getInstance()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    // 리뷰 정보
    fun getReviewComments() {
        //enqueue : 비동기식 통신을 할 때 사용/ execute: 동기식
//        val data = intent.getStringExtra("store_id")
//        val storeId = Integer.parseInt(data)
        if (intent.hasExtra("store_id")) {
            val data = intent.getStringExtra("store_id")
            //val photoUrl = intent.getStringExtra("photo_url")
            val rand = data!!.toInt()

            retrofitService.getReviewComments(rand.toLong()).enqueue(object :
                Callback<ApiResponse<ArrayList<GetReviewCommentsVO>>> {
                override fun onResponse(
                    call: Call<ApiResponse<ArrayList<GetReviewCommentsVO>>>,
                    response: Response<ApiResponse<ArrayList<GetReviewCommentsVO>>>,
                ) {
                    if (response.isSuccessful) {
                        // 통신 성공시
                        val result: ApiResponse<ArrayList<GetReviewCommentsVO>>? = response.body()
                        val reviews = result?.getResult()

                        Log.d("getReviewComments", "on response 성공: " + result?.toString())
                        Log.d("getReviewComments", "data : " + reviews?.toString())


                        if (reviews != null && reviews.isNotEmpty()) {
                            reviewAdapter.setReviews(reviews)

//                            val image = findViewById<ImageView>(R.id.imageView) // img 기능 fix 필요
//                            val name = findViewById<TextView>(R.id.userNameTextArea)
//
//                            val date = findViewById<TextView>(R.id.reviewDateTv)
//                            val reviewText = findViewById<TextView>(R.id.reviewTv)
//                            val star = findViewById<RatingBar>(R.id.starAvgRb)

                            for (review in reviews) {
                                val photoUrl = review.photo
//                                Log.d("getReviewComments", "photoUrl: $photoUrl")

                                reviewAdapter.setReviews(reviews)

                                val itemBinding = ReviewDetailItemBinding.inflate(layoutInflater)
                                itemBinding.userNameTextArea.text = review.user.toString()
                                itemBinding.starAvgRb.rating = review.star.toFloat()
                                itemBinding.reviewDateTv.text = review.date
                                itemBinding.reviewTv.text = review.comment

                                if (!photoUrl.isNullOrEmpty()) {
                                    Glide.with(this@ReviewDetailActivity)
                                        .load(photoUrl)
                                        .error(sit_dagom_icon)
                                        .into(itemBinding.imageView)
                                } else {
                                    itemBinding.imageView.setImageResource(sit_dagom_icon)
                                }

                                reviewAdapter.notifyItemInserted(reviews.indexOf(review))
                            }


                        }


                    }

                }

                override fun onFailure(
                    call: Call<ApiResponse<ArrayList<GetReviewCommentsVO>>>,
                    t: Throwable,
                ) {
                    Log.e("getReviewComments", "onFailure : ${t.message} ")
                }
            })
        }else
    {
        Toast.makeText(this, "store_id 값이 전달되지 않았습니다.", Toast.LENGTH_SHORT).show()
    }
    }

}

