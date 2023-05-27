package kr.ac.duksung.rebit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kr.ac.duksung.rebit.network.dto.GetReviewCommentsVO
import kr.ac.duksung.rebit.R.drawable.sit_dagom_icon
import kr.ac.duksung.rebit.databinding.ReviewDetailItemBinding
import kr.ac.duksung.rebit.databinding.ReviewDetailItemBinding.*

class ReviewAdapter : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {
    private var reviewList: List<GetReviewCommentsVO> = emptyList()

    fun setReviews(reviews: List<GetReviewCommentsVO>) {
        this.reviewList = reviews
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding =
            ReviewDetailItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]
        holder.bind(review)
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }

    inner class ReviewViewHolder(private val binding: ReviewDetailItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(review: GetReviewCommentsVO) {
            val photoUrl = review.photo
            if (!photoUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(photoUrl)
                    .error(sit_dagom_icon)
                    .into(binding.imageView)
            } else {
                binding.imageView.setImageResource(sit_dagom_icon)
            }

            binding.userNameTextArea.text = review.user.toString()
            binding.starAvgRb.rating = review.star.toFloat()
            binding.reviewDateTv.text = review.date
            binding.reviewTv.text = review.comment
        }
    }

//    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val image: ImageView = itemView.findViewById(R.id.imageView)
//        private val name: TextView = itemView.findViewById(R.id.userNameTextArea)
//        private val date: TextView = itemView.findViewById(R.id.reviewDateTv)
//        private val reviewText: TextView = itemView.findViewById(R.id.reviewTv)
//        private val star: RatingBar = itemView.findViewById(R.id.starAvgRb)
//
//        fun bind(review: GetReviewCommentsVO) {
//            // 리뷰 데이터를 뷰에 바인딩하는 코드 작성
//            // 이미지, 이름, 날짜, 별점, 리뷰 내용 등을 설정
//
//            // 이미지
////            val photoUrl = review.photo
////            if (photoUrl != null) {
////                Glide.with(itemView.context).load(photoUrl)
////                    .error(sit_dagom_icon).into(image)
////            } else {
////                image.setImageResource(sit_dagom_icon)
////            }
//
//            image.setImageResource(sit_dagom_icon)
//
//            // 이름
//            name.text = review.user.toString()
//
//            // 별점
//            star.rating = review.star.toFloat()
//
//            // 날짜
////            date.text = DateUtil.formatDateString(review.date)
//
//            // Date
//            val dateString = review.date
//            if (dateString != null) {
//                val formattedDate = formatDateString(dateString)
//                date.text = formattedDate
//            } else {
//                date.text = ""
//
//            }
//            // 리뷰 내용
//            reviewText.text = review.comment
//        }
//
//        private fun formatDateString(dateString: String): String {
//            return try {
//                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//                val outputFormat = SimpleDateFormat("yyyy년 MM월 dd일 EEEE", Locale.getDefault())
//
//                val date = inputFormat.parse(dateString)
//                outputFormat.format(date)
//            } catch (e: Exception) {
//                e.printStackTrace()
//                ""
//            }
//
//        }
//    }
}
//
//object DateUtil {
//    fun formatDateString(dateString: String): String {
//        return try {
//            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//            val outputFormat = SimpleDateFormat("yyyy년 MM월 dd일 EEEE", Locale.getDefault())
//
//            val date = inputFormat.parse(dateString)
//            outputFormat.format(date)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            ""
//        }
//    }
//}
//
