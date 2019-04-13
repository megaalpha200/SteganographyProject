package tk.jaaproductions.steganography.steganography.Helpers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import tk.jaaproductions.steganography.steganography.Interfaces.OnDecodeTaskCompleted
import java.lang.Exception

class DecodeAsyncTask(private val listener : OnDecodeTaskCompleted) : AsyncTask<String, Void, Pair<Bitmap?, String?>>() {
    val TAG = "DecodeAsyncTask"

    override fun doInBackground(vararg params: String?): Pair<Bitmap?, String?> {
        var message : String? = null
        var originalBitmap : Bitmap? = null

        try {
            originalBitmap = BitmapFactory.decodeFile(params[0]!!)
            message = Steganography.retrieveEncodedMessageFromImage(originalBitmap)
        }
        catch (ex : Exception) {
            Log.e(TAG, ex.toString())
        }

        return Pair(originalBitmap, message)
    }

    override fun onPostExecute(result: Pair<Bitmap?, String?>) {
        listener.onDecodeTaskCompleted(result)
    }
}