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
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest
import com.shashank.sony.fancytoastlib.FancyToast
import kr.ac.duksung.rebit.databinding.ActivityCameraBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class YonggiCameraActivity : AppCompatActivity(){
    lateinit var bitmap: Bitmap
    lateinit var imageView: ImageView

    private lateinit var binding: ActivityCameraBinding
    private lateinit var systemUiController: SystemUiController
    private var firstPhotoFileName: String? = null
    private var secondPhotoFileName: String? = null
    private var isSecondPhotoTaken = false


    // toast message
    private var toast: Toast?  = null

    var flag = 0

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


        // S3 설정
        fun uploadFileToS3(file: File, bucketName: String, accessKey: String, secretKey: String) {
            val credentials = BasicAWSCredentials(accessKey, secretKey)
            val s3Client = AmazonS3Client(credentials)

            val putObjectRequest = PutObjectRequest(bucketName, file.name, file)
            val putObject = s3Client.putObject(putObjectRequest)
            Log.d("S3_CHECK: ", putObject.toString())
        }


        // 부적합한 용기일때
        binding.noBtn.setOnClickListener {
            // Dialog만들기
            val mDialogView =
                LayoutInflater.from(this).inflate(R.layout.after_yongginae_model_unsuitable_dialog, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)

            val mAlertDialog = mBuilder.show()


            val noButton = mDialogView.findViewById<Button>(R.id.AgainButton)
            noButton.setOnClickListener {
                CallCamera()
                mAlertDialog.dismiss()
            }
        } //setOnClickListener


        binding.picBtn.setOnClickListener() {
            CallCamera()
            if(flag.equals(0)){
                makeToast( "정확한 측정을 위해 동전과 함께 용기의 윗부분을 촬영해주세요!")
            }
            flag+=1

            binding.startButton.setOnClickListener {

                // Dialog만들기
                // 해당 다회용기는 사용이 적합하다.
                val mDialogView =
                    LayoutInflater.from(this).inflate(R.layout.after_yongginae_model_dialog, null)
                val mBuilder = AlertDialog.Builder(this)
                    .setView(mDialogView)

                val mAlertDialog = mBuilder.show()

                // 용기내러 가기! 버튼 클릭시
                val togoButton = mDialogView.findViewById<Button>(R.id.togoButton)
                togoButton.setOnClickListener {

                    //Toast.makeText(this, "용기를 내서 포장하러 가는 당신! 멋져요⭐️️", Toast.LENGTH_SHORT).show()
                    mAlertDialog.dismiss()

                    // 포장하러가기 누르면.. 용기내 지도 화면으로 이동해 지도에 포장하러가기 상태바 띄움.
                    val intent = Intent(this, TogoActivity::class.java)
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

                        if (!isSecondPhotoTaken) {
                            firstPhotoFileName = generatePhotoFileName()
                            val firstImageFile = convertBitmapToFile(img, firstPhotoFileName.toString())
                            Log.d("FIRST_IMAGENAME:", firstImageFile.name)

                            val uploadTask = UploadTask(firstImageFile,
                                BuildConfig.bucketName,
                                BuildConfig.accessKey,
                                BuildConfig.secretKey)
                            uploadTask.execute()
                            isSecondPhotoTaken = true
                        } else {
                            secondPhotoFileName = generatePhotoFileName()
                            val secondImageFile = convertBitmapToFile(img, secondPhotoFileName.toString())
                            Log.d("SECOND_IMAGENAME:", secondImageFile.name)

                            val uploadTask = UploadTask(secondImageFile,
                                BuildConfig.bucketName,
                                BuildConfig.accessKey,
                                BuildConfig.secretKey)
                            uploadTask.execute()

                            // Reset for the next round of photos
                            isSecondPhotoTaken = false
                            firstPhotoFileName = null
                            secondPhotoFileName = null
                        }

                        if(flag.equals(1)){
                            makeToast( "정확한 측정을 위해 한번 더 동전과 함께 용기의 측면을 촬영해주세요!️️")
                        }
                        flag+=1
                    }
                }
            }
        }
    }

    private fun makeToast(message: String){
        toast?.cancel()
        toast = Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
        toast?.show()
    }

    private fun convertBitmapToFile(bitmap: Bitmap, filename: String): File {
        val file = File(this.cacheDir, filename)
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }


    private fun generatePhotoFileName(): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "IMG_$timeStamp.jpg"
    }
}