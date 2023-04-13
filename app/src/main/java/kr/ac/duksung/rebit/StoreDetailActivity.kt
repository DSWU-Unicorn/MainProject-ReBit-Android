package kr.ac.duksung.rebit

import android.content.Intent
import kr.ac.duksung.rebit.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil.setContentView
import androidx.viewbinding.ViewBinding
import kotlinx.android.synthetic.main.activity_store_detail.*
import kotlinx.android.synthetic.main.multi_image_item.view.*
import kr.ac.duksung.rebit.databinding.ActivityStoreDetailBinding
import kr.ac.duksung.rebit.databinding.ActivityTogoBinding
import kr.ac.duksung.rebit.datas.Store
import java.text.ParseException
import java.util.regex.Pattern

class StoreDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStoreDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_detail)

        binding = ActivityStoreDetailBinding.inflate(layoutInflater)
        val storeImageArea = findViewById<ImageView>(R.id.storeImageArea)

        setValues()
        setupEvents()

        val imgId = intArrayOf(
            R.drawable.megacoffee1, R.drawable.coffeedream2,
            R.drawable.eeeyo3, R.drawable.blackdown4,
            R.drawable.bagle5
        )
        val store = intent.getSerializableExtra("storeInfo") as Store

        val rand = "${store.id}"

        storeImageArea.setImageResource(imgId[rand.toInt()])

        val pic_btn = findViewById<Button>(R.id.pic_btn)
        pic_btn.setOnClickListener {
            Toast.makeText(this, "내 용기가 맞을까? 확인하러 가기", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }
        // review view
        val goto_review_btn = findViewById<Button>(R.id.goto_review_btn)
        goto_review_btn.setOnClickListener {
            Toast.makeText(this, "생생한 후기가 궁금하나요? 리뷰 보러 가기", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ReviewActivity::class.java)
            startActivity(intent)
        }
        // review create
        val todo_btn = findViewById<Button>(R.id.todo_btn)
        todo_btn.setOnClickListener {
            Toast.makeText(this, "이미 용기냈다면! 어땠는지 후기 작성하러 가기", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, CreateReview::class.java)
            startActivity(intent)
        }


    }

    fun setupEvents() {
    }

    fun setValues() {

        // storeInfo를 serializable로 받는다
        // 그냥 받은 채로 변수에 넣으면 오류가 나는데 이 때 Casting을 해줘야 한다
        val store = intent.getSerializableExtra("storeInfo") as Store

        // activity_store_detail.xml에 설정했던 view에 따라 매핑
        storeNameTextArea.text = "${store.storeName}"
        storeKindTextArea.text = "${store.category1}"
        addressTxt.text = store.category2
        telText.text = (store.tel).convertNumberToPhoneNumber()
    }

    // 전화번호 하이픈 추가
    fun String.convertNumberToPhoneNumber(): String {     // 코틀린의 확장함수 사용
        return try {
            val regexString = "(\\d{3})(\\d{3,4})(\\d{4})"
            return if (!Pattern.matches(regexString, this)) this else Regex(regexString).replace(
                this,
                "$1-$2-$3"
            )
        } catch (e: ParseException) {
            e.printStackTrace()
            this
        }
    }
}