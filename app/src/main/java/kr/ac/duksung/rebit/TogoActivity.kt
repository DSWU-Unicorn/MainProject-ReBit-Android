package kr.ac.duksung.rebit

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import kr.ac.duksung.rebit.databinding.ActivityTogoBinding
import kr.ac.duksung.rebit.network.RetofitClient
import kr.ac.duksung.rebit.network.RetrofitService
import kr.ac.duksung.rebit.network.dto.*
import net.daum.mf.map.api.CalloutBalloonAdapter
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class TogoActivity : AppCompatActivity(), MapView.POIItemEventListener,
    MapView.CurrentLocationEventListener { // TogoActivity
    private lateinit var retrofit: Retrofit
    private lateinit var retrofitService: RetrofitService

    private lateinit var binding: ActivityTogoBinding // 뷰 바인딩
    private lateinit var mMapView: MapView // 카카오 지도 뷰

    val storeList = ArrayList<StoreNameVO>()
    private val storeNameList = ArrayList<String>()

    var storeId: String = ""

    private val ACCESS_FINE_LOCATION = 1000     // Request Code


    // 사용자 현재 위치 트래킹을 위한 변수들
    private val gpsTAG = "MapTAG"
    private lateinit var currentMapPoint: MapPoint
    private var mCurrentLng: Double = 0.0
    private var mCurrentLat: Double = 0.0
    private var isTrackingMode = false //트래킹 모드인지 (현재위치 추적 눌렀을 경우 true되고 현재 위치 허용 안할시 false로 된다)

    // 상점 위치 좌표(위도, 경도)
    private var storeLatitude: Double = 0.0
    private var storeLongitude: Double = 0.0
    private var isArrived = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTogoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 키해시 구하기
        // getHashKey()

        //서버 연결
        initRetrofit()

        // 가게 이름 조회 통신
        // getStoreInfo()

        mMapView = MapView(this) // 카카오 지도 뷰
        // setCalloutBalloonAdapter: 마커를 추가하는 부분보다 앞에 있어야 커스텀 말풍선이 표시된다.
        mMapView.setCalloutBalloonAdapter(CustomBalloonAdapter(layoutInflater,
            retrofitService))  // 커스텀 말풍선 등록


        // 리스트 목록 클릭시
        setupEvents()


        // 포장하러 가는 중입니다.
        var toGoTxt = findViewById<TextView>(R.id.toGoTxt)

        /**
         * 검색
         */
        lifecycleScope.launch {
            try {
                retrofitService.getStoreAll().enqueue(object :
                    Callback<ApiResponse<ArrayList<StoreNameVO>>> {
                    override fun onResponse(
                        call: Call<ApiResponse<ArrayList<StoreNameVO>>>,
                        response: Response<ApiResponse<ArrayList<StoreNameVO>>>,
                    ) {
                        if (response.isSuccessful) {
                            // 통신 성공시
                            val result: ApiResponse<ArrayList<StoreNameVO>>? = response.body()
                            val datas = result?.getResult()

                            Log.d("StoreName", "onresponse 성공: " + result?.toString())
                            Log.d("StoreName", "data : " + datas?.toString())

                            for (data in datas!!) {
                                storeNameList.add(data.storeName)
                                storeList.add(data)
                                Log.d("storeList", "storeList : $storeList")
                            }

                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse<ArrayList<StoreNameVO>>>,
                        t: Throwable,
                    ) {
                        Log.e("StoreMarker", "onFailure : ${t.message} ")
                    }
                })
            } catch (e: Exception) {
                // Exception handling
                Log.e(ContentValues.TAG, "Exception: ${e.message}", e)
            }
        }

        val storeAdapter: ArrayAdapter<String> = ArrayAdapter(
            this, android.R.layout.simple_list_item_1,
            storeNameList
        )

        binding.searchView.isSubmitButtonEnabled = true

        binding.searchView.setOnQueryTextFocusChangeListener { searchView, hasFocus ->
            if (hasFocus) {
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
                lifecycleScope.launch {
                    try {
                        retrofitService.searchStoreByName(newText!!).enqueue(object :
                            Callback<ApiResponse<ArrayList<StoreNameVO2>>> {
                            override fun onResponse(
                                call: Call<ApiResponse<ArrayList<StoreNameVO2>>>,
                                response: Response<ApiResponse<ArrayList<StoreNameVO2>>>,
                            ) {
                                if (response.isSuccessful) {
                                    // 통신 성공시
                                    val result: ApiResponse<ArrayList<StoreNameVO2>>? =
                                        response.body()
                                    val datas = result?.getResult()

                                    Log.d(
                                        "SEARCH_STORE_NAME",
                                        "onresponse 성공: " + result?.toString()
                                    )
                                    Log.d("SEARCH_STORE_NAME", "data : " + datas?.toString())

                                    storeList.clear()
                                    storeNameList.clear()
                                    Log.d("SEARCH_STORE_NAME", "size1 : " + storeList.size)
                                    for (data in datas!!) {
                                        storeNameList.add(data.storeName)
                                        storeList.add(StoreNameVO(data.storeName, data.storeId))
                                    }
                                    Log.d("SEARCH_STORE_NAME", "storeList : $storeList")
                                    Log.d("SEARCH_STORE_NAME", "size2 : " + storeList.size)
                                    storeAdapter.notifyDataSetChanged()

                                }
                            }

                            override fun onFailure(
                                call: Call<ApiResponse<ArrayList<StoreNameVO2>>>,
                                t: Throwable,
                            ) {
                                Log.e("StoreMarker", "onFailure : ${t.message} ")
                            }
                        })
                    } catch (e: Exception) {
                        // Exception handling
                        Log.e(ContentValues.TAG, "Exception: ${e.message}", e)
                    }
                }

                return false
            }
        })

        //지도
        val mMapViewContainer = findViewById<ViewGroup>(R.id.map_mv_mapcontainer)
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

        // 230723
        // 현재 위치 업데이트를 위한 setCurrentLocationEventListener 등록
        // 230801
        // this에 MapView.CurrentLocationEventListener 구현
        mMapView.setCurrentLocationEventListener(this);
        //setCurrentLocationTrackingMode-> 지도랑 현재위치의 좌표를 찍어주고 따라다닌다
        mMapView.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading

        lifecycleScope.launch {
            try {
                // 사용자의 현재 위치 -> "구"로 가져와야 함.
                retrofitService.getStoreMarker("금천구")
                    .enqueue(object : // 임시적으로 줄여가게 많은 곳으로 하드코딩해 변경
                        Callback<ApiResponse<ArrayList<StoreMarkerVO>>> {
                        override fun onResponse(
                            call: Call<ApiResponse<ArrayList<StoreMarkerVO>>>,
                            response: Response<ApiResponse<ArrayList<StoreMarkerVO>>>,
                        ) {
                            if (response.isSuccessful) {
                                // 통신 성공시
                                val result: ApiResponse<ArrayList<StoreMarkerVO>>? = response.body()
                                val datas = result?.getResult()

                                val geocoder = Geocoder(applicationContext)

                                for (data in datas!!) {
                                    val address =
                                        geocoder.getFromLocationName(data.address, 10).get(0)
                                    Log.d("ADDRESS", "on response 성공: " + address.latitude)

                                    retrofitService.getStoreInfo(data.id)
                                        .enqueue(object :
                                            Callback<ApiResponse<StoreInfoVO>> {
                                            override fun onResponse(
                                                call: Call<ApiResponse<StoreInfoVO>>,
                                                response: Response<ApiResponse<StoreInfoVO>>,
                                            ) {
                                                if (response.isSuccessful) {
                                                    // 통신 성공시
                                                    val StoreInfoVO: ApiResponse<StoreInfoVO>? =
                                                        response.body()
                                                    val StoreInfoData = StoreInfoVO?.getResult()
                                                    val storeName = StoreInfoData?.storeName

//                                                    Log.d("가게이름*",
//                                                        "on response 성공: " + StoreInfoVO?.toString())
//                                                    Log.d("가게이름**",
//                                                        "data : " + StoreInfoData?.toString())
                                                    Log.d("가게이름***", "storeName : $storeName")


                                                    // 마커 생성 및 itemName 대입
                                                    val marker = MapPOIItem()
                                                    marker.apply {
                                                        mapPoint =
                                                            MapPoint.mapPointWithGeoCoord(
                                                                address.latitude,
                                                                address.longitude
                                                            )
                                                        Log.d("상점 위도경도테스트", "$mapPoint")

                                                        // exception
                                                        itemName = storeName
                                                            ?: "" // Assign the storeName to itemName, or empty string if null

                                                        markerType =
                                                            MapPOIItem.MarkerType.CustomImage          // 마커 모양 (커스텀)
                                                        customImageResourceId =
                                                            R.drawable.map_pin_blue            // 커스텀 마커 이미지
                                                        selectedMarkerType =
                                                            MapPOIItem.MarkerType.CustomImage  // 클릭 시 마커 모양 (커스텀)
                                                        customSelectedImageResourceId =
                                                            R.drawable.dagom_marker_resize    // 클릭 시 커스텀 마커 이미지
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
                                                Log.e("가게이름", "onFailure : ${t.message} ")
                                            }
                                        })

                                }
                                // 기존 코드
                                //                                marker.mapPoint = MapPoint.mapPointWithGeoCoord(address.latitude,
                                //                                    address.longitude)
                                //                                marker.itemName = data.id.toString()
                                //           mMapView.addPOIItem(marker)
                                //     }

                                Log.d("StoreMarker", "on response 성공: $result")
                                Log.d("StoreMarker", "data : $datas")

                            }
                        }

                        override fun onFailure(
                            call: Call<ApiResponse<ArrayList<StoreMarkerVO>>>,
                            t: Throwable,
                        ) {
                            Log.e("StoreMarker", "onFailure : ${t.message} ")
                        }
                    })
            } catch (e: Exception) {
                // Exception handling
                Log.e(ContentValues.TAG, "Exception: ${e.message}", e)
            }

        }

        // 포장하러가는중입니다...
        // intent 얻기
        val status = intent.getStringExtra("status")
        val store_id = intent.getStringExtra("store_id")

        Log.i("STATUS", status.toString()) // ok

        if (status.toBoolean()) {
            // 2. api 호출
            lifecycleScope.launch {
                try {
                    retrofitService.getStoreAddressTogo(Integer.parseInt(store_id))
                        ?.enqueue(object :
                            Callback<ApiResponse<StoreAddressVO>> {
                            override fun onResponse(
                                call: Call<ApiResponse<StoreAddressVO>>,
                                response: Response<ApiResponse<StoreAddressVO>>,
                            ) {
                                if (response.isSuccessful) {
                                    // 통신 성공시
                                    val result: ApiResponse<StoreAddressVO>? = response.body()
                                    val datas = result?.getResult()

                                    Log.d("MAINRESULT", "onresponse 성공: " + result?.toString())
                                    Log.d("MAINRESULT", "data : " + datas?.address)

                                    var geocoder = Geocoder(applicationContext)
                                    val fromLocationName =
                                        geocoder.getFromLocationName(datas.toString(), 1)
                                    Log.d("MAINRESULT",
                                        "LONGITUDE : " + fromLocationName.get(0).longitude.toString())
                                    Log.d("MAINRESULT",
                                        "LATITUDE : " + fromLocationName.get(0).latitude.toString())
                                    // 가게 주소->좌표로 변환한 값
                                    storeLatitude = fromLocationName.get(0).latitude
                                    storeLongitude = fromLocationName.get(0).longitude
                                }
                            }

                            override fun onFailure(
                                call: Call<ApiResponse<StoreAddressVO>>,
                                t: Throwable,
                            ) {
                                Log.e("MAINRESULT", "onFailure : ${t.message} ");
                            }
                        })
                } catch (e: Exception) {
                    // Exception handling
                    Log.e(ContentValues.TAG, "Exception: ${e.message}", e)
                }
            }
            // 3. ui 변경
            toGoTxt.visibility = View.VISIBLE
        }

    }//OnCreate()

//    private fun setCurrentLocationEventListener() {
//        // p1: MapPoint 객체 (사용자의 위치 좌표)
//        // p2: accuracyInMeters(위치 정확도인 accuracyInMeters (단위: 미터))
////    private fun setCurrentLocationEventListener() {
////        mMapView.setCurrentLocationEventListener(object : MapView.CurrentLocationEventListener {
//        fun onCurrentLocationUpdate(p0: MapView?, p1: MapPoint?, p2: Float) {
//            val mapPointGeo = p1?.mapPointGeoCoord
//            if (mapPointGeo != null) {
//                Log.i("onCurrentLocationUpdate",
//                    String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)",
//                        mapPointGeo.latitude,
//                        mapPointGeo.longitude,
//                        p2))
//            }
//            var currentMapPoint =
//                mapPointGeo?.let { MapPoint.mapPointWithGeoCoord(it.latitude, mapPointGeo.longitude) }
//            //이 좌표로 지도 중심 이동
//            mMapView.setMapCenterPoint(currentMapPoint, true)
//            //전역변수로 현재 좌표 저장
//            var mCurrentLat = mapPointGeo?.latitude
//            var mCurrentLng = mapPointGeo?.longitude
//            Log.d("onCurrentLocationUpdate", "현재위치 => " + mCurrentLat.toString() + "  " + mCurrentLng)
//            // mLoaderLayout.setVisibility(View.GONE) // 레이아웃 숨기기
//
//            //트래킹 모드가 아닌 단순 현재위치 업데이트일 경우, 한번만 위치 업데이트하고 트래킹을 중단시키기 위한 로직
//            if (!isTrackingMode) {
//                mMapView.currentLocationTrackingMode =
//                    MapView.CurrentLocationTrackingMode.TrackingModeOff
//            }
//        }
//
//        fun onCurrentLocationDeviceHeadingUpdate(mapView: MapView?, v: Float) {}
//
//        fun onCurrentLocationUpdateFailed(mapView: MapView?) {
//            Log.i("onCurrentLocationUpdate", "onCurrentLocationUpdateFailed")
//            mMapView.currentLocationTrackingMode =
//                MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
//        }
//
//        fun onCurrentLocationUpdateCancelled(mapView: MapView?) {
//            Log.i("onCurrentLocationUpdate", "onCurrentLocationUpdateCancelled")
//            mMapView.currentLocationTrackingMode =
//                MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
//        }
//    }


    //서버 연결
    private fun initRetrofit() {
        retrofit = RetofitClient.getInstance()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    private fun setupEvents() {
        // 메인화면의 이벤트관련 코드를 모아두는 장소
        // 리스트 클릭 이벤트 - 리스트뷰의 각 줄이 눌리는 시점의 이벤트
        binding.storeList.setOnItemClickListener { userAdapter, view, i, l ->
            // 눌린 위치에 해당하는 목록이 어떤 목록인지 가져오기
            try {
                Log.d("클릭", storeList[i].storeId.toString())
                val intent = Intent(this, StoreDetailActivity::class.java)
                intent.putExtra("store_id", storeList[i].storeId.toString())
                startActivity(intent)
            } catch (e: IndexOutOfBoundsException) {
                Toast.makeText(this, "Oops. 더이상의 가게 정보가 없어요", Toast.LENGTH_SHORT).show()
            }
        }
    }// setupEvents


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
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    // 위치추적 시작
    private fun startTracking() {
        mMapView.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading // 계속 따라옴
        isTrackingMode = true

        // 사용자 현위치 트래킹 기능 켜짐
        mMapView.setCustomCurrentLocationMarkerTrackingImage(R.drawable.custom_map_present_tracking,
            MapPOIItem.ImageOffset(16, 16))


    }

    // 위치추적 중지
//    private fun stopTracking() {
//        mMapView.currentLocationTrackingMode =
//            MapView.CurrentLocationTrackingMode.TrackingModeOff
//    }

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
        // 말풍선 클릭시 - 더 많은 정보 보러가기
        mapView: MapView?,
        marker: MapPOIItem?,
        calloutBalloonButtonType: MapPOIItem.CalloutBalloonButtonType?,
    ) {
        val storeName = marker?.itemName // Get the storeId from the clicked marker's itemName
        if (storeName != null) {
            retrofitService.getMarkerInfo(storeName)
                .enqueue(object : Callback<ApiResponse<MarkerInfoVO>> {
                    override fun onResponse(
                        call: Call<ApiResponse<MarkerInfoVO>>,
                        response: Response<ApiResponse<MarkerInfoVO>>,
                    ) {
                        if (response.isSuccessful) {
                            // success
                            val result: ApiResponse<MarkerInfoVO>? = response.body()
                            val data = result?.getResult()

                            Log.d("getMarkerInfo", "on response 성공: " + result?.toString())
                            Log.d("getMarkerInfo", "data : " + data?.toString())

                            if (data != null) {
                                storeId = data.id.toString()
                            }
                            // 가게 상세 페이지로 store_id값 넘기기.
                            //
                            val intent = Intent(this@TogoActivity, StoreDetailActivity::class.java)
                            intent.putExtra("store_id", storeId)
                            this@TogoActivity.startActivity(intent) // Use the TogoActivity context to start the activity

                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<MarkerInfoVO>>, t: Throwable) {
                        Log.e("getMarkerInfo", "onFailure : ${t.message} ")
                    }

                })
        }
    }

    // 커스텀 말풍선 클래스
    class CustomBalloonAdapter(
        inflater: LayoutInflater,
        private val retrofitService: RetrofitService, // Replace YourRetrofitService with your actual Retrofit service class
    ) : CalloutBalloonAdapter {
        val mCalloutBalloon: View = inflater.inflate(R.layout.custom_balloon_layout, null)
        val name: TextView = mCalloutBalloon.findViewById(R.id.ball_tv_name)
//        val address: TextView = mCalloutBalloon.findViewById(R.id.ball_tv_address)

        override fun getCalloutBalloon(poiItem: MapPOIItem?): View {
            // 마커 클릭 시 나오는 말풍선
            name.text = poiItem?.itemName   // 해당 마커의 정보 이용

            return mCalloutBalloon
        }

        override fun getPressedCalloutBalloon(p0: MapPOIItem?): View {

            return mCalloutBalloon
        }
    }

    override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {
        TODO("Not yet implemented")
    }


    private fun MapView.setOpenAPIKeyAuthenticationResultListener(togoActivity: TogoActivity) {

    }

//private fun MapView.setPOIItemEventListener(togoActivity: TogoActivity) {
//}

    private fun MapView.setMapViewEventListener(togoActivity: TogoActivity) {

    }

    private fun ListView.contains(view: String?): Boolean {
        return true
    }

//    override fun onCurrentLocationUpdate(p0: MapView?, p1: MapPoint?, p2: Float) {
//
//        Log.d("onCurrentLocationUpdate", "현재위치 => ")
//    }

    /*
     *  현재 위치 업데이트(setCurrentLocationEventListener)
     */
    override fun onCurrentLocationUpdate(
        mapView: MapView?,
        mapPoint: MapPoint,
        accuracyInMeters: Float,
    ) {
        val mapPointGeo = mapPoint.mapPointGeoCoord
        Log.i(gpsTAG,
            String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)",
                mapPointGeo.latitude,
                mapPointGeo.longitude,
                accuracyInMeters))
        currentMapPoint = MapPoint.mapPointWithGeoCoord(mapPointGeo.latitude, mapPointGeo.longitude)
        //이 좌표로 지도 중심 이동
        mMapView.setMapCenterPoint(currentMapPoint, true)
        //전역변수로 현재 좌표 저장
        mCurrentLat = mapPointGeo.latitude
        mCurrentLng = mapPointGeo.longitude
        Log.d(gpsTAG, "현재위치 => $mCurrentLat  $mCurrentLng")

        // 현재 좌표 - 가게 좌표 <= 250이라면 포장하셨습니까 다이얼로그 띄우기
        val distance =
            DistanceManager.getDistance(mCurrentLat, mCurrentLng, storeLatitude, storeLongitude)
        Log.d("반경250", distance.toString())


        if (distance <= 250) {
            isArrived = true
        }

        if (isArrived) {
            // 5. 다이얼로그
            // Dialog만들기
            val mDialogView =
                LayoutInflater.from(this).inflate(R.layout.after_togo_dialog, null)
            val mBuilder = androidx.appcompat.app.AlertDialog.Builder(this)
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
                                response: Response<ApiResponse<Int>>,
                            ) {
                                if (response.isSuccessful) {
                                    // 통신 성공시
                                    val result: ApiResponse<Int>? = response.body()
                                    val datas = result?.getResult()

                                    Log.d("POINTRESULT", "용기내 onresponse 성공: " + result?.toString())
                                    Log.d("POINTRESULT", "용기내 data : " + datas?.toString())
                                }
                            }

                            override fun onFailure(
                                call: Call<ApiResponse<Int>>,
                                t: Throwable,
                            ) {
                                Log.e("POINTRESULT", "용기내 onFailure : ${t.message} ");
                            }
                        })
                    } catch (e: Exception) {
                        // Exception handling
                        Log.e(ContentValues.TAG, "Exception: ${e.message}", e)
                    }
                }
                mAlertDialog.dismiss()
                // UI 초기화
                //toGoTxt.visibility = View.INVISIBLE
            }

            val noBtn = mDialogView.findViewById<Button>(R.id.noBtn)
            noBtn.setOnClickListener {
                mAlertDialog.dismiss()
            }
        }


        //        mLoaderLayout.setVisibility(View.GONE)
        // 트래킹 모드가 아닌 단순 현재위치 업데이트일 경우, 한번만 위치 업데이트하고 트래킹을 중단시키기 위한 로직
        if (!isTrackingMode) {
            mMapView.currentLocationTrackingMode =
                MapView.CurrentLocationTrackingMode.TrackingModeOff
        }
    }


    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {
    }

    override fun onCurrentLocationUpdateFailed(p0: MapView?) {
        Log.i(gpsTAG, "onCurrentLocationUpdateFailed")
        mMapView.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
    }

    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {
        Log.i(gpsTAG, "onCurrentLocationUpdateCancelled")
        mMapView.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
    }

}