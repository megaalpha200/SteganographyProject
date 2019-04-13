package tk.jaaproductions.steganography.steganography.Interfaces

import android.graphics.Bitmap

interface OnDecodeTaskCompleted {
    fun onDecodeTaskCompleted(bitmapMessagePair : Pair<Bitmap?, String?>)
}