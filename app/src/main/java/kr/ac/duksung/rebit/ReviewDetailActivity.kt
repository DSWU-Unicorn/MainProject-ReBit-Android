package kr.ac.duksung.rebit

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_store_detail.*
import kotlinx.android.synthetic.main.activity_togo.*
import kotlinx.android.synthetic.main.multi_image_item.*
import kr.ac.duksung.rebit.R.drawable.sit_dagom_icon
import kr.ac.duksung.rebit.network.RetofitClient
import kr.ac.duksung.rebit.network.RetrofitService
import kr.ac.duksung.rebit.network.dto.ApiResponse
import kr.ac.duksung.rebit.network.dto.GetReviewCommentsVO
import kr.ac.duksung.rebit.network.dto.ReviewCommentsVO
import kr.ac.duksung.rebit.network.dto.StoreNameVO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class ReviewDetailActivity : AppCompatActivity() {
    private lateinit var retrofit: Retrofit
    private lateinit var retrofitService: RetrofitService



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.review_detail_item) // xml change



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
        val storeId = intent.getLongExtra("store_id",357)


        retrofitService.getReviewComments(storeId)?.enqueue(object :
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

                    val image = findViewById<ImageView>(R.id.imageView) // img 기능 fix 필요
                    val name = findViewById<TextView>(R.id.userNameTextArea)
                    val date = findViewById<TextView>(R.id.reviewDateTv)
                    val reviewText = findViewById<TextView>(R.id.reviewTv)
                    val star = findViewById<TextView>(R.id.starAvgTv)

                    if (reviews != null) {

                        for (review in reviews) {
                            // 화면에 데이터 뿌린다.
                            // review 사진
                            if(review!=null){
                                val photoUrl = review.photo
                                if (photoUrl != null) {
                                    Glide.with(this@ReviewDetailActivity).load(photoUrl)
                                        .error(sit_dagom_icon).into(image)
                                } else image.setImageResource(sit_dagom_icon)

                                // user 이름
                                name.text = review!!.user.toString()
                                // star 별점
                                star.text = review!!.star.toString()
                                // date 날짜
                                date.text = review!!.date
                                // review text
                                reviewText.text = review!!.comment
                            }
                            else{
                                reviewText.text = "리뷰 정보가 없습니다"
                            }

                        }
                    }

                }
            }

            override fun onFailure(
                call: Call<ApiResponse<ArrayList<GetReviewCommentsVO>>>,
                t: Throwable,
            ) {
                TODO("Not yet implemented")
            }
        })
    }
}