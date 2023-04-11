package kr.ac.duksung.rebit

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kr.ac.duksung.rebit.databinding.ActivityTogoBinding
import kr.ac.duksung.rebit.datas.Store
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView


class TogoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTogoBinding

    // 정적인 arrayOf 대신 ArrayList 사용(4/8 토 14:30~15:44)
    val storeList = ArrayList<Store>();
    //lateinit var storeAdapter: StoreAdapter

    private val user = arrayOf(
        "Abhay",
        "Joseph",
        "Maria",
        "Avni",
        "Apoorva",
        "Chris",
        "David",
        "Kaira",
        "Dwayne",
        "Christopher",
        "Jim",
        "Russel",
        "Donald",
        "Brack",
        "Vladimir"
    )

//    private val GPS_ENABLE_REQUEST_CODE = 2001
//    private val PERMISSIONS_REQUEST_CODE = 100
//    var REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
var mMapView: MapView? = null
    var mMapViewContainer: ViewGroup? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTogoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 리스트 목록 클릭시
        setupEvents()
        // 목록 값 지정
        setValues()
//        val userAdapter: ArrayAdapter<String> = ArrayAdapter(
//            this, android.R.layout.simple_list_item_1,
//            user
//        )
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
            }
            val goto_review_btn = mDialogView.findViewById<Button>(R.id.goto_review_btn)

            goto_review_btn.setOnClickListener {
                Toast.makeText(this, "더 많은 리뷰 보러 가기", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ReviewActivity::class.java)
                startActivity(intent)
            }
            // review create
            val todo_btn = mDialogView.findViewById<Button>(R.id.todo_btn)

            todo_btn.setOnClickListener {
                Toast.makeText(this, "이미 용기냈다면! 어땠는지 후기 작성하러 가기", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, CreateReview::class.java)
                startActivity(intent)
            }
        }
        //지도
        val mMapView = MapView(this)
        val mMapViewContainer = findViewById(R.id.map_mv_mapcontainer) as ViewGroup
        mMapViewContainer.addView(mMapView)

        // 중심점 변경 - 덕성여대 차관
        mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(37.65320737529757, 127.01615398831316), true)

        // 줌 레벨 변경
        mMapView.setZoomLevel(1, true)

        // 줌 인
        mMapView.zoomIn(true)

        // 줌 아웃
        mMapView.zoomOut(true)

        //마커 찍기 (덕성여대)
        val MARKER_POINT1 = MapPoint.mapPointWithGeoCoord(37.65320737529757, 127.01615398831316)

        // 마커 아이콘 추가하는 함수
        val marker1 = MapPOIItem()

        // 클릭 했을 때 나오는 호출 값
        marker1.itemName = "여기 있음!"

        // 왜 있는지 잘 모르겠음
        marker1.tag = 0

        // 좌표를 입력받아 현 위치로 출력
        marker1.mapPoint = MARKER_POINT1

        //  (클릭 전)기본으로 제공하는 BluePin 마커 모양의 색.
        marker1.markerType = MapPOIItem.MarkerType.BluePin
        // (클릭 후) 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        marker1.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);

        // 지도화면 위에 추가되는 아이콘을 추가하기 위한 호출(말풍선 모양)
        mMapView.addPOIItem(marker1);
        // 리스너 등록
        mMapView.setMapViewEventListener(this) // this에 MapView.MapViewEventListener 구현.
        mMapView.setPOIItemEventListener(this)
        mMapView.setOpenAPIKeyAuthenticationResultListener(this)


        }//OnCreate()

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
        storeList.add(Store("Abhay", "휴게음식점", "경기 화성시 10용사1길 49 1층 아워시즌", "01022360284"))
        storeList.add(Store("Apoorva", "휴게음식점", "경기 화성시 10용사1길 49 1층 아워시즌", "01022360284"))
        storeList.add(Store("Avni", "휴게음식점", "경기 화성시 10용사1길 49 1층 아워시즌", "01022360284"))
        storeList.add(Store("David", "휴게음식점", "경기 화성시 10용사1길 49 1층 아워시즌", "01022360284"))
        storeList.add(Store("Chris", "휴게음식점", "경기 화성시 10용사1길 49 1층 아워시즌", "01022360284"))

        //storeAdapter = StoreAdapter(this, android.R.layout.simple_list_item_1, storeList)
        //binding.searchView.adapter = storeAdapter
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
