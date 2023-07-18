package kr.ac.duksung.rebit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.unity3d.player.e
import kotlinx.android.synthetic.main.activity_store_detail.*
import kr.ac.duksung.rebit.databinding.ActivityStoreDetailBinding
import kr.ac.duksung.rebit.databinding.ReviewDetailItemBinding
import kr.ac.duksung.rebit.network.RetofitClient
import kr.ac.duksung.rebit.network.RetrofitService
import kr.ac.duksung.rebit.network.dto.ApiResponse
import kr.ac.duksung.rebit.network.dto.GetReviewCommentsVO
import kr.ac.duksung.rebit.network.dto.StoreInfoVO
import kr.ac.duksung.rebit.network.dto.StoreMenuVO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.text.ParseException
import java.util.regex.Pattern


class StoreDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStoreDetailBinding

    // retrofit 사용해 통신 구현
    private lateinit var retrofit: Retrofit
    private lateinit var retrofitService: RetrofitService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_detail)


        //setValues()
        setupEvents()

        //서버 연결
        initRetrofit()

        // 통신-가게 상세 정보
        getStoreInfo()

        val data = intent.getStringExtra("store_id")
        val rand = data?.let { Integer.parseInt(it) }
        Log.d("store_id", rand.toString())

        // activity_store_detail.xml에 설정했던 id 값 사용가능
        binding = ActivityStoreDetailBinding.inflate(layoutInflater)
        pic_btn.setOnClickListener {
            Toast.makeText(this, "내 용기가 맞을까? 확인하러 가기", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, YonggiCameraActivity::class.java)
            intent.putExtra("store_id", data)
            startActivity(intent)
        }


        // review view
        val goto_review_btn = findViewById<Button>(R.id.goto_review_btn)
//        goto_review_btn.setOnClickListener {
//            Toast.makeText(this, "생생한 후기가 궁금하나요? 리뷰 보러 가기", Toast.LENGTH_SHORT).show()
//            val intent = Intent(this, ReviewActivity::class.java)
//            intent.putExtra("store_id", data) // store id 값 전달.
//            startActivity(intent)
//        }
//        // review create
        val todo_btn = findViewById<Button>(R.id.todo_btn)
