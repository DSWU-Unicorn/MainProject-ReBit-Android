package kr.ac.duksung.rebit

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import com.unity3d.player.UnityPlayerActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import kr.ac.duksung.rebit.databinding.ActivityMainBinding
import kr.ac.duksung.rebit.databinding.ActivityStoreDetailBinding
import kr.ac.duksung.rebit.network.RetofitClient
import kr.ac.duksung.rebit.network.RetrofitService
import kr.ac.duksung.rebit.network.dto.ApiResponse
import kr.ac.duksung.rebit.network.dto.StoreAddressVO
import kr.ac.duksung.rebit.network.dto.StoreNameVO
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {
    // 뒤로 가기
    private var isDouble = false

    private lateinit var retrofit: Retrofit
    private lateinit var retrofitService: RetrofitService

    // GPS
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null // 현재 위치를 가져오기 위한 변수
    lateinit var mLastLocation: Location // 위치 값을 가지고 있는 객체
    internal lateinit var mLocationRequest: LocationRequest // 위치 정보 요청의 매개변수를 저장하는
    private val REQUEST_PERMISSION_LOCATION = 10

    // 위치
    private var storeLatitude : Double = 0.0
    private var storeLongitude : Double = 0.0
    private var myLatitude : Double = 0.0
    private var myLongitude : Double = 0.0

    private var isArrived = false


    private lateinit var binding: ActivityMainBinding
    @RequiresApi(Build.VERSION_CODES.P)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)

        //
        val btn = findViewById<Button>(R.id.btn)
        btn.setOnClickListener{
            Log.d("버튼클릭:" , "버튼클릭됨 !")
            // Dialog만들기
            val mDialogView =
                LayoutInflater.from(this).inflate(R.layout.after_togo_dialog, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)

            val mAlertDialog = mBuilder.show()
            val yesBtn = mDialogView.findViewById<Button>(R.id.yesBtn)
            yesBtn.setOnClickListener {
                // 6. 포인트 증가
                // api 호출
                lifecycleScope.launch {
                    try {
                        retrofitService.postUserWithPointAfterYonggi(1)?.enqueue(object :
                            Callback<ApiResponse<Int>> {
                            override fun onResponse(
                                call: Call<ApiResponse<Int>>,
                                response: Response<ApiResponse<Int>>
                            ){
                                if(response.isSuccessful){
                                    // 통신 성공시
                                    val result: ApiResponse<Int>?=response.body()
                                    val datas = result?.getResult()

                                    Log.d("POINTRESULT" ,"용기내 onresponse 성공: "+ result?.toString())
                                    Log.d("POINTRESULT", "용기내 data : "+ datas?.toString())
                                }
                            }
                            override fun onFailure(
                                call: Call<ApiResponse<Int>>,
                                t: Throwable
                            ) {
                                Log.e("POINTRESULT","용기내 onFailure : ${t.message} ");
                            }
                        })
                    } catch  (e: Exception) {
                        // Exception handling
                        Log.e(ContentValues.TAG, "Exception: ${e.message}", e)
                    }
                }
                mAlertDialog.dismiss()
                // UI 초기화
                toGoTxt.visibility = INVISIBLE
            }

            val noBtn = mDialogView.findViewById<Button>(R.id.noBtn)
            noBtn.setOnClickListener {
                // Create and show the "No" dialog
                val noDialogView = LayoutInflater.from(this).inflate(R.layout.after_togo_diglog_nobtn, null)
                val noBuilder = AlertDialog.Builder(this)
                    .setView(noDialogView)

                val noAlertDialog = noBuilder.create() // Create the dialog but do not show it yet
                noAlertDialog.show() // Show the dialog
                val againBtn = noDialogView.findViewById<Button>(R.id.AgainButton)
                againBtn.setOnClickListener{
                      noAlertDialog.dismiss()
                }

                // Dismiss the current dialog
//                mAlertDialog.dismiss()
            }
        }




        try {
            val information =
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            val signatures = information.signingInfo.apkContentsSigners
            val md = MessageDigest.getInstance("SHA")
            for (signature in signatures) {
                val md: MessageDigest
                md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                var hashcode = String(Base64.encode(md.digest(), 0))
                Log.d("hashcode", "" + hashcode)
            }
        } catch (e: Exception) {
            Log.d("hashcode", "에러::" + e.toString())

        }


        //서버 연결
        initRetrofit()

        var toGoTxt = findViewById<TextView>(R.id.toGoTxt)

        // intent 얻기
        val status = intent.getStringExtra("status")
        val store_id = intent.getStringExtra("store_id")
        Log.d("STATUS", status.toString())
        Log.d("MAIN_STORE_ID", store_id.toString())
        if (status.toBoolean()) {

            mLocationRequest =  LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            // 1. GPS 권한 확인
            if (checkPermissionForLocation(this)) {
                startLocationUpdates()
            } else Toast.makeText(this, "GPS를 켜주세요", Toast.LENGTH_SHORT).show()

            // 2. api 호출
            lifecycleScope.launch {
                try {
                    retrofitService.getStoreAddressTogo(Integer.parseInt(store_id))?.enqueue(object :
                        Callback<ApiResponse<StoreAddressVO>> {
                        override fun onResponse(
                            call: Call<ApiResponse<StoreAddressVO>>,
                            response: Response<ApiResponse<StoreAddressVO>>
                        ){
                            if(response.isSuccessful){
                                // 통신 성공시
                                val result: ApiResponse<StoreAddressVO>?=response.body()
                                val datas = result?.getResult()

                                Log.d("MAINRESULT" ,"onresponse 성공: "+ result?.toString())
                                Log.d("MAINRESULT", "data : "+ datas?.address)

                                var geocoder = Geocoder(applicationContext)
                                val fromLocationName =
                                    geocoder.getFromLocationName(datas.toString(), 1)
                                Log.d("MAINRESULT", "LONGITUDE : "+ fromLocationName.get(0).longitude.toString())
                                Log.d("MAINRESULT", "LATITUDE : "+ fromLocationName.get(0).latitude.toString())

                                storeLatitude = fromLocationName.get(0).latitude
                                storeLongitude = fromLocationName.get(0).longitude

                            }
                        }
                        override fun onFailure(
                            call: Call<ApiResponse<StoreAddressVO>>,
                            t: Throwable
                        ) {
                            Log.e("MAINRESULT","onFailure : ${t.message} ");
                        }
                    })
                } catch  (e: Exception) {
                    // Exception handling
                    Log.e(ContentValues.TAG, "Exception: ${e.message}", e)
                }
            }
            // 3. ui 변경
            toGoTxt.visibility = VISIBLE
        }


        val recycle_btn = findViewById<Button>(R.id.recycle_btn)
        val todo_btn = findViewById<Button>(R.id.todo_btn)

        //== unity 연결 버튼
        val unity_btn = findViewById<Button>(R.id.unity_btn)

        recycle_btn.setOnClickListener{
            val intent = Intent(this, RecycleActivity::class.java)
            startActivity(intent)
        }
        todo_btn.setOnClickListener {
            val intent = Intent(this, TogoActivity::class.java)
            startActivity(intent)
        }

