package kr.ac.duksung.rebit

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.unity3d.player.e
import com.unity3d.player.i
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
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class CreateReviewActivity() : AppCompatActivity() {
    private lateinit var retrofit: Retrofit
    private lateinit var retrofitService: RetrofitService

    private val uriList = ArrayList<Uri>()// ArrayList object to store URIs of selected images

    lateinit var recyclerView: RecyclerView// RecyclerView to display selected images
    lateinit var adapter: MultiImageAdapter// Adapter to apply to the RecyclerView


//    val imageUri: Uri = ... //이미지의 Uri
//    val imagePath: String = getRealPathFromURI(imageUri)

    private var imageUri: Uri? = null // Variable to store the selected image URI
    private var photo: String = ""
    private lateinit var imageResult: ActivityResultLauncher<Intent>


    fun uploadFileToS3(file: File, bucketName: String, accessKey: String, secretKey: String) {
        val credentials = BasicAWSCredentials(accessKey, secretKey)
        val s3Client = AmazonS3Client(credentials)

        val putObjectRequest = PutObjectRequest(bucketName, file.name, file)
        val putObject = s3Client.putObject(putObjectRequest)
        Log.d("S3_CHECK: ", putObject.toString())
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_review)


        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val reviewEditText = findViewById<EditText>(R.id.reviewEditText)
        val storeNameTextArea = findViewById<TextView>(R.id.storeNameTextArea)

        // Button to open the photo album
        val btnGetImage = findViewById<Button>(R.id.getImage)
        btnGetImage.setOnClickListener {
            selectGallery()
            btnGetImage.visibility = View.GONE
        }
//        recyclerView = findViewById(R.id.photoRecyclerView)
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

        imageResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
//이미지를 받으면 ImageView에 적용한다
                    val imageUri = result.data?.data
                    Log.d("getStoreInfo", "image upper:" + imageUri) // 값 있음.

                    imageUri?.let {
                        //서버 업로드를 위해 파일 형태로 변환한다
                        var imageFile = File(getRealPathFromURI(it))

                        //이미지를 불러온다
                        Glide.with(this)
                            .load(imageUri)
                            .fitCenter()
                            .apply(RequestOptions().override(500, 500))
                            .into(ReviewImageArea)

                        val uploadTask = UploadTask(imageFile,
                            BuildConfig.bucketName,
                            BuildConfig.accessKey,
                            BuildConfig.secretKey)
                        uploadTask.execute()
                    }
                    // 여기서 할당
                    photo = imageUri.toString()
                }
            }

        //리뷰 등록 버튼 클릭 시
        val submitBtn = findViewById<Button>(R.id.submit_btn)
        submitBtn.setOnClickListener()
        {
            val data = intent.getStringExtra("store_id")
            val rand = data!!.toInt()

            val storeId: Long = rand.toLong() //통신
            val star: Int = ratingBar.rating.toInt()
            //아직 회원구분 기능이 없기에
            val userId: Long = 3
            val comment: String = reviewEditText.text.toString()

            // null 값 체크
            Log.d("getStoreInfo", "image:$photo") // image uri...어떻게 서버에 올리지...ㅜㅜ

            val review = ReviewCommentsVO(storeId, userId, star, photo, comment)

            //서버에 리뷰 데이터를 보내고 (post)
            retrofitService.postReviewComments(review)
                .enqueue(object : Callback<ApiResponse<Int>> {
                    override fun onResponse(
                        call: Call<ApiResponse<Int>>,
                        response: Response<ApiResponse<Int>>,
                    ) {
                        if (response.isSuccessful) {
                            val result: ApiResponse<Int>? = response.body()
                            val data = result?.getResult()

                            Log.d("postReviewComments",
                                "onResponse success : " + result?.toString())
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

            val storeId2 = intent.getStringExtra("store_id")
            val storeId2ToInt = Integer.parseInt(storeId2.toString())

            Toast.makeText(this, "리뷰가 등록되었습니다!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ReviewDetailActivity::class.java)
            intent.putExtra("store_id", storeId2ToInt.toString()) // 리뷰 조회 액티비티로 인텐트 넘긴다.
            startActivity(intent)

        }

    }//OnCreate

    //서버 연결
    private fun initRetrofit() {
        retrofit = RetofitClient.getInstance()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    //통신-가게 이름 가져오기.
    private fun getStoreInfo() {
//        val data = intent.getStringExtra("store_id")
//        val storeId = data?.let { Integer.parseInt(it) }
        val data = intent.getStringExtra("store_id")
        try {
            val rand = data!!.toInt()

            // Use the store ID to retrieve store information from the server
            // ... (code to retrieve store information)

            retrofitService.getStoreInfo(rand.toLong()).enqueue(object :
                Callback<ApiResponse<StoreInfoVO>> {
                override fun onResponse(
                    call: Call<ApiResponse<StoreInfoVO>>,
                    response: Response<ApiResponse<StoreInfoVO>>,
                ) {
                    if (response.isSuccessful) {
//통신 성공시
                        val result: ApiResponse<StoreInfoVO>? = response.body()
                        val data = result?.getResult()

                        Log.d("getStoreInfo", "on response 성공: " + result?.toString())
                        Log.d("getStoreInfo", "data : " + data?.toString())

//가게 이름
                        val storeName = findViewById<TextView>(R.id.storeNameTextArea)
                        storeName.text = data!!.storeName
                    }
                }

                override fun onFailure(call: Call<ApiResponse<StoreInfoVO>>, t: Throwable) {
                    Log.e("getStoreInfo", "onFailure : ${t.message} ")
                }
            })

        } catch (e: NumberFormatException) {
            // Handle the exception when parsing the store ID fails
            Log.e("postReviewComments", "Failed to parse store ID: $data", e)

            // Perform appropriate error handling, such as displaying an error message
            Toast.makeText(applicationContext, "Invalid store ID", Toast.LENGTH_SHORT).show()

            // Finish the activity or take other necessary actions
            finish()
        }
    }

    private fun selectGallery() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                GALLERY_PERMISSION_CODE
            )
        } else {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imageResult.launch(intent)
    }

    private fun getRealPathFromURI(contentUri: Uri): String {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(contentUri, filePathColumn, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
        val picturePath = cursor?.getString(columnIndex ?: 0)
        cursor?.close()
        return picturePath ?: ""
    }

    companion object {
        private const val GALLERY_PERMISSION_CODE = 1
    }
}
