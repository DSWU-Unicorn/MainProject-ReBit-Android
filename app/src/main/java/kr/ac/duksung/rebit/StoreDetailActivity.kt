package kr.ac.duksung.rebit

import kr.ac.duksung.rebit.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil.setContentView
import kotlinx.android.synthetic.main.activity_store_detail.*
import kr.ac.duksung.rebit.datas.Store

class StoreDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_detail)

        setValues()
        setupEvents()

    }
    fun setupEvents() {
    }

    fun setValues() {

        // storeInfo를 serializable로 받는다
        // 그냥 받은 채로 변수에 넣으면 오류가 나는데 이 때 Casting을 해줘야 한다
        val store = intent.getSerializableExtra("storeInfo") as Store

        // activity_store_detail.xml에 설정했던 view에 따라 매핑
        storeNameTextArea.text = "${store.storeName} / ${store.category1}"
        addressTxt.text = store.category2
        telText.text = store.tel
    }
}