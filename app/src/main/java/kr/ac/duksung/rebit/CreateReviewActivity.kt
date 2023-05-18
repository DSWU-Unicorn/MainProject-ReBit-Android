package kr.ac.duksung.rebit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_create_review.*
import kotlinx.android.synthetic.main.activity_create_review.storeNameTextArea
import kotlinx.android.synthetic.main.activity_store_detail.*
import kotlinx.android.synthetic.main.multi_image_item.*
import kr.ac.duksung.rebit.network.RetrofitService
import retrofit2.Retrofit
import kr.ac.duksung.rebit.network.RetofitClient
import kr.ac.duksung.rebit.network.dto.ApiResponse
import kr.ac.duksung.rebit.network.dto.ReviewCommentsVO
import kr.ac.duksung.rebit.network.dto.StoreInfoVO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateReviewActivity() : AppCompatActivity() {
    private lateinit var retrofit: Retrofit
    private lateinit var retrofitService: RetrofitService

    private val uriList = ArrayList<Uri>() // ArrayList object to store URIs of selected images

    lateinit var recyclerView: RecyclerView // RecyclerView to display selected images
    lateinit var adapter: MultiImageAdapter // Adapter to apply to the RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_review)

        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val reviewEditText = findViewById<EditText>(R.id.reviewEditText)

        // Button to open the photo album
        val btnGetImage = findViewById<Button>(R.id.getImage)
        btnGetImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = MediaStore.Images.Media.CONTENT_TYPE
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            startActivityForResult(intent, 2222)
        }
        recyclerView = findViewById(R.id.photoRecyclerView)
        val close_btn = findViewById<Button>(R.id.close_btn)
        close_btn.setOnClickListener {
            finish()
        }
        // name
//        val data = intent.getStringExtra("store_id") // intent는 한번만 받을 수 있나본데?
//        val storeId = data?.let { Integer.parseInt(it) }

        //서버 연결
        initRetrofit()
        getStoreInfo()

