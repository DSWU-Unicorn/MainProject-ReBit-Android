package kr.ac.duksung.rebit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class GuideActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)

        // 포인트 획득 버튼 클릭시
        val okButton = findViewById<Button>(R.id.successButton)
        okButton.setOnClickListener {

            Toast.makeText(this, "포인트를 획득했습니다!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, RecycleActivity::class.java)
            startActivity(intent)
        }
    }
}