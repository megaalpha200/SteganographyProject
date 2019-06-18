import Steganography.Companion.embedMessage
import Steganography.Companion.generateEncodedImage
import Steganography.Companion.retrieveEncodedMessageFromImage
import java.io.File
import java.lang.NumberFormatException
import javax.imageio.ImageIO

fun main() {
    do {
        try {
            println("Hello! Welcome to Image Encoder (Data in Picture)!")
            println("Created by: Jose A. Alvarado")
            println("Copyright J.A.A. Productions 2019")
            println()
            println("Please select an option...")
            println("1. Encode")
            println("2. Decode")
            println("3. Quit")
            println()
            print("Choice: ")

            val userChoice = readLine()!!

            when (Integer.parseInt(userChoice)) {
                1 -> {
                    println()
                    encode()
                }
                2->{
                    println()
                    decode()
                }
                3-> {
                    println("Goodbye!")
                    return
                }
                else -> throw WrongMenuChoiceException()
            }

        }
        catch (e: NoSuchFileException) {
            println("Picture does not exist!")
        }
        catch (e: NumberFormatException) {
            println("Please enter a number from 1 to 3!")
        }
        catch (e: Exception) {
            println(e.toString())
        }

        println()
    } while (true)
}

fun encode() {
    print("Enter your message: ")
    val nameInput = readLine()!!
    println()
    print("Enter the location of the image: ")
    val originalPath = readLine()!!
    println()

    if (!checkIfFileExists(originalPath))
        throw NoSuchFileException(File(originalPath))

    print("Enter the location of the encoded image: ")
    val newPath = readLine()!!
    println()

    val encodedImgSaveLoc = generateEncodedImage(embedMessage(ImageIO.read(File(originalPath)), nameInput), newPath)
    println()
    println("Encoded Image Saved At: $encodedImgSaveLoc")
    println("Image encoded successfully!")
}

fun decode() {
    print("Enter the location of the encoded image: ")
    val picPath = readLine()!!

    if (!checkIfFileExists(picPath))
        throw NoSuchFileException(File(picPath))

    println("The message is \"${retrieveEncodedMessageFromImage(picPath)}\"")
}

private fun checkIfFileExists(filename: String): Boolean {
    return File(filename).exists()
}

private class WrongMenuChoiceException : Exception() {
    override fun toString(): String {
        return this.javaClass.canonicalName + ": Invalid Choice!"
    }
}


