package tk.jaaproductions.steganography.steganography

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import android.app.Activity
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import tk.jaaproductions.steganography.steganography.Helpers.DecodeAsyncTask
import tk.jaaproductions.steganography.steganography.Helpers.EncodeAsyncTask
import tk.jaaproductions.steganography.steganography.Interfaces.OnDecodeTaskCompleted
import tk.jaaproductions.steganography.steganography.Interfaces.OnEncodeTaskCompleted
import java.io.File


class MainActivity : AppCompatActivity(),
    OnEncodeTaskCompleted, OnDecodeTaskCompleted {
    private val ENCODE_SELECTED_PIC = 1
    private val DECODE_SELECTED_PIC = 2
    private val MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 3

    private var mProgressDialog : ProgressDialog? = null

    private var hasStorageWritePermission : Boolean = false

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_encode -> {
                message.setText(R.string.title_encode)
                decodedMessageTextView.text = getString(R.string.message_to_decode_hint)
                decodedImageView.setImageResource(R.drawable.ic_photo_black_24dp)
                encodeButton.visibility = View.VISIBLE
                encodedImageView.visibility = View.VISIBLE
                messageToEncodeEditText.visibility = View.VISIBLE
                decodedImageView.visibility = View.INVISIBLE
                decodedMessageTextView.visibility = View.INVISIBLE
                decodeButton.visibility = View.INVISIBLE
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_decode -> {
                message.setText(R.string.title_decode)
                messageToEncodeEditText.text.clear()
                encodedImageView.setImageResource(R.drawable.ic_photo_black_24dp)
                decodedImageView.visibility = View.VISIBLE
                decodedMessageTextView.visibility = View.VISIBLE
                decodeButton.visibility = View.VISIBLE
                encodeButton.visibility = View.INVISIBLE
                encodedImageView.visibility = View.INVISIBLE
                messageToEncodeEditText.visibility = View.INVISIBLE
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        mProgressDialog = ProgressDialog(this)
        checkAndAskForWritePermission()

        encodeButton.setOnClickListener {
            if (!hasStorageWritePermission)
                checkAndAskForWritePermission()
            else if (messageToEncodeEditText.text.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, ENCODE_SELECTED_PIC)
            }
            else {
                Toast.makeText(this, "Please fill out all fields!", Toast.LENGTH_LONG).show()
            }
        }

        decodeButton.setOnClickListener {
            if (!hasStorageWritePermission)
                checkAndAskForWritePermission()
            else {
                val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, DECODE_SELECTED_PIC)
            }
        }
    }

    private fun checkAndAskForWritePermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_WRITE_STORAGE)
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_WRITE_STORAGE)

                // MY_PERMISSIONS_REQUEST_WRITE_STORAGE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            hasStorageWritePermission = true
        }
    }

    private fun encode(picUri : Uri) {
        val projection = arrayOf(MediaStore.Images.Media.DATA)

        val cursor = this.contentResolver.query(picUri, projection, null, null, null)
        cursor!!.moveToFirst()

        val columnIndex = cursor.getColumnIndex(projection[0])
        val filepath = cursor.getString(columnIndex)
        cursor.close()

        mProgressDialog?.setCancelable(false)
        mProgressDialog?.setMessage("Please Wait...")
        mProgressDialog?.show()
        EncodeAsyncTask(this)
            .execute(filepath, messageToEncodeEditText.text.toString())
    }

    override fun onEncodeTaskCompleted(file : File?) {
        if (file != null) {
            MediaScannerConnection.scanFile(this, arrayOf(file.toString()), null
            ) { _, _ ->
                encodedImageView.setImageURI(Uri.parse(file.absolutePath))
            }
            Toast.makeText(this, "Image saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
        else
            Toast.makeText(this, "Encoding failed!", Toast.LENGTH_LONG).show()

        mProgressDialog?.dismiss()
    }

    private fun decode(picUri : Uri) {
        val projection = arrayOf(MediaStore.Images.Media.DATA)

        val cursor = contentResolver.query(picUri, projection, null, null, null)
        cursor!!.moveToFirst()

        val columnIndex = cursor.getColumnIndex(projection[0])
        val filepath = cursor.getString(columnIndex)
        cursor.close()

        mProgressDialog?.setCancelable(false)
        mProgressDialog?.setMessage("Please Wait...")
        mProgressDialog?.show()
        DecodeAsyncTask(this).execute(filepath)
    }

    override fun onDecodeTaskCompleted(bitmapMessagePair: Pair<Bitmap?, String?>) {
        val bitmap = bitmapMessagePair.first
        val message = bitmapMessagePair.second

        if (bitmap != null && message != null) {
            decodedImageView.setImageBitmap(bitmap)
            decodedMessageTextView.text = message
        }
        else
            decodedMessageTextView.text = resources.getString(R.string.decoding_failed_msg)

        mProgressDialog?.dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            ENCODE_SELECTED_PIC -> if (resultCode == Activity.RESULT_OK) {
                encode(data!!.data!!)
            }

            DECODE_SELECTED_PIC -> if (resultCode == Activity.RESULT_OK) {
                decode(data!!.data!!)
            }
            else -> {

            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_WRITE_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // related task you need to do.
                    hasStorageWritePermission = true
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

}
