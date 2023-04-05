package kr.ac.duksung.rebit

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kr.ac.duksung.rebit.databinding.ActivityCameraBinding


class CameraActivity : AppCompatActivity() {
    lateinit var bitmap: Bitmap
    lateinit var imageView: ImageView

    private lateinit var binding: ActivityCameraBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        //객체 생성
        imageView = findViewById(R.id.imageView)
        val picBtn: Button = findViewById(R.id.pic_btn)

        //버튼 이벤트
        picBtn.setOnClickListener {
            //사진 촬영
            val intent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            activityResult.launch(intent)

            binding = ActivityCameraBinding.inflate(layoutInflater)
            setContentView(binding.root)

            binding.startButton.setOnClickListener {

                // Dialog만들기
                val mDialogView =
                    LayoutInflater.from(this).inflate(R.layout.after_model_dialog, null)
                val mBuilder = AlertDialog.Builder(this)
                    .setView(mDialogView)

                val mAlertDialog = mBuilder.show()


                val okButton = mDialogView.findViewById<Button>(R.id.successButton)
                okButton.setOnClickListener {

                    Toast.makeText(this, "포인트 획득했습니다!", Toast.LENGTH_SHORT).show()
                    mAlertDialog.dismiss()
                }

                val noButton = mDialogView.findViewById<Button>(R.id.AgainButton)
                noButton.setOnClickListener {
                    val intent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    activityResult.launch(intent)

                }
            }
        }


    }//onCreate


    //결과 가져오기
    private val activityResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {

        if (it.resultCode == RESULT_OK && it.data != null) {
            //값 담기
            val extras = it.data!!.extras

            //bitmap으로 타입 변경
            bitmap = extras?.get("data") as Bitmap

            //화면에 보여주기
            imageView.setImageBitmap(bitmap)


//            binding=ActivityCameraBinding.inflate(layoutInflater)
//            setContentView(binding.root)
//
//            binding.startButton.setOnClickListener {
//
//                // Dialog만들기
//                val mDialogView = LayoutInflater.from(this).inflate(R.layout.after_model_dialog, null)
//                val mBuilder = AlertDialog.Builder(this)
//                    .setView(mDialogView)
//
//                val mAlertDialog = mBuilder.show()
//
//
//                val okButton = mDialogView.findViewById<Button>(R.id.successButton)
//                okButton.setOnClickListener {
//
//                    Toast.makeText(this, "포인트 획득했습니다!", Toast.LENGTH_SHORT).show()
//                    mAlertDialog.dismiss()
//                }
//
//                val noButton = mDialogView.findViewById<Button>(R.id.AgainButton)
//                noButton.setOnClickListener {
//                    mAlertDialog.dismiss()
//                }
//            }

        }
    }
}

