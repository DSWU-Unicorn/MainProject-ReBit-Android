package kr.ac.duksung.rebit

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.viewpager.widget.ViewPager
import kr.ac.duksung.rebit.databinding.ActivityRecycleBinding
import kr.ac.duksung.rebit.network.RetofitClient
import kr.ac.duksung.rebit.network.RetrofitService
import kr.ac.duksung.rebit.network.dto.ApiResponse
import kr.ac.duksung.rebit.network.dto.CardNewsVO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class RecycleActivity : AppCompatActivity() {
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    private lateinit var binding: ActivityRecycleBinding
    private var imageList = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycle)

        val camera_btn = findViewById<Button>(R.id.camera_btn)
        val tip_button = findViewById<Button>(R.id.tip_button)

        //서버 연결
        initRetrofit()

        //통신
        getCardNews()

        // 카메라
        camera_btn.setOnClickListener {
            var intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }


        // 오늘의 Tip 기능
        // 뷰 바인딩-> 여기선 사용하지 않겠음. Github Issues #6 참고
//        binding = ActivityRecycleBinding.inflate(layoutInflater)
//        setContentView(binding.root)

        tip_button.setOnClickListener {
            // Dialog만들기
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.today_tip_dialog, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)

            val mAlertDialog = mBuilder.show()

            // 포인트 획득 버튼 클릭시
            val okButton = mDialogView.findViewById<Button>(R.id.successButton)
            okButton.setOnClickListener {
                Toast.makeText(this, "포인트를 획득했습니다!", Toast.LENGTH_SHORT).show()
                mAlertDialog.dismiss()
            }
        }


//        close_btn.setOnClickListener {
//            var intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//        }

    }
    // 뒤로가기
    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    //서버 연결
    private fun initRetrofit() {
        retrofit = RetofitClient.getInstance()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    //통신
    fun getCardNews() {
        retrofitService.getCardNews("2023-03-16")?.enqueue(object :
            Callback<ApiResponse<ArrayList<CardNewsVO>>> {
            override fun onResponse(
                call: Call<ApiResponse<ArrayList<CardNewsVO>>>,
                response: Response<ApiResponse<ArrayList<CardNewsVO>>>
            ) {
                if(response.isSuccessful) {
                    //정상적으로 통신 성공
                    val result : ApiResponse<ArrayList<CardNewsVO>>? = response.body();
                    val data = result?.getResult();

                    Log.d("CardNews" ,"onresponse 성공: "+ result?.toString() )
                    Log.d("CardNews", "data : "+ data?.toString())

                    for (cardNews in data!!) {
                        imageList.add(cardNews.image_src)
                    }
                    Log.d("CardNews", "imageList : "+ imageList?.toString())

                    //
                    val adapter = ViewPagerAdapter(imageList)
                    val pager = findViewById<ViewPager>(R.id.viewPager)
                    pager.adapter = adapter

                } else {
                    //통신 실패(응답코드 3xx, 4xx 등)
                    Log.d("YMC", "onResponse 실패" + response.errorBody().toString())
                }
            }

            override fun onFailure(call: Call<ApiResponse<ArrayList<CardNewsVO>>>, t: Throwable) {
                //통신 실패(인터넷 끊김, 예외 발생 등 시스템적인 이유)
                Log.d("YMC", "onFailure 에러: " + t.message.toString());
            }

        })
    }

}