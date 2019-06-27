/*
* Created By: Jose A. Alvarado
* */

import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import java.lang.StringBuilder
import javax.imageio.ImageIO

class Steganography {
    companion object {
        private const val delimiter = "11111110"

        private fun convertMessageToBinary(msg : String) : Pair<ArrayList<String>, Map<String, Char>> {
            val binaryNameArrayList = arrayListOf<String>()
            val charBinMap = mutableMapOf<String, Char>()

            println("Message: $msg")
            print("Binary Representation: ")

            msg.forEach {
                val currCharBinRepresentation = String.format("%08d", Integer.toBinaryString(it.toInt()).toInt())
                print("$currCharBinRepresentation ")
                charBinMap[currCharBinRepresentation] = it
                binaryNameArrayList.add(currCharBinRepresentation)
            }

            println()

            return Pair(binaryNameArrayList, charBinMap)
        }

        fun generateEncodedImage(image : BufferedImage, picturePath : String) : String {
            val picturePathFile = File(picturePath)

            ImageIO.write(image, "png", picturePathFile)
            return picturePathFile.absolutePath
        }

        fun embedMessage(picture : BufferedImage, msg : String) : BufferedImage {
            val originalBufferedImg : BufferedImage = picture
            val picWidth : Int = originalBufferedImg.width
            val picHeight : Int = originalBufferedImg.height
            val newBufferedImage = BufferedImage(picWidth, picHeight, originalBufferedImg.type)

            val convertMessageToBinaryResult = convertMessageToBinary(msg)
            val binConvertedMsgArrayList = convertMessageToBinaryResult.first
            val binConvertedNameCharBinMap = convertMessageToBinaryResult.second

            val separator = System.lineSeparator()
            val file = File(".\\debug.txt")
            val outputStream = FileOutputStream(file)

            outputStream.write("Message: $msg$separator$separator".toByteArray())
            outputStream.write("Binary Representation:$separator".toByteArray())

            val binConvertedNameStringBuilder = StringBuilder("")
            binConvertedMsgArrayList.forEach {
                binConvertedNameStringBuilder.append(it)
                outputStream.write("$it ".toByteArray())
            }
            binConvertedNameStringBuilder.append(delimiter)

            val binConvertedNameCharArray = binConvertedNameStringBuilder.toString().toCharArray()

            outputStream.write("$separator$separator".toByteArray())
            outputStream.write("Image in Binary...$separator".toByteArray())

            var count = 0
            for (yIndex in (0 until picHeight)) {
                for (xIndex in (0 until picWidth)) {
                    val pixel = originalBufferedImg.getRGB(xIndex, yIndex)
                    val rgbBinStr = Integer.toBinaryString(pixel)
                    outputStream.write("Pixel (x:$xIndex, y:$yIndex): $separator".toByteArray())

                    if (count < binConvertedNameCharArray.size) {
                        val currLetterIndex = count / 8
                        val currBitIndex = count % 8

                        if (currLetterIndex < binConvertedMsgArrayList.size) {
                            val currLetterBin = binConvertedMsgArrayList[currLetterIndex]
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

                        newBufferedImage.setRGB(xIndex, yIndex, newPixel)
                        count++
                    }
                    else {
                        newBufferedImage.setRGB(xIndex, yIndex, pixel)
                        outputStream.write("NO ENCODING$separator$rgbBinStr$separator".toByteArray())
                    }

                    outputStream.write(separator.toByteArray())
                }
            }

            outputStream.flush()
            outputStream.close()

            return newBufferedImage
        }

        fun retrieveEncodedMessageFromImage(picturePath: String) : String {
            val bufferedImg : BufferedImage = ImageIO.read(File(picturePath))
            val picWidth : Int = bufferedImg.width
            val picHeight : Int = bufferedImg.height

            val tempChar = StringBuilder("")
            val encodedMessageStringBuilder = StringBuilder("")

            var count = 0
            outer@ for ((_, yIndex) in (0 until picHeight).withIndex()) {
                for ((_, xIndex) in (0 until picWidth).withIndex()) {
                    val pixel = bufferedImg.getRGB(xIndex, yIndex)
                    val rgbBinStr = Integer.toBinaryString(pixel)

                    val rgbBinCharArray = rgbBinStr.toCharArray()

                    if (count < 8) {
                        tempChar.append(rgbBinCharArray[rgbBinCharArray.lastIndex])
                        count++

                        if (count == 8) {
                            count = 0

                            if (tempChar.toString() == delimiter) {
                                tempChar.clear()
                                break@outer
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