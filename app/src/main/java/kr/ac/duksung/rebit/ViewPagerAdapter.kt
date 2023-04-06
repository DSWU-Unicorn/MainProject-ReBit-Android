package kr.ac.duksung.rebit

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

class ViewPagerAdapter(private val list: ArrayList<String>) : PagerAdapter() {

    //position에 해당하는 페이지 생성
    override fun instantiateItem(container: ViewGroup, position: Int) : Any {
        val inflater = LayoutInflater.from(container.context)
        val view =  inflater.inflate(R.layout.viewholder_recycle, container, false)
        val imageView = view.findViewById<ImageView>(R.id.imageView1)
        //Glide.with(view).load(list[position]).into(imageView)

        Glide.with(container.context).load(list[position]).apply(
            RequestOptions()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
        ).into(imageView)
        Log.d("glide", list[position])

        /*
        val url = Uri.parse(list[position])
        imageView.setImageURI(url)

         */

        container.addView(view)
        return view
    }

    //position에 위치한 페이지 제거
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }

    //사용가능한 뷰 개수 리턴
    override fun getCount(): Int {
        return list.size
    }

    //페이지뷰가 특정 키 객체(key object)와 연관 되는지 여부
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return (view==`object`)
    }

}