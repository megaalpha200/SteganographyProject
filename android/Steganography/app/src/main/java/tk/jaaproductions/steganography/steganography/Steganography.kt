import android.graphics.Bitmap
import android.graphics.BitmapRegionDecoder
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.lang.StringBuilder
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*

class Steganography {
    companion object {
        private const val delimiter = "11111110"

        private fun convertMessageToBinary(name : String) : Pair<ArrayList<String>, Map<String, Char>> {
            val binaryNameArrayList = arrayListOf<String>()
            val charBinMap = mutableMapOf<String, Char>()

            println("Message: $name")
            print("Binary Representation: ")

            name.forEach {
                val currCharBinRepresentation = String.format("%08d", Integer.toBinaryString(it.toInt()).toInt())
                print("$currCharBinRepresentation ")
                charBinMap[currCharBinRepresentation] = it
                binaryNameArrayList.add(currCharBinRepresentation)
            }

            println()

            return Pair(binaryNameArrayList, charBinMap)
        }

        fun embedMessage(picture : Bitmap, name : String) : Bitmap {
            val originalBitmapImg : Bitmap = picture
            val picWidth : Int = originalBitmapImg.width
            val picHeight : Int = originalBitmapImg.height
            val newBitmapImage = originalBitmapImg.copy(Bitmap.Config.ARGB_8888, true)

            val convertMessageToBinaryResult = convertMessageToBinary(name)
            val binConvertedNameArrayList = convertMessageToBinaryResult.first
            val binConvertedNameCharBinMap = convertMessageToBinaryResult.second



            val separator = System.lineSeparator()
            val encoded_imgs_directory =
                Paths.get("${Environment.getExternalStorageDirectory().absolutePath}/Encoded")
            if (!Files.exists(encoded_imgs_directory))
                Files.createDirectory(encoded_imgs_directory)

            val df = SimpleDateFormat("yyyyMMddhhmmss", Locale.US)
            val file = File(encoded_imgs_directory.toString(), "${df.format(Date())}.txt")
            val outputStream = FileOutputStream(file)

            outputStream.write("Message: $name$separator$separator".toByteArray())
            outputStream.write("Binary Representation:$separator".toByteArray())

            val binConvertedNameStringBuilder = StringBuilder("")
            binConvertedNameArrayList.forEach {
                binConvertedNameStringBuilder.append(it)
                outputStream.write("$it ".toByteArray())
            }
            binConvertedNameStringBuilder.append(delimiter)

            val binConvertedNameCharArray = binConvertedNameStringBuilder.toString().toCharArray()

            outputStream.write("$separator$separator".toByteArray())
            outputStream.write("Image in Binary...$separator".toByteArray())

            var count = 0
            outerEncodeLoop@ for (yIndex in (0 until picHeight)) {
                for (xIndex in (0 until picWidth)) {
                    val pixel = originalBitmapImg.getPixel(xIndex, yIndex)
                    val rgbBinStr = Integer.toBinaryString(pixel)

                    outputStream.write("Pixel (x:$xIndex, y:$yIndex): $separator".toByteArray())

                    if (count < binConvertedNameCharArray.size) {
                        val currLetterIndex = count / 8
                        val currBitIndex = count % 8

                        if (currLetterIndex < binConvertedNameArrayList.size) {
                            val currLetterBin = binConvertedNameArrayList[currLetterIndex]
                            val currLetter = binConvertedNameCharBinMap[currLetterBin]

                            val currLetterBinStrBuilder = StringBuilder(currLetterBin)
                            val tempChar = currLetterBinStrBuilder[currBitIndex]
                            currLetterBinStrBuilder.replace(currBitIndex, currBitIndex + 1, "[")
                            currLetterBinStrBuilder.insert(currBitIndex + 1, "$tempChar]")


                            outputStream.write("Current Letter: ${if (currLetter!!.isWhitespace()) "[SPACE]" else currLetter} - $currLetterBinStrBuilder$separator".toByteArray())
                        }
                        else {
                            val delimiterStrBuilder = StringBuilder(delimiter)
                            val tempChar = delimiterStrBuilder[currBitIndex]
                            delimiterStrBuilder.replace(currBitIndex, currBitIndex + 1, "[")
                            delimiterStrBuilder.insert(currBitIndex + 1, "$tempChar]")

                            outputStream.write("Current Letter: [DELIMITER] - $delimiterStrBuilder$separator".toByteArray())
                        }

                        val rgbBinCharArray = rgbBinStr.toCharArray()
                        rgbBinCharArray[rgbBinCharArray.lastIndex] = binConvertedNameCharArray[count]

                        val newRgbBinStrBuilder = StringBuilder("")
                        rgbBinCharArray.forEach { newRgbBinStrBuilder.append(it) }
                        val newRgbBinStr = newRgbBinStrBuilder.toString()
                        val newPixel = Integer.parseUnsignedInt(newRgbBinStr, 2)

                        outputStream.write("$rgbBinStr -> $newRgbBinStrBuilder$separator".toByteArray())

                        newBitmapImage.setPixel(xIndex, yIndex, newPixel)
                        count++
                    }
                    else {
                        newBitmapImage.setPixel(xIndex, yIndex, pixel)
                        outputStream.write("NO ENCODING$separator$rgbBinStr$separator".toByteArray())
                        //break@outerEncodeLoop
                    }

                    outputStream.write(separator.toByteArray())
                }
            }

            outputStream.flush()
            outputStream.close()

            return newBitmapImage
        }

        fun retrieveEncodedMessageFromImage(picture: Bitmap) : String {
            val bitmapImg : Bitmap = picture
            val picWidth : Int = bitmapImg.width
            val picHeight : Int = bitmapImg.height

            val tempChar = StringBuilder("")
            val encodedMessageStringBuilder = StringBuilder("")

            var count = 0
            outerDecodeLoop@ for (yIndex in (0 until picHeight)) {
                for (xIndex in (0 until picWidth)) {
                    val pixel = bitmapImg.getPixel(xIndex, yIndex)
                    val rgbBinStr = Integer.toBinaryString(pixel)

                    val rgbBinCharArray = rgbBinStr.toCharArray()

                    if (count < 8) {
                        tempChar.append(rgbBinCharArray[rgbBinCharArray.lastIndex])
                        count++

                        if (count == 8) {
                            count = 0

                            if (tempChar.toString() == delimiter) {
                                tempChar.clear()
                                break@outerDecodeLoop
                            }
                            else {
                                encodedMessageStringBuilder.append(Integer.parseInt(tempChar.toString(), 2).toChar())
                                tempChar.clear()
                            }
                        }
                    }
                }
            }

            return encodedMessageStringBuilder.toString()
        }
    }
}
