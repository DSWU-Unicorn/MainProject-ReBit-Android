package kr.ac.duksung.rebit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class ListViewReviewAdapter(val context: Context, val ReviewList: ArrayList<Review>) :
    BaseAdapter() {

    //    private var mBinding: ListviewReviewItemBinding? = null
//    private val binding get() = mBinding!!
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(context).inflate(R.layout.listview_review_item, null)

        val image = view.findViewById<ImageView>(R.id.ReviewImageArea)
        val name = view.findViewById<TextView>(R.id.userNameTextArea)
        val date = view.findViewById<TextView>(R.id.reviewDateTextArea)
        val review_text = view.findViewById<TextView>(R.id.reviewTextArea)

        val review = ReviewList[position]

        image.setImageResource(review.image)
        name.text = review.name
        date.text = review.date
        review_text.text = review.review_text

        return view
//        mBinding = ListviewReviewItemBinding.inflate(LayoutInflater.from(context))
//        val img = binding.Img
//        val name = binding.Name
//        var convertView = convertView
//
//        if (convertView == null) {
//            convertView = LayoutInflater.from(parent?.context)
//                .inflate(R.layout.listview_review_item, parent, false)
//
//        }
//        val reviewImage = convertView?.findViewById<ImageView>(R.id.ReviewImageArea)
//        // reviewImage?.ImageView = List[position]
//        Review image = Review.get(position);
//
//        val userName = convertView?.findViewById<TextView>(R.id.userNameTextArea)
//        val reviewDateTextArea = convertView?.findViewById<TextView>(R.id.reviewDateTextArea)
//        val username = convertView?.findViewById<TextView>(R.id.userNameTextArea)
//        val username = convertView?.findViewById<TextView>(R.id.userNameTextArea)

    }

    override fun getCount(): Int {
        return ReviewList.size
    }

    override fun getItem(position: Int): Any {
        return ReviewList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


}