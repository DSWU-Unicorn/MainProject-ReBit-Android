package kr.ac.duksung.rebit

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.unity3d.player.UnityPlayerActivity

class MainActivity : AppCompatActivity() {
    // 뒤로 가기
    private var isDouble = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
}