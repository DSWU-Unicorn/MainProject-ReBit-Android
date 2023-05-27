package kr.ac.duksung.rebit

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
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
import androidx.lifecycle.lifecycleScope
import kr.ac.duksung.rebit.databinding.ActivityCameraBinding
import kr.ac.duksung.rebit.network.RetofitClient
import kr.ac.duksung.rebit.network.RetrofitService
import kr.ac.duksung.rebit.network.dto.ApiResponse
import kr.ac.duksung.rebit.network.dto.RecycleVO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import com.google.android.gms.location.*
import kotlinx.coroutines.launch
import kr.ac.duksung.rebit.model.Classifier
import kr.ac.duksung.rebit.network.dto.RecycleDetailVO
import java.io.IOException
import java.util.*


class CameraActivity : AppCompatActivity() {
    // 통신
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    // GPS
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null // 현재 위치를 가져오기 위한 변수
    lateinit var mLastLocation: Location // 위치 값을 가지고 있는 객체
    internal lateinit var mLocationRequest: LocationRequest // 위치 정보 요청의 매개변수를 저장하는
    private val REQUEST_PERMISSION_LOCATION = 10
    private lateinit var mAlertDialog : AlertDialog

    private lateinit var content : String
    lateinit var bitmap: Bitmap
    lateinit var imageView: ImageView

    private lateinit var textView : TextView
    private lateinit var textView2 : TextView
    private lateinit var geocoder: Geocoder

    // 모델 연결
    private lateinit var classifier: Classifier
    private var dataLabel : String = ""

    private lateinit var binding: ActivityCameraBinding

    // 권한을 확인할때의 권한 확인을 위함
    val CAMERA = arrayOf(Manifest.permission.CAMERA)

    // 권한 요청을 위한 권한 자체를 정의
    val CAMERA_CODE = 98

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        geocoder = Geocoder(this)


        // 모델 연결
        initClassifier()

        // 서버 연결
        initRetrofit()


        mLocationRequest =  LocationRequest.create().apply {

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        }



        //객체 생성
        imageView = findViewById(R.id.imageView)
        // 촬영버튼
        val picBtn: Button = findViewById(R.id.pic_btn)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.picBtn.setOnClickListener() {
            CallCamera()
        }

        binding.startButton.setOnClickListener {
            // Dialog만들기
            val mDialogView =
                LayoutInflater.from(this).inflate(R.layout.after_recycle_model_dialog, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)

            mAlertDialog = mBuilder.show()

            val howto_text = mDialogView.findViewById<TextView>(R.id.howto_text)
            val title_text = mDialogView.findViewById<TextView>(R.id.title_text)
            howto_text.text = content
            title_text.text = dataLabel + "-분리수거 방법"
            Log.d("content", "content1 : "+ howto_text)

            /*
            val okButton = mDialogView.findViewById<Button>(R.id.successButton)
            okButton.setOnClickListener {
                postUserPointByRecycle(1L)

                Toast.makeText(this, "포인트 획득했습니다!", Toast.LENGTH_SHORT).show()
                mAlertDialog.dismiss()

                /**
                 * 포인트 획득 버튼 누르면 Unity로 돌아가게끔 수정 !
                 */
            }
             */

            val okButton = mDialogView.findViewById<Button>(R.id.successButton)
            okButton.setOnClickListener {
                // 현재 위치 찾기
                if (checkPermissionForLocation(this)) {
                    startLocationUpdates()
                }

            }


