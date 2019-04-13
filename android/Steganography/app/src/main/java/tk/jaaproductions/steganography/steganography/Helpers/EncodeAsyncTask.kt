package tk.jaaproductions.steganography.steganography.Helpers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import tk.jaaproductions.steganography.steganography.Interfaces.OnEncodeTaskCompleted
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*

class EncodeAsyncTask(private val listener : OnEncodeTaskCompleted) : AsyncTask<String, Void, Bitmap?>() {
    val TAG = "EncodeAsyncTask"

    override fun doInBackground(vararg params: String?): Bitmap? {
        var newBitmap : Bitmap? = null

        try {
             newBitmap = Steganography.embedMessage(BitmapFactory.decodeFile(params[0]), params[1]!!)
        }
        catch (ex : Exception) {
            Log.e(TAG, ex.toString())
        }

        return newBitmap
    }

    override fun onPostExecute(result: Bitmap?) {
        var file : File? = null

        if (result != null) {
            val encoded_imgs_directory =
                Paths.get("${Environment.getExternalStorageDirectory().absolutePath}/Encoded")
            if (!Files.exists(encoded_imgs_directory))
                Files.createDirectory(encoded_imgs_directory)

            val df = SimpleDateFormat("yyyyMMddhhmmss", Locale.US)
            file = File(encoded_imgs_directory.toString(), "${df.format(Date())}.jpg")
            val outputStream = FileOutputStream(file)

            result.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        }

        listener.onEncodeTaskCompleted(file)
    }
}