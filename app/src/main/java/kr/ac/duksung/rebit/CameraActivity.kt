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
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import kr.ac.duksung.rebit.databinding.ActivityCameraBinding
import kr.ac.duksung.rebit.network.RetofitClient
import kr.ac.duksung.rebit.network.RetrofitService
import kr.ac.duksung.rebit.network.dto.ApiResponse
import kr.ac.duksung.rebit.network.dto.CardNewsVO
import kr.ac.duksung.rebit.network.dto.RecycleVO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class CameraActivity : AppCompatActivity() {
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    private lateinit var content : String
    private lateinit var dataLabel : String
    lateinit var bitmap: Bitmap
    lateinit var imageView: ImageView

    private lateinit var binding: ActivityCameraBinding

    // 권한을 확인할때의 권한 확인을 위함
    val CAMERA = arrayOf(Manifest.permission.CAMERA)

    // 권한 요청을 위한 권한 자체를 정의
    val CAMERA_CODE = 98

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        // dataLabel 설정
        dataLabel = "플라스틱"

        // 서버 연결
        initRetrofit()

        // 분리수거 방법 통신
        retrofitService.getRecycle(dataLabel)?.enqueue(object :
            Callback<ApiResponse<RecycleVO>> {
            override fun onResponse(
                call: Call<ApiResponse<RecycleVO>>,
                response: Response<ApiResponse<RecycleVO>>
            ) {
                if(response.isSuccessful) {
                    //정상적으로 통신 성공
                    val result : ApiResponse<RecycleVO>? = response.body();
                    val data = result?.getResult();

                    Log.d("Recycle" ,"onresponse 성공: "+ result?.toString() )
                    Log.d("Recycle", "data : "+ data)
                    Log.d("Recycle", "content : "+ data?.content)
                    content = data!!.content
                    Log.d("content", "content1 : "+ content)


                } else {
                    //통신 실패(응답코드 3xx, 4xx 등)
                    Log.d("YMC", "onResponse 실패" + response.errorBody().toString())
                }
            }

            override fun onFailure(call: Call<ApiResponse<RecycleVO>>, t: Throwable) {
                //통신 실패(인터넷 끊김, 예외 발생 등 시스템적인 이유)
                Log.d("YMC", "onFailure 에러: " + t.message.toString());
            }

        })



        //객체 생성
        imageView = findViewById(R.id.imageView)
        // 촬영버튼
        val picBtn: Button = findViewById(R.id.pic_btn)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.picBtn.setOnClickListener() {
            CallCamera()
            binding.startButton.setOnClickListener {

                // Dialog만들기
                val mDialogView =
                    LayoutInflater.from(this).inflate(R.layout.after_model_dialog, null)
                val mBuilder = AlertDialog.Builder(this)
                    .setView(mDialogView)

                val mAlertDialog = mBuilder.show()


                val howto_text = mDialogView.findViewById<TextView>(R.id.howto_text)
                val title_text = mDialogView.findViewById<TextView>(R.id.title_text)
                howto_text.text = content
                title_text.text = dataLabel + "-분리수거 방법"
                Log.d("content", "content1 : "+ howto_text)


                val okButton = mDialogView.findViewById<Button>(R.id.successButton)
                okButton.setOnClickListener {

                    Toast.makeText(this, "포인트 획득했습니다!", Toast.LENGTH_SHORT).show()
                    mAlertDialog.dismiss()
                }

                val noButton = mDialogView.findViewById<Button>(R.id.AgainButton)
                noButton.setOnClickListener {
                    CallCamera()
                }
            } //setOnClickListener
        }
    }//onCreate

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
                    }
                }
            }
        }
    }


    //서버 연결
    private fun initRetrofit() {
        retrofit = RetofitClient.getInstance()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }


}

