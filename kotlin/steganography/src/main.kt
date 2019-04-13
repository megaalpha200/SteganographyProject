import Steganography.Companion.embedMessage
import Steganography.Companion.generateEncodedImage
import Steganography.Companion.retrieveEncodedMessageFromImage
import java.io.File
import javax.imageio.ImageIO

fun main() {

    println("Hello! Welcome to Image Encoder!")
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
        else -> println("Invalid Choice")
    }
}

fun encode() {
    print("Enter your message: ")
    val nameInput = readLine()!!
    println()
    print("Enter the location of the image: ")
    val originalPath = readLine()!!
    println()
    print("Enter the location of the encoded image: ")
    val newPath = readLine()!!
    println()

    generateEncodedImage(embedMessage(ImageIO.read(File(originalPath)), nameInput), newPath)
    println("Image encoded successfully!")
}

fun decode() {
    print("Enter the location of the encoded image: ")
    val picPath = readLine()!!

    println("The message is \"${retrieveEncodedMessageFromImage(picPath)}\"")
}


