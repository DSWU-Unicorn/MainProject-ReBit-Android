package kr.ac.duksung.rebit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast

class CardNewsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_news)

        // 로그

        var getData = intent.getIntExtra("data", 1)

        val cardnewsImage = findViewById<ImageView>(R.id.cardnewsImageArea)
        // Toast.makeText(this, getData, Toast.LENGTH_LONG).show()

        if (getData == 1) {
            cardnewsImage.setImageResource(R.drawable.cardnews_0)
        }

        if (getData == 2) {
            cardnewsImage.setImageResource(R.drawable.cardnews_1)
        }
        if (getData == 3) {
            cardnewsImage.setImageResource(R.drawable.cardnews_2)
        }
        if (getData == 4) {
            cardnewsImage.setImageResource(R.drawable.cardnews_3)
        }

    }
}