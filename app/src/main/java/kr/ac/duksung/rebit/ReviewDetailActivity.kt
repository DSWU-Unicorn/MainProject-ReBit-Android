package kr.ac.duksung.rebit

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class ReviewDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_detail)


        var ReviewLists = arrayListOf<Review>(
            Review(
                R.drawable.review_0,
                "홍길동",
                "date",
                "주는 이 온갖 힘있다. 이상이 인생을 인생을 이상은 투명하되 끝에 듣는다. 따뜻한 그들은 우리 물방아 풀"
            ),
            Review(
                R.drawable.review_1,
                "심청이",
                "date",
                "주는 이 온갖 힘있다. 이상이 인생을 인생을 이상은 투명하되 끝에 듣는다. 따뜻한 그들은 우리 물방아 풀"
            ),
            Review(
                R.drawable.review_2,
                "심사임당",
                "date",
                "주는 이 온갖 힘있다. 이상이 인생을 인생을 이상은 투명하되 끝에 듣는다. 따뜻한 그들은 우리 물방아 풀"
            ),
            Review(
                R.drawable.review_3,
                "광개토대왕",
                "date",
                "주는 이 온갖 힘있다. 이상이 인생을 인생을 이상은 투명하되 끝에 듣는다. 따뜻한 그들은 우리 물방아 풀"
            ),
            Review(
                R.drawable.review_4,
                "이순신",
                "date",
                "주는 이 온갖 힘있다. 이상이 인생을 인생을 이상은 투명하되 끝에 듣는다. 따뜻한 그들은 우리 물방아 풀"
            ),
            Review(
                R.drawable.review_0,
                "강감찬",
                "date",
                "주는 이 온갖 힘있다. 이상이 인생을 인생을 이상은 투명하되 끝에 듣는다. 따뜻한 그들은 우리 물방아 풀"
            )
        )
        val Adapter = ListViewReviewAdapter(this, ReviewLists)
        val listView = findViewById<ListView>(R.id.listView)

        listView.adapter=Adapter
    }
}