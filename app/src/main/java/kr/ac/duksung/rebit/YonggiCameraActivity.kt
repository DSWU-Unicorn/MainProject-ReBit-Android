package kr.ac.duksung.rebit

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kr.ac.duksung.rebit.databinding.ActivityCameraBinding

class YonggiCameraActivity : AppCompatActivity(){
    lateinit var bitmap: Bitmap
    lateinit var imageView: ImageView

    private lateinit var binding: ActivityCameraBinding
    private lateinit var systemUiController: SystemUiController

    // 권한을 확인할때의 권한 확인을 위함
    val CAMERA = arrayOf(Manifest.permission.CAMERA)

    // 권한 요청을 위한 권한 자체를 정의
    val CAMERA_CODE = 98

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        //
        val data = intent.getStringExtra("store_id")
        val store_id = Integer.parseInt(data)
        Log.d("YONGGICAMERA_STORE_ID", store_id.toString())

        //객체 생성
        imageView = findViewById(R.id.imageView)
        // 촬영버튼
        val picBtn: Button = findViewById(R.id.pic_btn)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Initialize the SystemUiController with the current window
        systemUiController = SystemUiController(window)

        // Set the status bar color
        systemUiController.setStatusBarColor(ContextCompat.getColor(this, R.color.teal_200))

        // Set the navigation bar color
        systemUiController.setNavigationBarColor(ContextCompat.getColor(this, R.color.white))

        // Set the system bars color
        systemUiController.setSystemBarsColor(ContextCompat.getColor(this, R.color.purple_200))


        binding.picBtn.setOnClickListener() {
            CallCamera()
            binding.startButton.setOnClickListener {

                // Dialog만들기
                val mDialogView =
                    LayoutInflater.from(this).inflate(R.layout.after_yongginae_model_dialog, null)
                val mBuilder = AlertDialog.Builder(this)
                    .setView(mDialogView)

                val mAlertDialog = mBuilder.show()


                val togoButton = mDialogView.findViewById<Button>(R.id.togoButton)
                togoButton.setOnClickListener {

                    Toast.makeText(this, "용기를 내서 포장하러 가는 당신! 멋져요⭐️️", Toast.LENGTH_SHORT).show()
                    mAlertDialog.dismiss()

                    // 포장하러가기 누르면.. 용기내 main 화면으로 이동해 지도에 포장하러가기 상태바 띄움.
                    //
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("status", "true")
                    intent.putExtra("store_id", store_id.toString())
                    startActivity(intent)
                }

                val noButton = mDialogView.findViewById<Button>(R.id.AgainButton)
                noButton.setOnClickListener {
                    CallCamera()
                }
            } //setOnClickListener
        }
    }//onCreate

    fun checkPermission(permissions: Array<out String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(this, permissions, CAMERA_CODE)
                    return false;
                }
            }
        }

        return true;
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_CODE -> {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "카메라 권한을 승인해 주세요.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    fun CallCamera() {
        if (checkPermission(CAMERA)) {
            val itt = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(itt, CAMERA_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_CODE -> {
                    if (data?.extras?.get("data") != null) {
                        val img = data?.extras?.get("data") as Bitmap
                        binding.imageView.setImageBitmap(img)
                    }
                }
            }
        }
    }
}