//        todo_btn.setOnClickListener {
//            Toast.makeText(this, "이미 용기냈다면! 어땠는지 후기 작성하러 가기", Toast.LENGTH_SHORT).show()
//            val intent = Intent(this, CreateReviewActivity::class.java)
//            intent.putExtra("store_id", data) // store id 값 전달.
//            startActivity(intent)
//        }


    }// OnCreate

    //서버 연결
    private fun initRetrofit() {
        retrofit = RetofitClient.getInstance()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    // 통신
    private fun getStoreInfo() {
        val data = intent.getStringExtra("store_id")
        val rand = Integer.parseInt(data.toString())

        gotoReviewBtn.setOnClickListener {
            val intent = Intent(this, ReviewDetailActivity::class.java) // 리뷰 보러 가기
            intent.putExtra("store_id", data)         // store_id 값을 review 로 intent 처리
            startActivity(intent)
        }
//        val todoBtn = findViewById<Button>(R.id.todo_btn)
//
//        todoBtn.setOnClickListener {
//            val intent = Intent(this, CreateReviewActivity::class.java) // 리뷰 작성
//            intent.putExtra("store_id", data)
//            startActivity(intent)
//        }

        retrofitService.getStoreInfo(rand.toLong()).enqueue(object :
            Callback<ApiResponse<StoreInfoVO>> {
            override fun onResponse(
                call: Call<ApiResponse<StoreInfoVO>>,
                response: Response<ApiResponse<StoreInfoVO>>,
            ) {
                if (response.isSuccessful) {
                    // 통신 성공시
                    val result: ApiResponse<StoreInfoVO>? = response.body()
                    val storeInfoVO = result?.getResult()

                    Log.d("info", "onresponse 성공: " + result?.toString())
                    Log.d("info", "data : " + storeInfoVO?.toString())

                    // 화면에 데이터 뿌린다.
                    // 가게 이름
                    storeNameTextArea.text =
                        storeInfoVO!!.storeName // data binding 으로 findViewById 안써도 접근 가능.
                    // 가게 주소
                    addressTxt.text = storeInfoVO.address
                    // 가게 사진
                    val photoUrl = storeInfoVO.store_photo
                    // 만약 사진이 없거나, error 시 dagom 사진 보이도록
                    Glide.with(this@StoreDetailActivity).load("https:$photoUrl")
                        .error(R.drawable.sit_dagom_icon).into(storeImageArea)
                    // 가게 카테고리
                    storeKindTextArea.text = storeInfoVO.category
                    // 가게 전화번호
                    telText.text = (storeInfoVO.tel).convertNumberToPhoneNumber()
                    // 가게 별점 평균, 가게 리뷰 수
                    try {
                        // starAvgTv.text = (storeInfoVO.reviewList[0].toString()) // 프론트단에서 별점 평균 구함
                        //Log.d("storeInfoVO","starAvg: ${starAvgTv.text}")
                        // 리뷰 데이터 통신
                        getReviewComments()
//
//                        retrofitService.getReviewComments(rand.toLong()).enqueue(object :
//                            Callback<ArrayList<GetReviewCommentsVO>> {
//                            override fun onResponse(
//                                call: Call<ApiResponse<ArrayList<GetReviewCommentsVO>>>,
//                                response: Response<ArrayList<GetReviewCommentsVO>>,
//                            ) {
//                                if (response.isSuccessful) {
//                                    // 통신 성공시
//                                    val result: ApiResponse<StoreInfoVO>? = response.body()
//                                    val storeInfoVO = result?.getResult()
//
//                                    Log.d("getReviewComments", "onresponse 성공: " + result?.toString())
//                                    Log.d("getReviewComments", "data : " + storeInfoVO?.toString())
//
//                                    // 화면에 데이터 뿌린다.
//
//                                }
//                            }
//
//                            override fun onFailure(
//                                call: Call<ArrayList<GetReviewCommentsVO>>,
//                                t: Throwable,
//                            ) {
//                                Log.e("getReviewComments", "onFailure : ${t.message} ")
//                            }
//                        })

                        // reviewNumTv.text = (storeInfoVO.reviewList[1].toString()) // 프론트단에서 리뷰 수 조회
                        //Log.d("storeInfoVO","reviewNum: ${reviewNumTv.text}")

                    } catch (e: IndexOutOfBoundsException) {
                        // e.printStackTrace()
                        starAvgTv.text = "아직 등록된 별점이 없습니다. 첫번째 별점을 달아주세요!"
                        reviewNumTv.text = "아직 등록된 리뷰가 없습니다. 첫번째 리뷰어가 되어주세요!"
                    }


                }
            }

            override fun onFailure(
                call: Call<ApiResponse<StoreInfoVO>>,
                t: Throwable,
            ) {
                Log.e("info", "onFailure : ${t.message} ")
            }
        })

        // 리뷰 작성
        val reviewStoreData = intent.getStringExtra("store_id")
        //val rand = Integer.parseInt(reviewStoredata.toString())

        val todoBtn = findViewById<Button>(R.id.todo_btn)
        todoBtn.setOnClickListener {
            val intent = Intent(this, CreateReviewActivity::class.java)
            intent.putExtra("store_id", reviewStoreData)
            startActivity(intent)
        }

        // 메뉴 통신

        retrofitService.getStoreMenu(rand.toLong())
            .enqueue(object : Callback<ApiResponse<StoreMenuVO>> {
                override fun onResponse(
                    call: Call<ApiResponse<StoreMenuVO>>,
                    response: Response<ApiResponse<StoreMenuVO>>,
                ) {
                    if (response.isSuccessful) {
                        // 통신 성공시
                        val result: ApiResponse<StoreMenuVO>? = response.body()
                        val storeMenuVO = result?.getResult()

                        Log.d("getStoreMenu", "onresponse 성공: " + result?.toString())
                        Log.d("getStoreMenu", "data : " + storeMenuVO?.toString())
                        Log.d("getStoreMenu", "store id: " + rand.toLong())

                        // 화면에 데이터 뿌린다.
                        // 가게 메뉴판
                        val menuText = storeMenuVO?.menu
                        // 받아온 데이터 조작
                        // 메뉴 가격의 콤마(,)뒤에 숫자까지 감지해서 연속한 숫자가 끝날때 한줄 break
                        val regex =
                            Regex("(\\D+)(\\d{1,3}(?:,\\d{3})*)") // 메뉴와 가격 패턴을 찾기 위한 정규식입니다.
                        val modifiedText = menuText?.replace(regex) { matchResult ->
                            val menu = matchResult.groupValues[1].trim() // 앞, 뒤 공백 제거
                            val price = matchResult.groupValues[2]
                            "$menu $price\n" // 개행문자를 추가해 한줄 개행
                        }
                        if (modifiedText==""){ // 만약 메뉴가 비었다면
                            menuTv.text = "메뉴판을 최신 데이터로 업데이트 중입니다.."
                        }
                        else{
                            menuTv.text = modifiedText
                        }

                    }
                }

                override fun onFailure(call: Call<ApiResponse<StoreMenuVO>>, t: Throwable) {
                    Log.e("getStoreMenu", "onFailure : ${t.message} ")
                }
            })
    }

    private fun setupEvents() {
    }

    // 전화번호 하이픈 추가
    fun String.convertNumberToPhoneNumber(): String {     // 코틀린의 확장함수 사용
        return try {
            val regexString = "(\\d{2})(\\d{3,4})(\\d{4})"
            if (!Pattern.matches(regexString, this)) this else Regex(regexString).replace(
                this,
                "$1-$2-$3"
            )
        } catch (e: ParseException) {
            e.printStackTrace()
            this
        }
    }

    // 리뷰 정보
    fun getReviewComments() {
        //enqueue : 비동기식 통신을 할 때 사용/ execute: 동기식
//        val data = intent.getStringExtra("store_id")
//        val storeId = Integer.parseInt(data)
        val data = intent.getStringExtra("store_id")
        val rand = Integer.parseInt(data.toString())

        if (intent.hasExtra("store_id")) {
//            val storeId = intent.getLongExtra("store_id", 357)
            //val photoUrl = intent.getStringExtra("photo_url")

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
                            val reviewsCount = reviews.size.toInt()
                            // 리뷰 수 구하기
                            reviewNumTv.text = reviewsCount.toString()
                            var reviewsSumOfStar = 0

                            for (review in reviews) {
                                // 별점 평균 구하기
                                reviewsSumOfStar += review.star.toInt()
                            }
                            starAvgTv.text = (reviewsSumOfStar / reviewsCount).toString()
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
        } else {
            Toast.makeText(this, "store_id 값이 전달되지 않았습니다.", Toast.LENGTH_SHORT).show()
        }
    }


}

private operator fun Int.div(reviewsCount: String) {

}

private fun Any.error(s: String) {

}

private fun Any.into(storeImageArea: ImageView?) {

}

