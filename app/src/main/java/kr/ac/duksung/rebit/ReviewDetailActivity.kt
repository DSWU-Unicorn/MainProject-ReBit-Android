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
                "간편하게 배달이나 포장으로 음식을 먹고 나면 쓰레기가 쌓입니다. 쓰레기를 만드는 대신 가지고 있는 용기를 가져가 음식을 받아오는 ‘용기내’ 챌린지, 요즘 많이들 하죠."
            ),
            Review(
                R.drawable.review_1,
                "심청이",
                "date",
                "연희동에선 두부도 플라스틱 통 대신 용기에 담아올 수 있었습니다. 플라스틱 쓰레기가 많이 생기는 밀키트를 사는 대신 직접 가져간 용기에 내용물만 담아왔고, 쌀가게에선 가져간 용기에 필요한 만큼만 쌀을 담아왔습니다."
            ),
            Review(
                R.drawable.review_2,
                "심사임당",
                "date",
                "용기내 챌린지는 음식 배달 및 포장으로 발생하는 불필요한 쓰레기를 줄이자는 취지에서 시작된 운동이다. '용기내 챌린지'는 이름대로 용기가 필요하다 "
            ),
            Review(
                R.drawable.review_3,
                "광개토대왕",
                "date",
                "2019년, 그린피스는 소비자를 대상으로 ‘대형마트의 일회용 플라스틱 사용’에 관한 인식 조사를 진행했다. 대다수 소비자는 대형마트의 일회용 플라스틱 사용이 과도하다고 생각하고 있었다. 소비자 10명 중 7명은 ‘플라스틱 사용을 줄인 마트가 있다면 구매처를 변경해서라도 이용해 볼 용의가 있다'고 답하기도 했다."
            ),
            Review(
                R.drawable.review_4,
                "이순신",
                "date",
                "대형마트들은 플라스틱 등 일회용품 없이 장보고 싶어하는 소비자들을 위한 고민을, 소비자들은 환경을 위해 불필요한 일회성 포장재를 줄이고자 하는 마음을 담은 #용기내 캠페인은 개인이 소소하게 실천할 수 있는 환경 운동으로 이슈가 되고 있으며 많은 사람들의 참여를 이끌어냈다."
            ),
            Review(
                R.drawable.review_0,
                "강감찬",
                "date",
                "그린피스 서울사무소 공식 유튜브 채널에서 #용기내 캠페인 관련 영상이 업데이트되고 있다."
            )
        )
        val Adapter = ListViewReviewAdapter(this, ReviewLists)
        val listView = findViewById<ListView>(R.id.listView)

        listView.adapter = Adapter
    }
}