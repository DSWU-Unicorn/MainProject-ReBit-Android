package kr.ac.duksung.rebit

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.coroutines.launch
import kr.ac.duksung.rebit.databinding.ActivityCameraBinding
import kr.ac.duksung.rebit.network.RetofitClient
import kr.ac.duksung.rebit.network.RetrofitClientFlask
import kr.ac.duksung.rebit.network.RetrofitService
import kr.ac.duksung.rebit.network.dto.ApiResponse
import kr.ac.duksung.rebit.network.dto.StoreInfoVO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class YonggiCameraActivity : AppCompatActivity() {
    lateinit var bitmap: Bitmap
    lateinit var imageView: ImageView

    private lateinit var binding: ActivityCameraBinding
    private lateinit var systemUiController: SystemUiController
    private var firstPhotoFileName: String? = null
    private var secondPhotoFileName: String? = null
    private var isSecondPhotoTaken = false
    private var store_id_s: String? = null
    private var mDialogView: View? = null
    private var mAlertDialog: AlertDialog? = null

    private lateinit var retrofit: Retrofit
    private lateinit var retrofitService: RetrofitService


    // toast message
    private var toast: Toast? = null

    var flag = 0

    // 권한을 확인할때의 권한 확인을 위함
    val CAMERA = arrayOf(Manifest.permission.CAMERA)

    // 권한 요청을 위한 권한 자체를 정의
    val CAMERA_CODE = 98

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        //
        store_id_s = intent.getStringExtra("store_id")
        val store_id = Integer.parseInt(store_id_s)
        Log.d("YONGGICAMERA_STORE_ID", store_id.toString())

        //객체 생성
        imageView = findViewById(R.id.imageView)
        // 촬영버튼
        val picBtn: Button = findViewById(R.id.pic_btn)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Initialize the SystemUiController with the current window
        systemUiController = SystemUiController(window)

        // Set the status bar color
        systemUiController.setStatusBarColor(ContextCompat.getColor(this, R.color.teal_200))

        // Set the navigation bar color
        systemUiController.setNavigationBarColor(ContextCompat.getColor(this, R.color.white))

        // Set the system bars color
        systemUiController.setSystemBarsColor(ContextCompat.getColor(this, R.color.purple_200))

        // 플라스크 서버 연결
        initRetrofit()

        // S3 설정
        fun uploadFileToS3(file: File, bucketName: String, accessKey: String, secretKey: String) {
            val credentials = BasicAWSCredentials(accessKey, secretKey)
            val s3Client = AmazonS3Client(credentials)

            val putObjectRequest = PutObjectRequest(bucketName, file.name, file)
            val putObject = s3Client.putObject(putObjectRequest)
            Log.d("S3_CHECK: ", putObject.toString())
        }


        // 부적합한 용기일때
        binding.noBtn.setOnClickListener {
            // Dialog만들기
            mDialogView =
                LayoutInflater.from(this)
                    .inflate(R.layout.after_yongginae_model_unsuitable_dialog, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)

            val mAlertDialog = mBuilder.show()


            val noButton = mDialogView?.findViewById<Button>(R.id.AgainButton)
            noButton?.setOnClickListener {
                CallCamera()
                mAlertDialog.dismiss()
            }
        } //setOnClickListener


        binding.picBtn.setOnClickListener() {
            CallCamera()
            if (flag.equals(0)) {
                makeToast("정확한 측정을 위해 동전과 함께 용기의 윗부분을 촬영해주세요!")
            }
            flag += 1

            // 촬영 완료 버튼
            binding.startButton.setOnClickListener {
                getYongginaeResult()


            } //setOnClickListener
        }
    }//onCreate

    private fun getYongginaeResult() {
        //1. 카페 - 블랙다운357, 백다방358, 그때거기359,
        //2. 식당 - 이요 356, 맥켄 360
        var store_type: String? = null
        if (store_id_s.equals("357") || store_id_s.equals("358") || store_id_s.equals("359")) {
            store_type = "1"
        } else store_type = "2"

        lifecycleScope.launch {
            retrofitService.getYongginaeResult(
                firstPhotoFileName.toString(),
                secondPhotoFileName.toString(),
                store_type
            ).enqueue(object :
                Callback<ApiResponse<String>> {
                override fun onResponse(
                    call: Call<ApiResponse<String>>,
                    response: Response<ApiResponse<String>>,
                ) {
                    if (response.isSuccessful) {
                        val result: ApiResponse<String>? = response.body();
                        val data = result?.getMessage()
                        Log.d("YONGGINAE_RESULT", "data : " + data)

                        // Dialog만들기
                        // 해당 다회용기는 사용이 적합하다.
                        val mDialogView =
                            LayoutInflater.from(applicationContext)
                                .inflate(R.layout.after_yongginae_model_dialog, null)
                        val mBuilder =
                            AlertDialog.Builder(this@YonggiCameraActivity, R.style.AlertDialogTheme)
                                .setView(mDialogView)
                        var resultText = mDialogView?.findViewById<TextView>(R.id.howto_text)

                        if (data.equals("1")) {
                            Log.d("YONGGINAE_RESULT: ", "1찍힘")
                            resultText?.text = "해당 용기는 포장이 가능합니다."
                        } else if (data.equals("0")) {
                            Log.d("YONGGINAE_RESULT: ", "0찍힘")
                            resultText?.text = "해당 용기는 포장이 불가능합니다."
                        }

                        mAlertDialog = mBuilder.show()


                        // 용기내러 가기! 버튼 클릭시
                        val togoButton = mDialogView?.findViewById<Button>(R.id.togoButton)
                        togoButton?.setOnClickListener {

                            //Toast.makeText(this, "용기를 내서 포장하러 가는 당신! 멋져요⭐️️", Toast.LENGTH_SHORT).show()
                            mAlertDialog?.dismiss()

                            // 포장하러가기 누르면.. 용기내 지도 화면으로 이동해 지도에 포장하러가기 상태바 띄움.
                            val intent = Intent(this@YonggiCameraActivity, TogoActivity::class.java)
                            intent.putExtra("status", "true")
                            intent.putExtra("store_id", store_id_s.toString())
                            startActivity(intent)

                        }

                        val noButton = mDialogView?.findViewById<Button>(R.id.AgainButton)
                        noButton?.setOnClickListener {
                            CallCamera()
                        }

                    } else {
                        //통신 실패(응답코드 3xx, 4xx 등)
                        Log.d("YMC", "onResponse 실패" + response.errorBody().toString())
                    }
                }

                override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
                    Log.e("YMC", "onFailure : ${t.message} ")
                }
            })
        }
}

    fun checkPermission(permissions: Array<out String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(this, permissions, CAMERA_CODE)
                    return false;
                }
            }
        }
        return true;
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_CODE -> {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "카메라 권한을 승인해 주세요.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    fun CallCamera() {
        if (checkPermission(CAMERA)) {
            val itt = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(itt, CAMERA_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_CODE -> {
                    if (data?.extras?.get("data") != null) {
                        val img = data?.extras?.get("data") as Bitmap
                        binding.imageView.setImageBitmap(img)

                        if (!isSecondPhotoTaken) {
                            firstPhotoFileName = null
                            secondPhotoFileName = null
                            firstPhotoFileName = generatePhotoFileName()
                            val firstImageFile = convertBitmapToFile(img, firstPhotoFileName.toString())
                            Log.d("FIRST_IMAGENAME:", firstImageFile.name)

                            val uploadTask = UploadTask(firstImageFile,
                                BuildConfig.bucketName,
                                BuildConfig.accessKey,
                                BuildConfig.secretKey)
                            uploadTask.execute()

                            isSecondPhotoTaken = true
                        } else {
                            secondPhotoFileName = generatePhotoFileName()
                            val secondImageFile = convertBitmapToFile(img, secondPhotoFileName.toString())
                            Log.d("SECOND_IMAGENAME:", secondImageFile.name)

                            val uploadTask = UploadTask(secondImageFile,
                                BuildConfig.bucketName,
                                BuildConfig.accessKey,
                                BuildConfig.secretKey)
                            uploadTask.execute()

                            // Reset for the next round of photos
                            isSecondPhotoTaken = false
                        }

                        if(flag.equals(1)){
                            makeToast( "정확한 측정을 위해 한번 더 동전과 함께 용기의 측면을 촬영해주세요!️️")
                        }
                        flag+=1
                    }
                }
            }
        }
    }

    private fun makeToast(message: String){
        toast?.cancel()
        toast = Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
        toast?.show()
    }

    private fun convertBitmapToFile(bitmap: Bitmap, filename: String): File {
        val file = File(this.cacheDir, filename)
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }


    private fun generatePhotoFileName(): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "IMG_$timeStamp.jpg"
    }

    private fun initRetrofit() {
        retrofit = RetrofitClientFlask.getInstance()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }
}