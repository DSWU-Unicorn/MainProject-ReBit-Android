package kr.ac.duksung.rebit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.databinding.DataBindingUtil.setContentView
import kr.ac.duksung.rebit.databinding.ActivityReviewBinding

class ReviewActivity : AppCompatActivity() {
    private lateinit var binding : ActivityReviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        val reviewList = mutableListOf<String>()
        reviewList.add("1.별을 별 강아지, 사랑과 소녀들의 슬퍼하는 까닭입니다. 다 이름과 내 이름과, 차 이웃 불러 듯합니다. 하나에 나는 둘 부끄러운 헤는 슬퍼하는 다하지 이런 별을 까닭입니다. 라이너 위에 이름과, 있습니다. 이런 이국 무엇인지 나는 계십니다. 무엇인지 오는 새워 있습니다. 아스라히 시인의 때 어머님, 이름과, 없이 까닭입니다. 딴은 불러 아스라히 이 이름자를 이네들은 사람들의 별 봅니다. 별이 강아지, 이름을 위에 그러나 봅니다.")
        reviewList.add("2.별을 별 강아지, 사랑과 소녀들의 슬퍼하는 까닭입니다. 다 이름과 내 이름과, 차 이웃 불러 듯합니다. 하나에 나는 둘 부끄러운 헤는 슬퍼하는 다하지 이런 별을 까닭입니다. 라이너 위에 이름과, 있습니다. 이런 이국 무엇인지 나는 계십니다. 무엇인지 오는 새워 있습니다. 아스라히 시인의 때 어머님, 이름과, 없이 까닭입니다. 딴은 불러 아스라히 이 이름자를 이네들은 사람들의 별 봅니다. 별이 강아지, 이름을 위에 그러나 봅니다.")
        reviewList.add("3.별을 별 강아지, 사랑과 소녀들의 슬퍼하는 까닭입니다. 다 이름과 내 이름과, 차 이웃 불러 듯합니다. 하나에 나는 둘 부끄러운 헤는 슬퍼하는 다하지 이런 별을 까닭입니다. 라이너 위에 이름과, 있습니다. 이런 이국 무엇인지 나는 계십니다. 무엇인지 오는 새워 있습니다. 아스라히 시인의 때 어머님, 이름과, 없이 까닭입니다. 딴은 불러 아스라히 이 이름자를 이네들은 사람들의 별 봅니다. 별이 강아지, 이름을 위에 그러나 봅니다.")
        reviewList.add("4.별을 별 강아지, 사랑과 소녀들의 슬퍼하는 까닭입니다. 다 이름과 내 이름과, 차 이웃 불러 듯합니다. 하나에 나는 둘 부끄러운 헤는 슬퍼하는 다하지 이런 별을 까닭입니다. 라이너 위에 이름과, 있습니다. 이런 이국 무엇인지 나는 계십니다. 무엇인지 오는 새워 있습니다. 아스라히 시인의 때 어머님, 이름과, 없이 까닭입니다. 딴은 불러 아스라히 이 이름자를 이네들은 사람들의 별 봅니다. 별이 강아지, 이름을 위에 그러나 봅니다.")
        reviewList.add("5.별을 별 강아지, 사랑과 소녀들의 슬퍼하는 까닭입니다. 다 이름과 내 이름과, 차 이웃 불러 듯합니다. 하나에 나는 둘 부끄러운 헤는 슬퍼하는 다하지 이런 별을 까닭입니다. 라이너 위에 이름과, 있습니다. 이런 이국 무엇인지 나는 계십니다. 무엇인지 오는 새워 있습니다. 아스라히 시인의 때 어머님, 이름과, 없이 까닭입니다. 딴은 불러 아스라히 이 이름자를 이네들은 사람들의 별 봅니다. 별이 강아지, 이름을 위에 그러나 봅니다.")
        reviewList.add("6.별을 별 강아지, 사랑과 소녀들의 슬퍼하는 까닭입니다. 다 이름과 내 이름과, 차 이웃 불러 듯합니다. 하나에 나는 둘 부끄러운 헤는 슬퍼하는 다하지 이런 별을 까닭입니다. 라이너 위에 이름과, 있습니다. 이런 이국 무엇인지 나는 계십니다. 무엇인지 오는 새워 있습니다. 아스라히 시인의 때 어머님, 이름과, 없이 까닭입니다. 딴은 불러 아스라히 이 이름자를 이네들은 사람들의 별 봅니다. 별이 강아지, 이름을 위에 그러나 봅니다.")

        Log.e("ReviewActivity", reviewList.random())

        binding = setContentView(this, R.layout.activity_review)
        binding.showAllReviewBtn.setOnClickListener {
            val intent = Intent(this, ReviewDetailActivity::class.java)
            startActivity(intent)
        }
        binding.reviewTextArea.setText(reviewList.random())
    }
}