            val noButton = mDialogView.findViewById<Button>(R.id.AgainButton)
            noButton.setOnClickListener {
                CallCamera()
            }
        } //setOnClickListener


    }//onCreate

    private fun initClassifier() {
        classifier = Classifier(this, Classifier.IMAGENET_CLASSIFY_MODEL)
        try {
            classifier.init()
        } catch (exception: IOException) {
            Toast.makeText(this, "Can not init Classifier!!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        classifier.finish()
        super.onDestroy()
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
        // 사용자에게 권한 요청 후 결과에 대한 처리 로직
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()

            } else {
                Log.d("ttt", "onRequestPermissionsResult() _ 권한 허용 거부")
                Toast.makeText(this, "권한이 없어 해당 기능을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show()
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
        // 모델 연결
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_CODE -> {
                    if (data?.extras?.get("data") != null) {
                        val img = data?.extras?.get("data") as Bitmap
                        val output = classifier.classify(img)
                        val resultStr =
                            String.format(Locale.ENGLISH, "class : %s", output.first)
                        binding.run {
                            binding.textResult.text = resultStr
                            Log.d("MODEL_RESLT: " , resultStr)
                            dataLabel = resultStr
                            binding.imageView.setImageBitmap(img)
                            getRecycle()
                        }
                    }
                }
            }
        }
    }


    // 서버 연결
    private fun initRetrofit() {
        retrofit = RetofitClient.getInstance()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    private fun getRecycle() {
        // 분리수거 방법 통신
        lifecycleScope.launch {
            try {
                Log.d("RECYCLE_DATA_LABEL: ", dataLabel)
                retrofitService.getRecycle("Plastic")?.enqueue(object :
                    Callback<ApiResponse<RecycleVO>> {
                    override fun onResponse(
                        call: Call<ApiResponse<RecycleVO>>,
                        response: Response<ApiResponse<RecycleVO>>
                    ) {
                        if(response.isSuccessful) {
                            //정상적으로 통신 성공
                            val result : ApiResponse<RecycleVO>? = response.body();
                            val data = result?.getResult();

                            Log.d("RECYCLE_CAMERA" ,"onresponse 성공: "+ result?.toString() )
                            Log.d("RECYCLE_CAMERA", "data : "+ data)
                            Log.d("RECYCLE_CAMERA", "content : "+ data?.content)
                            content = data!!.content
                            Log.d("RECYCLE_CAMERA_CONTENT", "content1 : "+ content)


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
            } catch  (e: Exception) {
                // Exception handling
                Log.e(ContentValues.TAG, "Exception: ${e.message}", e)
            }
        }
    }

    private fun startLocationUpdates() {

        //FusedLocationProviderClient의 인스턴스를 생성.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        // 기기의 위치에 관한 정기 업데이트를 요청하는 메서드 실행
        // 지정한 루퍼 스레드(Looper.myLooper())에서 콜백(mLocationCallback)으로 위치 업데이트를 요청
        mFusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
    }

    // 시스템으로 부터 위치 정보를 콜백으로 받음
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // 시스템에서 받은 location 정보를 onLocationChanged()에 전달
            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)
        }
    }

    // 시스템으로 부터 받은 위치정보를 화면에 갱신해주는 메소드
    fun onLocationChanged(location: Location) {
        // 다이얼로그
        val mDialogView2 =
            LayoutInflater.from(this).inflate(R.layout.after_recycle_dialog, null)
        //val mBuilder2 = AlertDialog.Builder(this).create()
        //mBuilder2.setView(mDialogView2)


        val mBuilder2 = AlertDialog.Builder(this)
            .setView(mDialogView2)
        val mAlertDialog2 = mBuilder2.show()


        mAlertDialog.dismiss()
        //val mAlertDialog2 = mBuilder2.show()
        //mBuilder2.window?.setLayout(900, WindowManager.LayoutParams.WRAP_CONTENT)
        textView = mAlertDialog2.findViewById<TextView>(R.id.text1)!!
        textView2 = mAlertDialog2.findViewById<TextView>(R.id.text2)!!
        var addressTxt = mAlertDialog2.findViewById<TextView>(R.id.addressTxt)!!
        val successBtn = mAlertDialog2.findViewById<Button>(R.id.successButton)!!


        // 지오코딩
        mLastLocation = location
        Log.d("latitude", mLastLocation.latitude.toString())
        val address = geocoder.getFromLocation(mLastLocation.latitude,mLastLocation.longitude, 1)
        val nowAddr = address.get(0).getAddressLine(0).toString();
        textView2.text = nowAddr
        Log.d("latitude", nowAddr)

        var address_gu = textView2.text
        var range = IntRange(11, 13)
        Log.d("range", address_gu.slice(range).toString())

        var slice_address_gu = address_gu.slice(range).toString()
        if (slice_address_gu.contains("구")) {
            addressTxt.text = "현재 위치: " + slice_address_gu
        } else {
            addressTxt.text = "현재 위치: 도봉구 쌍문동"
        }

        // 지역별 추가적 분리수거 정보 조회 통신
        retrofitService.getRecycleDetailByRegion("도봉구")?.enqueue(object :
            Callback<ApiResponse<RecycleDetailVO>> {
            override fun onResponse(
                call: Call<ApiResponse<RecycleDetailVO>>,
                response: Response<ApiResponse<RecycleDetailVO>>
            ) {
                if(response.isSuccessful) {
                    //정상적으로 통신 성공
                    val result : ApiResponse<RecycleDetailVO>? = response.body();
                    val data = result?.getResult();

                    Log.d("RecycleDetailVO" ,"onresponse 성공: "+ result?.toString() )
                    Log.d("RecycleDetailVO", "data : "+ data?.toString())
                    textView.text = "[재활용 가능자원 배출 요일] \n" + data!!.day +
                            "\n\n [비닐, 투명페트명 배출 요일]\n" + data!!.typicalDay


                } else {
                    //통신 실패(응답코드 3xx, 4xx 등)
                    Log.d("YMC", "onResponse 실패" + response.errorBody().toString())
                }
            }

            override fun onFailure(call: Call<ApiResponse<RecycleDetailVO>>, t: Throwable) {
                //통신 실패(인터넷 끊김, 예외 발생 등 시스템적인 이유)
                Log.d("YMC", "onFailure 에러: " + t.message.toString());
            }


        })

        successBtn.setOnClickListener{
            // 포인트 획득 통신
            retrofitService.postUserPointByRecycle(1L)?.enqueue(object :
                Callback<ApiResponse<Int>> {
                override fun onResponse(
                    call: Call<ApiResponse<Int>>,
                    response: Response<ApiResponse<Int>>
                ) {
                    if(response.isSuccessful) {
                        //정상적으로 통신 성공
                        val result : ApiResponse<Int>? = response.body();
                        val data = result?.getResult();

                        Log.d("pointRecycle" ,"onresponse 성공: "+ result?.toString() )
                        Log.d("pointRecycle", "data : "+ data?.toString())
                        val point = data.toString()

                        // 다이얼로그 종료
                        mAlertDialog2.dismiss()

                        // toast
                        Toast.makeText(applicationContext, "회원 포인트 : " + point, Toast.LENGTH_SHORT).show()


                    } else {
                        //통신 실패(응답코드 3xx, 4xx 등)
                        Log.d("YMC", "onResponse 실패" + response.errorBody().toString())
                    }
                }

                override fun onFailure(call: Call<ApiResponse<Int>>, t: Throwable) {
                    //통신 실패(인터넷 끊김, 예외 발생 등 시스템적인 이유)
                    Log.d("YMC", "onFailure 에러: " + t.message.toString());
                }
            })
            // 화면 이동
            var intent = Intent(this, RecycleActivity::class.java)
            startActivity(intent)
        }


    }
    // 위치 권한이 있는지 확인하는 메서드
    private fun checkPermissionForLocation(context: Context): Boolean {
        // Android 6.0 Marshmallow 이상에서는 위치 권한에 추가 런타임 권한이 필요
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                // 권한이 없으므로 권한 요청 알림 보내기
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_LOCATION)
                false
            }
        } else {
            true
        }
    }

    //위도 경도로 주소 구하는 Reverse-GeoCoding
    private fun getAddress(location: Location): String {
        return try {
            with(Geocoder(applicationContext, Locale.KOREA).getFromLocation(location.latitude, location.longitude, 1).first()){
                getAddressLine(0)   //주소
                countryName     //국가이름 (대한민국)
                countryCode     //국가코드
                adminArea       //행정구역 (서울특별시)
                locality        //관할구역 (중구)
                thoroughfare    //상세구역 (봉래동2가)
                featureName     //상세주소 (122-21)
            }
        } catch (e: Exception){
            e.printStackTrace()
            getAddress(location)
        }
    }

}