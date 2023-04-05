package kr.ac.duksung.rebit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CreateReview() : AppCompatActivity() {
    private val TAG = "MultiImageActivity"
    private val uriList = ArrayList<Uri>() // ArrayList object to store URIs of selected images

     lateinit var recyclerView: RecyclerView // RecyclerView to display selected images
     lateinit var adapter: MultiImageAdapter // Adapter to apply to the RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_review)

        // Button to open the photo album
        val btnGetImage = findViewById<Button>(R.id.getImage)
        btnGetImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = MediaStore.Images.Media.CONTENT_TYPE
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            startActivityForResult(intent, 2222)
        }

        recyclerView = findViewById(R.id.recyclerView)

        val close_btn = findViewById<Button>(R.id.close_btn)

        close_btn.setOnClickListener {
            finish()
        }
    }

    // Method executed after returning from the photo album
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        data?.let {
            // No image selected
            if (it.data == null) {
                Toast.makeText(applicationContext, "No image selected.", Toast.LENGTH_LONG).show()
            } else {
                // Single image selected
                if (it.clipData == null) {
                    Log.e("single choice: ", it.data.toString())
                    val imageUri = it.data!!
                    uriList.add(imageUri)

                    adapter = MultiImageAdapter(uriList, applicationContext)
                    recyclerView.adapter = adapter
                    recyclerView.layoutManager =
                        LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)
                }
                // Multiple images selected
                else {
                    val clipData = it.clipData!!
                    Log.e("clipData", clipData.itemCount.toString())

                    if (clipData.itemCount > 10) {
                        Toast.makeText(
                            applicationContext,
                            "You can select up to 10 photos.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Log.e(TAG, "multiple choice")

                        for (i in 0 until clipData.itemCount) {
                            val imageUri =
                                clipData.getItemAt(i).uri // Get the URIs of the selected images
                            try {
                                uriList.add(imageUri) // Add the URI to the list

                            } catch (e: Exception) {
                                Log.e(TAG, "File select error", e)
                            }
                        }

                        adapter = MultiImageAdapter(uriList, applicationContext)
                        recyclerView.adapter = adapter // Set the adapter to the RecyclerView
                        recyclerView.layoutManager = LinearLayoutManager(
                            this,
                            LinearLayoutManager.HORIZONTAL,
                            true
                        ) // Apply horizontal scrolling to the RecyclerView
                    }
                }
            }
        }

    }


}