//        // 리뷰 등록 버튼 클릭 시
//        val submitBtn = findViewById<Button>(R.id.submit_btn)
//
//        submitBtn.setOnClickListener()
//        {
//            // 서버에 리뷰 데이터를 보내고 (post)
//            // Retrieve user input data from EditText views
//            val star: Int = ratingBar.rating.toInt()
//            val storeId: Long = storeId!!.toLong() // 통신
//            val userId: Long = 1 // 아직 회원구분 기능이 없기에, 하드 코딩.
//            val photo: String = "http://s3.amazonaws.com/[bucket_name]/" // 사진 기능 버그 fix 필요
//            val comment: String = reviewEditText.text.toString()
//
//            val review = ReviewCommentsVO(storeId,
//                userId,
//                star,
//                photo,
//                comment)
//
//            retrofitService.postReviewComments(review)
//                .enqueue(object : Callback<ApiResponse<Int>> {
//                    override fun onResponse(
//                        call: Call<ApiResponse<Int>>,
//                        response: Response<ApiResponse<Int>>,
//                    ) {
//                        if (response.isSuccessful) {
//                            val result: ApiResponse<Int>? = response.body()
//                            val data = result?.getResult()
//
//                            Log.d("postReviewComments", "onResponse success" + result?.toString())
//                            Log.d("postReviewComments", "data: " + data?.toString())
//                        } else {
//                            Log.d("postReviewComments",
//                                "onResponse error: " + response.errorBody().toString())
//                        }
//                    }
//
//                    override fun onFailure(call: Call<ApiResponse<Int>>, t: Throwable) {
//                        Log.e("postReviewComments", "onFailure: ${t.message}")
//                    }
//                })
//
//            Toast.makeText(this, "리뷰가 등록되었습니다!", Toast.LENGTH_SHORT).show()
//            val intent = Intent(this, ReviewDetailActivity::class.java)
//            startActivity(intent)
//        }
    } //OnCreate

    //서버 연결
    private fun initRetrofit() {
        retrofit = RetofitClient.getInstance()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    // 통신-가게 이름 가져오기.
    fun getStoreInfo() {
        val data = intent.getStringExtra("store_id")
        val storeId = data?.let { Integer.parseInt(it) }

        storeId?.let {
            retrofitService.getStoreInfo(it.toLong())?.enqueue(object :
                Callback<ApiResponse<StoreInfoVO>> {
                override fun onResponse(
                    call: Call<ApiResponse<StoreInfoVO>>,
                    response: Response<ApiResponse<StoreInfoVO>>,
                ) {
                    if (response.isSuccessful) {
                        // 통신 성공시
                        val result: ApiResponse<StoreInfoVO>? = response.body()
                        val data = result?.getResult()

                        Log.d("getStoreInfo", "on response 성공: " + result?.toString())
                        Log.d("getStoreInfo", "data : " + data?.toString())

                        // 가게 이름
                        val storeName = findViewById<TextView>(R.id.storeNameTextArea)
                        storeName.text = data!!.storeName
                    }
                }

                override fun onFailure(call: Call<ApiResponse<StoreInfoVO>>, t: Throwable) {
                    Log.e("getStoreInfo", "onFailure : ${t.message} ")
                }
            })
        }
        // 리뷰 등록 버튼 클릭 시
        val submitBtn = findViewById<Button>(R.id.submit_btn)

        submitBtn.setOnClickListener()
        {
            // 서버에 리뷰 데이터를 보내고 (post)
            // Retrieve user input data from EditText views
            val star: Int = ratingBar.rating.toInt()
            val storeId: Long = storeId!!.toLong() // 통신
            val userId: Long = 2 // 아직 회원구분 기능이 없기에, 하드 코딩.
            val photo: String = "http://s3.amazonaws.com/[bucket_name]/" // 사진 기능 버그 fix 필요
            val comment: String = reviewEditText.text.toString()

            val review = ReviewCommentsVO(storeId,
                userId,
                star,
                photo,
                comment)

            retrofitService.postReviewComments(review)
                .enqueue(object : Callback<ApiResponse<Int>> {
                    override fun onResponse(
                        call: Call<ApiResponse<Int>>,
                        response: Response<ApiResponse<Int>>,
                    ) {
                        if (response.isSuccessful) {
                            val result: ApiResponse<Int>? = response.body()
                            val data = result?.getResult()

                            Log.d("postReviewComments", "onResponse success" + result?.toString())
                            Log.d("postReviewComments", "data: " + data?.toString())
                        } else {
                            Log.d("postReviewComments",
                                "onResponse error: " + response.errorBody().toString())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<Int>>, t: Throwable) {
                        Log.e("postReviewComments", "onFailure: ${t.message}")
                    }
                })

            Toast.makeText(this, "리뷰가 등록되었습니다!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ReviewDetailActivity::class.java)
            intent.putExtra("store_id", storeId)
            startActivity(intent)
        }

    }

    //Method executed after returning from the photo album
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        data?.let {
            // No image selected
            if (it.data == null) {
                Toast.makeText(applicationContext, "No image selected.", Toast.LENGTH_LONG).show()
            } else {
                // Single image selected
                if (it.clipData == null) {
                    Log.e("single choice: ", it.data.toString())
                    val imageUri = it.data!!
                    uriList.add(imageUri)

                    adapter = MultiImageAdapter(uriList, applicationContext)
                    recyclerView.adapter = adapter
                    recyclerView.layoutManager =
                        LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)
                }
                // Multiple images selected
                else {
                    val clipData = it.clipData!!
                    Log.e("clipData", clipData.itemCount.toString())

                    if (clipData.itemCount > 10) {
                        Toast.makeText(
                            applicationContext,
                            "You can select up to 10 photos.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Log.e("onActivityResult", "multiple choice")

                        for (i in 0 until clipData.itemCount) {
                            val imageUri =
                                clipData.getItemAt(i).uri // Get the URIs of the selected images
                            try {
                                uriList.add(imageUri) // Add the URI to the list

                            } catch (e: Exception) {
                                Log.e("onActivityResult", "File select error", e)
                            }
                        }

                        adapter = MultiImageAdapter(uriList, applicationContext)
                        recyclerView.adapter = adapter // Set the adapter to the RecyclerView
                        recyclerView.layoutManager = LinearLayoutManager(
                            this,
                            LinearLayoutManager.HORIZONTAL,
                            true
                        ) // Apply horizontal scrolling to the RecyclerView
                    }
                }
            }
        }

    }


}

