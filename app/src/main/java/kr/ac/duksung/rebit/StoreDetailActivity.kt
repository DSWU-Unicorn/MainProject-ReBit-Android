package kr.ac.duksung.rebit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_store_detail.*
import kr.ac.duksung.rebit.databinding.ActivityStoreDetailBinding
import kr.ac.duksung.rebit.network.RetofitClient
import kr.ac.duksung.rebit.network.RetrofitService
import kr.ac.duksung.rebit.network.dto.ApiResponse
import kr.ac.duksung.rebit.network.dto.StoreInfoVO
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

    // goto_review_btn
    // 버튼 클릭시 store_id intent로 넘기기 필요.
    //====================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_detail)
        // activity_store_detail.xml에 설정했던 id 값 사용가능
        binding = ActivityStoreDetailBinding.inflate(layoutInflater)

        //setValues()

        setupEvents()
        //서버 연결
        initRetrofit()

        // 통신-가게 상세 정보
        getStoreInfo()

        //val store = intent.getSerializableExtra("storeInfo") as Store
        //
        val data = intent.getStringExtra("store_id")
        //val rand = "${store.id}"
        val rand = Integer.parseInt(data)
        Log.d("store_id", rand.toString())

//        val imgId = intArrayOf(
//            R.drawable.megacoffee1, R.drawable.coffeedream2,
//            R.drawable.eeeyo3, R.drawable.blackdown4,
//            R.drawable.bagle5
//        )

        //storeImageArea.setImageResource(imgId[rand.toInt()])

        // val pic_btn = findViewById<Button>(R.id.pic_btn)
        pic_btn.setOnClickListener {
            Toast.makeText(this, "내 용기가 맞을까? 확인하러 가기", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }
//        // review view
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
    fun getStoreInfo() {
        //enqueue : 비동기식 통신을 할 때 사용/ execute: 동기식
        val data = intent.getStringExtra("store_id")
        //val rand = "${store.id}"
        val rand = Integer.parseInt(data)

        // store_id 값을 review 로 intent 처리
        val goto_review_btn = findViewById<Button>(R.id.goto_review_btn)

        goto_review_btn.setOnClickListener {
            val intent = Intent(this, ReviewDetailActivity::class.java) // 리뷰 보러 가기
            intent.putExtra("store_id", data)
            startActivity(intent)
        }
        val todo_btn = findViewById<Button>(R.id.todo_btn)

        todo_btn.setOnClickListener {
            val intent = Intent(this, CreateReviewActivity::class.java) // 리뷰 작성
            intent.putExtra("store_id", data)
            startActivity(intent)
        }


        retrofitService.getStoreInfo(rand.toLong())?.enqueue(object :
            Callback<ApiResponse<StoreInfoVO>> {
            override fun onResponse(
                call: Call<ApiResponse<StoreInfoVO>>,
                response: Response<ApiResponse<StoreInfoVO>>,
            ) {
                if (response.isSuccessful) {
                    // 통신 성공시
                    val result: ApiResponse<StoreInfoVO>? = response.body()
                    val data = result?.getResult()

                    Log.d("info", "onresponse 성공: " + result?.toString())
                    Log.d("info", "data : " + data?.toString())

                    // 화면에 데이터 뿌린다.
                    // 가게 이름
                    storeNameTextArea.text = data!!.storeName
                    // 가게 주소
                    addressTxt.text = data.address // data binding 으로 findViewById 안써도 접근 가능.
                    // 가게 사진
                    val photoUrl = data.store_photo
                    //Glide.with(this@StoreDetailActivity).load(photoUrl).into(storeImageArea);
                    Glide.with(this@StoreDetailActivity).load("https:$photoUrl")
                        .error(R.drawable.sit_dagom_icon).into(storeImageArea);

                    // 가게 카테고리
                    storeKindTextArea.text = data.category
                    // 가게 전화번호
                    telText.text = (data.tel).convertNumberToPhoneNumber()
                    // 가게 별점 평균, 가게 리뷰 수
                    try {
                        starAvgTv.text = (data.reviewList[0].toString())
                        reviewNumTv.text = (data.reviewList[1].toString())
                    } catch (e: IndexOutOfBoundsException) {
                        e.printStackTrace()
                        starAvgTv.text = "아직 등록된 별점이 없습니다. 첫번째 별점을 달아주세요!"
                        reviewNumTv.text = "아직 등록된 리뷰가 없습니다. 첫번째 리뷰어가 되어주세요!"
                    }

                }
            }

            override fun onFailure(
                call: Call<ApiResponse<StoreInfoVO>>,
                t: Throwable,
            ) {
                Log.e("info", "onFailure : ${t.message} ");
            }
        })
    }

    fun setupEvents() {
    }

//    fun setValues() {
//
//        // storeInfo를 serializable로 받는다
//        // 그냥 받은 채로 변수에 넣으면 오류가 나는데 이 때 Casting을 해줘야 한다
//        val store = intent.getSerializableExtra("storeInfo") as Store
//        storeImageArea
//        // activity_store_detail.xml에 설정했던 view에 따라 매핑
//        storeNameTextArea.text = "${store.storeName}"
//        storeKindTextArea.text = "${store.category1}"
//        addressTxt.text = store.category2
//        telText.text = (store.tel).convertNumberToPhoneNumber()
//    }

    // 전화번호 하이픈 추가
    fun String.convertNumberToPhoneNumber(): String {     // 코틀린의 확장함수 사용
        return try {
            val regexString = "(\\d{2})(\\d{3,4})(\\d{4})"
            return if (!Pattern.matches(regexString, this)) this else Regex(regexString).replace(
                this,
                "$1-$2-$3"
            )
        } catch (e: ParseException) {
            e.printStackTrace()
            this
        }
    }
}


private fun Any.error(s: String) {

}

private fun Any.into(storeImageArea: ImageView?) {

}