//        //== unity 버튼 연결
        unity_btn.setOnClickListener {
//            val intent = Intent(this, UnityPlayerActivity::class.java)
            val intent = Intent(this, UnityPlayerActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onBackPressed() {
        Log.d("MainActivity", "backbutton")
        if (isDouble == true) { // 두번 뒤로가기 클릭시
            finish() // 앱 종료
        }
        isDouble = true // 한번 뒤로가기 클릭시
        Toast.makeText(this, "뒤로가기 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()

        Handler().postDelayed({
            isDouble = false
        }, 2000) // 한번 클릭 후 2초 지나면 false로 변경
    }

    //서버 연결
    private fun initRetrofit() {
        retrofit = RetofitClient.getInstance()
        retrofitService = retrofit.create(RetrofitService::class.java)
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
        mLastLocation = location
        Log.d("MAIN_위도 : ",  mLastLocation.latitude.toString()) // 갱신 된 위도
        Log.d("MAIN_경도 : ",  mLastLocation.longitude.toString())  // 갱신 된 경도
        myLatitude = mLastLocation.latitude
        myLongitude = mLastLocation.longitude

        /**
         * 4. 거리 계산
         */
        var myLocation = Location("my location")
        myLocation.latitude = myLatitude
        myLocation.longitude = myLongitude

        var storeLocation = Location("store location")
        storeLocation.latitude = storeLatitude
        storeLocation.longitude = storeLongitude
        Log.d("STORELOCATION: " , storeLocation.latitude.toString())
        Log.d("STORELOCATION: " , storeLocation.longitude.toString())

        val distance = myLocation.distanceTo(storeLocation)
        Log.d("MAIN_DISTANCE", distance.toString())

        if (distance <= 250) {
            isArrived = true
        }

        if (isArrived) {
            // 5. 다이얼로그
            // Dialog만들기
            val mDialogView =
                LayoutInflater.from(this).inflate(R.layout.after_togo_dialog, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)

            val mAlertDialog = mBuilder.show()
            val yesBtn = mDialogView.findViewById<Button>(R.id.yesBtn)
            yesBtn.setOnClickListener {
                // 6. 포인트 증가
                // api 호출
                lifecycleScope.launch {
                    try {
                        retrofitService.postUserWithPointAfterYonggi(1)?.enqueue(object :
                            Callback<ApiResponse<Int>> {
                            override fun onResponse(
                                call: Call<ApiResponse<Int>>,
                                response: Response<ApiResponse<Int>>
                            ){
                                if(response.isSuccessful){
                                    // 통신 성공시
                                    val result: ApiResponse<Int>?=response.body()
                                    val datas = result?.getResult()

                                    Log.d("POINTRESULT" ,"용기내 onresponse 성공: "+ result?.toString())
                                    Log.d("POINTRESULT", "용기내 data : "+ datas?.toString())
                                }
                            }
                            override fun onFailure(
                                call: Call<ApiResponse<Int>>,
                                t: Throwable
                            ) {
                                Log.e("POINTRESULT","용기내 onFailure : ${t.message} ");
                            }
                        })
                    } catch  (e: Exception) {
                        // Exception handling
                        Log.e(ContentValues.TAG, "Exception: ${e.message}", e)
                    }
                }
                mAlertDialog.dismiss()
                // UI 초기화
                toGoTxt.visibility = INVISIBLE
            }

            val noBtn = mDialogView.findViewById<Button>(R.id.noBtn)
            noBtn.setOnClickListener {
                mAlertDialog.dismiss()
            }
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

    // 사용자에게 권한 요청 후 결과에 대한 처리 로직
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()

            } else {
                Log.d("ttt", "onRequestPermissionsResult() _ 권한 허용 거부")
                Toast.makeText(this, "권한이 없어 해당 기능을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


}