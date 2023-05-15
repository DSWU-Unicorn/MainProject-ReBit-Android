package kr.ac.duksung.rebit

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.contains
import androidx.lifecycle.lifecycleScope
import com.unity3d.player.e
import kotlinx.android.synthetic.main.activity_store_detail.*
import kotlinx.coroutines.launch
import kr.ac.duksung.rebit.databinding.ActivityTogoBinding
import kr.ac.duksung.rebit.datas.Store
import kr.ac.duksung.rebit.network.RetofitClient
import kr.ac.duksung.rebit.network.RetrofitService
import kr.ac.duksung.rebit.network.dto.ApiResponse
import kr.ac.duksung.rebit.network.dto.StoreInfoVO
import kr.ac.duksung.rebit.network.dto.StoreMarkerVO
import net.daum.mf.map.api.CalloutBalloonAdapter
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

interface StoreMarkerCallback {
    fun onStoreMarkerReceived(storeId: Long)
}

class TogoActivity : AppCompatActivity(), MapView.POIItemEventListener,
    StoreMarkerCallback { // TogoActivity
    private lateinit var retrofit: Retrofit
    private lateinit var retrofitService: RetrofitService

    private lateinit var binding: ActivityTogoBinding // 뷰 바인딩
    private lateinit var mMapView: MapView // 카카오 지도 뷰

    // store_id 반환해 사용하기 위한 코드
    private var storeMarkerCallback: StoreMarkerCallback? = null
    private var storeMarkerId: Long = 0 // Variable to hold the data.id value


    // 정적인 arrayOf 대신 ArrayList 사용(4/8 토 14:30~15:44)
    val storeList = ArrayList<Store>();
    //lateinit var storeAdapter: StoreAdapter

    private val user = arrayOf(
        // 통신한 후 가게이름으로 변경 필요.
        "메가커피",
        "커피드림",
        "콩블랑제리",
        "블랙다운커피",
        "히피스 베이글",
    )

    private val ACCESS_FINE_LOCATION = 1000     // Request Code

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTogoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 키해시 구하기
        // getHashKey()
        //
        supportActionBar?.hide();      // 액션바가 안 보이도록 // 근데 안먹네;;

        //서버 연결
        initRetrofit()

        // 가게 이름 조회 통신
        //getStoreInfo()


        mMapView = MapView(this) // 카카오 지도 뷰
        // setCalloutBalloonAdapter: 마커를 추가하는 부분보다 앞에 있어야 커스텀 말풍선이 표시된다.
        val storeId =         fetchStoreMarkers()

//        mMapView.setCalloutBalloonAdapter(CustomBalloonAdapter(layoutInflater, storeId))  // 커스텀 말풍선 등록

        // Set the storeMarkerCallback to the activity
        storeMarkerCallback = this
        // Call the function that contains the API communication code
        fetchStoreMarkers()

        // 리스트 목록 클릭시
        setupEvents()
        // 목록 값 지정
        // setValues()

        var storeAdapter: ArrayAdapter<String> = ArrayAdapter(
            this, android.R.layout.simple_list_item_1,
            user
        )

        binding.searchView.isSubmitButtonEnabled = true

        binding.searchView.setOnQueryTextFocusChangeListener { searchView, hasFocus ->
            if (hasFocus) {
                // focus 를 가지고 있는 경우에만 가게목록이 뜨도록.
                // binding.user.adapter = userAdapter;
                // 오류 발생. 해결 -> 해당 액티비티의 xml id 놓침.
                binding.storeList.adapter = storeAdapter
            }
        }
        // 가게명 검색 시 필터기능
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.searchView.clearFocus()
                if (binding.storeList.contains(query)) {
                    storeAdapter.filter.filter(query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                storeAdapter.filter.filter(newText)
                return false
            }
        })
        // 검색돋보기 클릭시
