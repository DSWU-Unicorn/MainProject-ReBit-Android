package kr.ac.duksung.rebit

import android.os.AsyncTask
import android.util.Log
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest
import java.io.File

class UploadTask(private val file: File, private val bucketName: String, private val accessKey: String, private val secretKey: String) : AsyncTask<Void, Void, String>() {

    override fun doInBackground(vararg params: Void): String {
        val credentials = BasicAWSCredentials(accessKey, secretKey)
        val s3Client = AmazonS3Client(credentials)

        val putObjectRequest = PutObjectRequest(bucketName, file.name, file)
        val putObject = s3Client.putObject(putObjectRequest)
        return putObject.toString()
    }

    override fun onPostExecute(result: String) {
        Log.d("S3_CHECK: ", result)
    }
}
