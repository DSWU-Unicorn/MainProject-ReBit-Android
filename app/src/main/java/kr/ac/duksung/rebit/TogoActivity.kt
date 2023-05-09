

package kr.ac.duksung.rebit

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_togo.*
import kotlinx.coroutines.launch
import kr.ac.duksung.rebit.databinding.ActivityTogoBinding
import kr.ac.duksung.rebit.datas.Store
import kr.ac.duksung.rebit.network.RetofitClient
import kr.ac.duksung.rebit.network.RetrofitService
import kr.ac.duksung.rebit.network.dto.ApiResponse
import kr.ac.duksung.rebit.network.dto.StoreMarkerVO
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class TogoActivity : AppCompatActivity() ,MapView.POIItemEventListener {
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService

    private lateinit var binding: ActivityTogoBinding
    private lateinit var mMapView: MapView // Declare the mMapView variable

    // 정적인 arrayOf 대신 ArrayList 사용(4/8 토 14:30~15:44)
    val storeList = ArrayList<Store>();
    //lateinit var storeAdapter: StoreAdapter

    private val user = arrayOf(
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

        //
        getSupportActionBar()?.hide();      // 안 보이도록 합니다.

        //서버 연결
        initRetrofit()

        mMapView = MapView(this)


        // 리스트 목록 클릭시
        setupEvents()
        // 목록 값 지정
        setValues()

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
        binding.searchView.setOnClickListener {
            // Dialog만들기
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.store_info_dialog, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("가게 정보")
            mBuilder.show()

            val pic_btn = mDialogView.findViewById<Button>(R.id.pic_btn)
            pic_btn.setOnClickListener {
                Toast.makeText(this, "내 용기가 맞을까? 확인하러 가기", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, CameraActivity::class.java)
                startActivity(intent)

                // action bar show
                getSupportActionBar()?.show();
            }
            val goto_review_btn = mDialogView.findViewById<Button>(R.id.goto_review_btn)

            goto_review_btn.setOnClickListener {
                Toast.makeText(this, "생생한 후기가 궁금하나요? 리뷰 보러 가기", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ReviewActivity::class.java)
                startActivity(intent)
            }
            // review create
            val todo_btn = mDialogView.findViewById<Button>(R.id.todo_btn)

            todo_btn.setOnClickListener {
                Toast.makeText(this, "이미 용기냈다면! 어땠는지 후기 작성하러 가기", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, CreateReviewActivity::class.java)
                startActivity(intent)
            }
        }

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

        lifecycleScope.launch {
            try {
                retrofitService.getStoreMarker("강남구")?.enqueue(object :
                    Callback<ApiResponse<ArrayList<StoreMarkerVO>>> {
                    override fun onResponse(
                        call: Call<ApiResponse<ArrayList<StoreMarkerVO>>>,
                        response: Response<ApiResponse<ArrayList<StoreMarkerVO>>>
                    ){
                        if(response.isSuccessful){
                            // 통신 성공시
                            val result: ApiResponse<ArrayList<StoreMarkerVO>>?=response.body()
                            val datas = result?.getResult()

                            var geocoder = Geocoder(applicationContext)

                            for(data in datas!!) {
                                var address = geocoder.getFromLocationName(data.address, 10).get(0)
                                Log.d("ADDRESS" ,"onresponse 성공: "+ address.latitude)
                                var marker = MapPOIItem()
                                marker.mapPoint = MapPoint.mapPointWithGeoCoord(address.latitude, address.longitude)
                                marker.itemName = data.id.toString()
                                mMapView.addPOIItem(marker)
                            }

                            Log.d("StoreMarker" ,"onresponse 성공: "+ result?.toString() )
                            Log.d("StoreMarker", "data : "+ datas?.toString())

                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse<ArrayList<StoreMarkerVO>>>,
                        t: Throwable
                    ) {
                        Log.e("StoreMarker","onFailure : ${t.message} ");
                    }
                })
            } catch  (e: Exception) {
                // Exception handling
                Log.e(ContentValues.TAG, "Exception: ${e.message}", e)
            }
        }


    }//OnCreate()

    //서버 연결
    private fun initRetrofit() {
        retrofit = RetofitClient.getInstance()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }


    // 하드코딩으로 마커찍기
    fun setMakerHardCoding() {
        // 중심점 변경 - 덕성여대 차관
        mMapView.setMapCenterPoint(
            MapPoint.mapPointWithGeoCoord(
                37.65320737529757,
                127.01615398831316
            ), true
        )

        // 줌 레벨 변경 // 낮을수록 확대
        mMapView.setZoomLevel(1, true)

        // 줌 인
        mMapView.zoomIn(true)

        // 줌 아웃
        mMapView.zoomOut(true)

        val markerArr = ArrayList<MapPOIItem>()
        //setMarkerValues()

        // test 마커 찍기
        val MARKER_POINT1 =
            MapPoint.mapPointWithGeoCoord(37.64858331573457, 127.01432862276108) // 메가커피
        val MARKER_POINT2 =
            MapPoint.mapPointWithGeoCoord(37.65173675700233, 127.01432639564919) // 커피드림
        val MARKER_POINT3 =
            MapPoint.mapPointWithGeoCoord(37.65136960646743, 127.01432632511757) // 이요
        val MARKER_POINT4 =
            MapPoint.mapPointWithGeoCoord(37.65016688485748, 127.01355837181578) // 블랙다운커피
        val MARKER_POINT5 =
            MapPoint.mapPointWithGeoCoord(37.65006777280117, 127.01359234883397) // 히피스 베이글

        // 마커 아이콘 추가하는 함수
        val marker1 = MapPOIItem()
        val marker2 = MapPOIItem()
        val marker3 = MapPOIItem()
        val marker4 = MapPOIItem()
        val marker5 = MapPOIItem()

        // 클릭 했을 때 나오는 호출 값
        marker1.itemName =
            "메가커피"
        marker2.itemName =
            "커피드림"
        marker3.itemName =
            "eeeyo"
        marker4.itemName =
            "블랙다운커피"
        marker5.itemName =
            "히피스 베이글"

        // 왜 있는지 잘 모르겠음
        marker1.tag = 0
        marker2.tag = 0
        marker3.tag = 0
        marker4.tag = 0
        marker5.tag = 0

        // 좌표를 입력받아 현 위치로 출력
        marker1.mapPoint = MARKER_POINT1
        marker2.mapPoint = MARKER_POINT2
        marker3.mapPoint = MARKER_POINT3
        marker4.mapPoint = MARKER_POINT4
        marker5.mapPoint = MARKER_POINT5

        //  (클릭 전)기본으로 제공하는 BluePin 마커 모양의 색.
        marker1.markerType = MapPOIItem.MarkerType.BluePin
        marker2.markerType = MapPOIItem.MarkerType.BluePin
        marker3.markerType = MapPOIItem.MarkerType.BluePin
        marker4.markerType = MapPOIItem.MarkerType.BluePin
        marker5.markerType = MapPOIItem.MarkerType.BluePin


        // (클릭 후) 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        marker1.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        marker2.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        marker3.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        marker4.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        marker5.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);

        // 지도화면 위에 추가되는 아이콘을 추가하기 위한 호출(말풍선 모양)
        mMapView.addPOIItem(marker1);
        mMapView.addPOIItem(marker2);
        mMapView.addPOIItem(marker3);
        mMapView.addPOIItem(marker4);
        mMapView.addPOIItem(marker5);

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

    fun setValues() {
        // test data 삽입
        storeList.add(Store(0,"메가MGC커피 4.19사거리점", "휴게음식점", "서울 강북구 삼양로 510 1층 메가커피", "02-900-1288"))
        storeList.add(Store(1,"커피드림", "휴게음식점", "서울특별시 도봉구 삼양로144길 25", "01022360284"))
        storeList.add(Store(2,"eeeyo", "휴게음식점", "서울 도봉구 삼양로142길 33 일층", "0507-1323-2307"))
        storeList.add(Store(3,"블랙다운커피", "휴게음식점", "서울 강북구 삼양로 528-1 1층", "02-6338-0606"))
        storeList.add(Store(4,"히피스 베이글", "휴게음식점", "서울 강북구 삼양로 528", "02-906-6778"))

        //storeAdapter = StoreAdapter(this, android.R.layout.simple_list_item_1, storeList)
        //binding.searchView.adapter = storeAdapter
    }

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
        grantResults: IntArray
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
    }

    // 위치추적 중지
    private fun stopTracking() {
        mMapView.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOff
    }

    override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {
        val intent = Intent(this, StoreDetailActivity::class.java)
        intent.putExtra("store_id", p1?.itemName)
        startActivity(intent)

    }

    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {
        TODO("Not yet implemented")
    }

    override fun onCalloutBalloonOfPOIItemTouched(
        p0: MapView?,
        p1: MapPOIItem?,
        p2: MapPOIItem.CalloutBalloonButtonType?
    ) {
        TODO("Not yet implemented")
    }

    override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {
        TODO("Not yet implemented")
    }

}


private fun MapView.setOpenAPIKeyAuthenticationResultListener(togoActivity: TogoActivity) {

}

private fun MapView.setPOIItemEventListener(togoActivity: TogoActivity) {

}

private fun MapView.setMapViewEventListener(togoActivity: TogoActivity) {

}

private fun MapView.setCurrentLocationEventListener(togoActivity: TogoActivity) {

}

private fun ListView.contains(view: String?): Boolean {
    return true
}