package kr.ac.duksung.rebit

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Log.d
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.MenuItemCompat.getActionView
import androidx.core.view.contains
import kr.ac.duksung.rebit.databinding.ActivityMainBinding
import kr.ac.duksung.rebit.databinding.ActivityTogoBinding
import net.daum.mf.map.api.MapView
import java.security.AccessControlContext
import java.security.AccessController.getContext
import java.util.logging.Logger

class TogoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTogoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTogoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = arrayOf(
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

        val userAdapter: ArrayAdapter<String> = ArrayAdapter(
            this, android.R.layout.simple_list_item_1,
            user
        )
        binding.userList.adapter = userAdapter;

        binding.searchView.isSubmitButtonEnabled=true

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.searchView.clearFocus()
                if (user.contains(query)) {
                    userAdapter.filter.filter(query)
                }
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                userAdapter.filter.filter(newText)
                return false
            }
        })

        binding.searchView.setOnClickListener {
            // Dialog만들기
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.store_info_dialog, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("가게 정보")
            mBuilder.show()

            val pic_btn = mDialogView.findViewById<Button>(R.id.pic_btn)
            pic_btn.setOnClickListener{
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
//        val map = MapView(this)
//        val mapView = findViewById<ViewGroup>(R.id.mapView)
//
//        mapView.addView(map)
        val mMapView = MapView(this)
        val mMapViewContainer = findViewById(R.id.map_mv_mapcontainer) as ViewGroup

        mMapViewContainer.addView(mMapView)

// 리스너 등록
        mMapView.setMapViewEventListener(this) // this에 MapView.MapViewEventListener 구현.
        mMapView.setPOIItemEventListener(this)
        mMapView.setOpenAPIKeyAuthenticationResultListener(this)

    }


}

private fun MapView.setOpenAPIKeyAuthenticationResultListener(togoActivity: TogoActivity) {

}

private fun MapView.setPOIItemEventListener(togoActivity: TogoActivity) {

}

private fun MapView.setMapViewEventListener(togoActivity: TogoActivity) {

}