//        binding.searchView.setOnClickListener {
//            // Dialog만들기
//            val mDialogView = LayoutInflater.from(this).inflate(R.layout.store_info_dialog, null)
//            val mBuilder = AlertDialog.Builder(this)
//                .setView(mDialogView)
//                .setTitle("가게 정보")
//            mBuilder.show()
//
//            val pic_btn = mDialogView.findViewById<Button>(R.id.pic_btn)
//            pic_btn.setOnClickListener {
//                Toast.makeText(this, "내 용기가 맞을까? 확인하러 가기", Toast.LENGTH_SHORT).show()
//                val intent = Intent(this, CameraActivity::class.java)
//                startActivity(intent)
//
//                // action bar show
//                getSupportActionBar()?.show();
//            }
//            val goto_review_btn = mDialogView.findViewById<Button>(R.id.goto_review_btn)
//
//            goto_review_btn.setOnClickListener {
//                Toast.makeText(this, "생생한 후기가 궁금하나요? 리뷰 보러 가기", Toast.LENGTH_SHORT).show()
//                val intent = Intent(this, ReviewActivity::class.java)
//                startActivity(intent)
//            }
//            // review create
//            val todo_btn = mDialogView.findViewById<Button>(R.id.todo_btn)
//
//            todo_btn.setOnClickListener {
//                Toast.makeText(this, "이미 용기냈다면! 어땠는지 후기 작성하러 가기", Toast.LENGTH_SHORT).show()
//
//                val intent = Intent(this, CreateReviewActivity::class.java)
//                startActivity(intent)
//            }
//        }

        //지도
        val mMapViewContainer = findViewById(R.id.map_mv_mapcontainer) as ViewGroup
        mMapViewContainer.addView(mMapView)

        if (checkLocationService()) {
            // GPS가 켜져있을 경우
            permissionCheck()
        } else {
            // GPS가 꺼져있을 경우
            Toast.makeText(this, "GPS를 켜주세요", Toast.LENGTH_SHORT).show()
        }

        // 리스너 등록
        mMapView.setMapViewEventListener(this) // this에 MapView.MapViewEventListener 구현.
        mMapView.setPOIItemEventListener(this)
        mMapView.setOpenAPIKeyAuthenticationResultListener(this)


    }//OnCreate()

    // Function to fetch store markers
    private fun fetchStoreMarkers() {
        lifecycleScope.launch {
            try {
                retrofitService.getStoreMarker("도봉구")
                    ?.enqueue(object :
                        Callback<ApiResponse<ArrayList<StoreMarkerVO>>> {
                        override fun onResponse(
                            call: Call<ApiResponse<ArrayList<StoreMarkerVO>>>,
                            response: Response<ApiResponse<ArrayList<StoreMarkerVO>>>,
                        ) {
                            if (response.isSuccessful) {
                                // 통신 성공시
                                val result: ApiResponse<ArrayList<StoreMarkerVO>>? =
                                    response.body()
                                val datas = result?.getResult()

                                var geocoder = Geocoder(applicationContext)

                                for (data in datas!!) {
                                    var address =
                                        geocoder.getFromLocationName(data.address, 10).get(0)
                                    Log.d("ADDRESS", "on response 성공: " + address.latitude)

                                    // callback with storeId
                                    storeMarkerCallback?.onStoreMarkerReceived(
                                        data.id ?: 0)
                                    // getStoreInfo 통신
                                    retrofitService.getStoreInfo(data.id.toLong())
                                        ?.enqueue(object :
                                            Callback<ApiResponse<StoreInfoVO>> {
                                            override fun onResponse(
                                                call: Call<ApiResponse<StoreInfoVO>>,
                                                response: Response<ApiResponse<StoreInfoVO>>,
                                            ) {
                                                if (response.isSuccessful) {
                                                    // 통신 성공시
                                                    val result: ApiResponse<StoreInfoVO>? =
                                                        response.body()
                                                    val data = result?.getResult()
                                                    val storeName = data?.storeName

                                                    Log.d("가게이름*",
                                                        "on response 성공: " + result?.toString())
                                                    Log.d("가게이름**",
                                                        "data : " + data?.toString())
                                                    Log.d("가게이름***", "storeName : $storeName")

                                                    // 마커 생성 및 itemName 대입
                                                    var marker = MapPOIItem()
                                                    marker.apply {
                                                        mapPoint =
                                                            MapPoint.mapPointWithGeoCoord(
                                                                address.latitude,
                                                                address.longitude
                                                            )
                                                        // 가게 이름
//                                                        itemName =
//                                                            data?.storeName.toString() // Assign the storeName to itemName
                                                        itemName = storeName
                                                            ?: "" // Assign the storeName to itemName, or empty string if null

                                                        markerType =
                                                            MapPOIItem.MarkerType.CustomImage          // 마커 모양 (커스텀)
                                                        customImageResourceId =
                                                            R.drawable.map_pin_blue            // 커스텀 마커 이미지
                                                        selectedMarkerType =
                                                            MapPOIItem.MarkerType.CustomImage  // 클릭 시 마커 모양 (커스텀)
                                                        customSelectedImageResourceId =
                                                            R.drawable.dagom     // 클릭 시 커스텀 마커 이미지
                                                        isCustomImageAutoscale =
                                                            false      // 커스텀 마커 이미지 크기 자동 조정
                                                        setCustomImageAnchor(0.5f, 1.0f)
                                                    }
                                                    mMapView.addPOIItem(marker)


                                                }
                                            }

                                            override fun onFailure(
                                                call: Call<ApiResponse<StoreInfoVO>>,
                                                t: Throwable,
                                            ) {
                                                Log.e("가게이름", "onFailure : ${t.message} ");
                                            }
                                        })

//                                        mapPoint = MapPoint.mapPointWithGeoCoord(address.latitude,
//                                            address.longitude)
//                                        markerType =
//                                            MapPOIItem.MarkerType.CustomImage          // 마커 모양 (커스텀)
//                                        customImageResourceId =
//                                            R.drawable.map_pin_blue            // 커스텀 마커 이미지
//                                        selectedMarkerType =
//                                            MapPOIItem.MarkerType.CustomImage  // 클릭 시 마커 모양 (커스텀)
//                                        customSelectedImageResourceId =
//                                            R.drawable.dagom     // 클릭 시 커스텀 마커 이미지
//                                        isCustomImageAutoscale = false      // 커스텀 마커 이미지 크기 자동 조정
//                                        setCustomImageAnchor(0.5f, 1.0f)    // 마커 이미지 기준점
                                }
                                // 기존 코드
//                                marker.mapPoint = MapPoint.mapPointWithGeoCoord(address.latitude,
//                                    address.longitude)
//                                marker.itemName = data.id.toString()
                                //           mMapView.addPOIItem(marker)
                                //     }

                                Log.d("StoreMarker", "onresponse 성공: " + result?.toString())
                                Log.d("StoreMarker", "data : " + datas?.toString())

                            }
                        }

                        override fun onFailure(
                            call: Call<ApiResponse<ArrayList<StoreMarkerVO>>>,
                            t: Throwable,
                        ) {
                            Log.e("StoreMarker", "onFailure : ${t.message} ");
                        }
                    })
            } catch (e: Exception) {
                // Exception handling
                Log.e(ContentValues.TAG, "Exception: ${e.message}", e)
            }
        }
    }

    //서버 연결
    private fun initRetrofit() {
        retrofit = RetofitClient.getInstance()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    fun setupEvents() {
        // 메인화면의 이벤트관련 코드를 모아두는 장소
        // 리스트 클릭 이벤트 - 리스트뷰의 각 줄이 눌리는 시점의 이벤트
        binding.storeList.setOnItemClickListener { userAdapter, view, i, l ->
            // 눌린 위치에 해당하는 목록이 어떤 목록인지 가져오기
            try {
                val clickedStore = storeList[i]
                // 선택된 목록정보를 가져왔으면 이제 화면 이동
                val myIntent = Intent(this, StoreDetailActivity::class.java)
                // 정보를 담아주기
                myIntent.putExtra("storeInfo", clickedStore)
                // 화면 전환
                startActivity(myIntent)
            } catch (e: IndexOutOfBoundsException) {
                Toast.makeText(this, "Oops. 더이상의 가게 정보가 없어요", Toast.LENGTH_SHORT).show()
            }
        }
    }// setupEvents

//    fun setValues() {
//        // test data 삽입 // 통신 후 데이터로 변경 필요
//        storeList.add(Store(0,
//            "메가MGC커피 4.19사거리점",
//            "휴게음식점",
//            "서울 강북구 삼양로 510 1층 메가커피",
//            "02-900-1288"))
//        storeList.add(Store(1, "커피드림", "휴게음식점", "서울특별시 도봉구 삼양로144길 25", "01022360284"))
//        storeList.add(Store(2, "eeeyo", "휴게음식점", "서울 도봉구 삼양로142길 33 일층", "0507-1323-2307"))
//        storeList.add(Store(3, "블랙다운커피", "휴게음식점", "서울 강북구 삼양로 528-1 1층", "02-6338-0606"))
//        storeList.add(Store(4, "히피스 베이글", "휴게음식점", "서울 강북구 삼양로 528", "02-906-6778"))
//
//        //storeAdapter = StoreAdapter(this, android.R.layout.simple_list_item_1, storeList)
//        //binding.searchView.adapter = storeAdapter
//    }

    // 위치 권한 확인
    private fun permissionCheck() {
        val preference = getPreferences(MODE_PRIVATE)
        val isFirstCheck = preference.getBoolean("isFirstPermissionCheck", true)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한이 없는 상태
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // 권한 거절 (다시 한 번 물어봄)
                val builder = AlertDialog.Builder(this)
                builder.setMessage("현재 위치를 확인하시려면 위치 권한을 허용해주세요.")
                builder.setPositiveButton("확인") { dialog, which ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        ACCESS_FINE_LOCATION
                    )
                }
                builder.setNegativeButton("취소") { dialog, which ->

                }
                builder.show()
            } else {
                if (isFirstCheck) {
                    // 최초 권한 요청
                    preference.edit().putBoolean("isFirstPermissionCheck", false).apply()
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        ACCESS_FINE_LOCATION
                    )
                } else {
                    // 다시 묻지 않음 클릭 (앱 정보 화면으로 이동)
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("현재 위치를 확인하시려면 설정에서 위치 권한을 허용해주세요.")
                    builder.setPositiveButton("설정으로 이동") { dialog, which ->
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:$packageName")
                        )
                        startActivity(intent)
                    }
                    builder.setNegativeButton("취소") { dialog, which ->

                    }
                    builder.show()
                }
            }
        } else {
            // 권한이 있는 상태
            startTracking()
        }
    }

    // 권한 요청 후 행동
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 요청 후 승인됨 (추적 시작)
                Toast.makeText(this, "위치 권한이 승인되었습니다", Toast.LENGTH_SHORT).show()
                startTracking()
            } else {
                // 권한 요청 후 거절됨 (다시 요청 or 토스트)
                Toast.makeText(this, "위치 권한이 거절되었습니다", Toast.LENGTH_SHORT).show()
                permissionCheck()
            }
        }
    }

    // GPS가 켜져있는지 확인
    private fun checkLocationService(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    // 위치추적 시작
    private fun startTracking() {
        mMapView.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
        mMapView.setCustomCurrentLocationMarkerTrackingImage(R.drawable.custom_poi_marker_start,
            MapPOIItem.ImageOffset(16, 16))

//        val lm: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//         val userNowLocation: Location? = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
//        // 위도 , 경도
//        val uLatitude = userNowLocation?.latitude
//        val uLongitude = userNowLocation?.longitude
//        val uNowPosition = MapPoint.mapPointWithGeoCoord(uLatitude!!, uLongitude!!)
//
//        // 현 위치에 마커 찍기
//        val marker = MapPOIItem()
//        marker.apply {
//            // marker.itemName = "현재 위치"
//            mapPoint = uNowPosition
//            markerType =
//                MapPOIItem.MarkerType.CustomImage          // 마커 모양 (커스텀)
//            customImageResourceId =
//                R.drawable.custom_poi_marker_start          // 커스텀 마커 이미지
//            // 클릭 시 어떤 이벤트를 보여줘야 할지 고민.
////            selectedMarkerType =
////                MapPOIItem.MarkerType.CustomImage  // 클릭 시 마커 모양 (커스텀)
////            customSelectedImageResourceId =
////                R.drawable.custom_map_present_direction     // 클릭 시 커스텀 마커 이미지
//            isCustomImageAutoscale = true      // 커스텀 마커 이미지 크기 자동 조정
//            setCustomImageAnchor(0.5f, 1.0f)    // 마커 이미지 기준점
//        }
//        mMapView.addPOIItem(marker)
    }

    // 위치추적 중지
    private fun stopTracking() {
        mMapView.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOff
    }

    // 마커 클릭 이벤트
    override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {
        // 마커 클릭시 말풍선 띄우도록.
    }

    @Deprecated("Deprecated in Java")
    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {
        // 말풍선 클릭 시 (Deprecated)
        // 이 함수도 작동하지만 그냥 아래 있는 함수에 작성하자
    }

    override fun onCalloutBalloonOfPOIItemTouched(
        p0: MapView?,
        p1: MapPOIItem?,
        p2: MapPOIItem.CalloutBalloonButtonType?,
    ) {
        // 해당 method 에 대해
        val intent = Intent(this, StoreDetailActivity::class.java)
        intent.putExtra("store_id",
            p1?.itemName) // id를 넘길때 말풍선 이름으로 해놨구나.. -> 현재는 이름으로 변경해서 예외 발생함.
        startActivity(intent)
    }

    override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {
        TODO("Not yet implemented")
    }

    // 커스텀 말풍선 클래스
    class CustomBalloonAdapter(inflater: LayoutInflater, private val context: Context) : CalloutBalloonAdapter {
        val mCalloutBalloon: View = inflater.inflate(R.layout.custom_balloon_layout, null)
        val name: TextView = mCalloutBalloon.findViewById(R.id.ball_tv_name)
        val address: TextView = mCalloutBalloon.findViewById(R.id.ball_tv_address)

        override fun getCalloutBalloon(poiItem: MapPOIItem?): View {
            // 마커 클릭 시 나오는 말풍선
            name.text = poiItem?.itemName   // 해당 마커의 정보 이용 가능
            address.text = "getCalloutBalloon" // 통신후 가게 주소 띄울 예정

            return mCalloutBalloon
        }

        override fun getPressedCalloutBalloon(p0: MapPOIItem?): View {
            TODO("Not yet implemented")
        }

        fun getPressedCalloutBalloon(): View {
            // 말풍선 클릭 시
//            val storeId = poiItem?.itemName?.toLongOrNull()
            val storeId = storeMarkerId

            if (storeId != null) {
                val intent = Intent(context, StoreDetailActivity::class.java)
                intent.putExtra("store_id", storeId)
                context.startActivity(intent)
            }

            return mCalloutBalloon
        }

        companion object {
            var storeMarkerId: Long? = null
        }
//        fun getPressedCalloutBalloon(storeId: Long): View {
//            // 말풍선 클릭 시
//            // address.text = "getPressedCalloutBalloon"
//            // 주석 해지하면 클릭시 위 text 보이는데, 바로 intent로 넘어가야 하니까 주석함.
//
//
//            val intent = Intent(this, StoreDetailActivity::class.java)
////            intent.putExtra("store_id", poiItem?.itemName) // id를 넘길때 말풍선 이름으로 해놨구나.. -> 현재는 이름으로 변경해서 예외 발생함.
//            intent.putExtra("store_id", storeId) // id를 넘길때 말풍선 이름으로 해놨구나.. -> 현재는 이름으로 변경해서 예외 발생함.
//            startActivity(intent)
//
//            return mCalloutBalloon
//
//        }
    }

    override fun onStoreMarkerReceived(storeId: Long) {
        // Assign the received storeId to the storeMarkerId variable
        CustomBalloonAdapter.storeMarkerId = storeId

        // Get the instance of CustomBalloonAdapter and call getPressedCalloutBalloon()
        val balloonAdapter = CustomBalloonAdapter(layoutInflater, this)
        balloonAdapter?.getPressedCalloutBalloon()
    }
    // kakaoMap 앱 키 해시 얻어오는 메소드. // Logcat 에 KEY_HASH 입력 후 나오는 값 확인할 것!
//    fun getHashKey(){
//        var packageInfo : PackageInfo = PackageInfo()
//        try {
//            packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
//        } catch (e: PackageManager.NameNotFoundException){
//            e.printStackTrace()
//        }
//
//        for (signature: Signature in packageInfo.signatures){
//            try{
//                var md: MessageDigest = MessageDigest.getInstance("SHA")
//                md.update(signature.toByteArray())
//                Log.e("KEY_HASH", Base64.encodeToString(md.digest(), Base64.DEFAULT))
//            } catch(e: NoSuchAlgorithmException){
//                Log.e("KEY_HASH", "Unable to get MessageDigest. signature = " + signature, e)
//            }
//        }
//    }

}


private fun MapView.setOpenAPIKeyAuthenticationResultListener(togoActivity: TogoActivity) {

}

//private fun MapView.setPOIItemEventListener(togoActivity: TogoActivity) {
//}

private fun MapView.setMapViewEventListener(togoActivity: TogoActivity) {

}

//private fun MapView.setCurrentLocationEventListener(togoActivity: TogoActivity) {
//
//}

private fun ListView.contains(view: String?): Boolean {
    return true
}

