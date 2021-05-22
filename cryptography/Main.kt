package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.Exception
import kotlin.experimental.xor

fun Byte.bitByBit(action: (Int) -> Unit) {
    val byte = this.toInt()
    7.downTo(0).forEach {
        action(byte.shr(it).and(1))
    }
}

fun main() {

    do {
        println("Task (hide, show, exit):")
        val task = readLine()!!

        when (task) {
            "hide" -> {
                println("Input image file:")
                val input = readLine()!!
                println("Output image file:")
                val output = readLine()!!
                println("Message to hide:")
                val message = readLine()!!
                println("Password:")
                val password = readLine()!!
                try {
                    if (hide(input, output, message, password)) {
                        println("Message saved in: $output image.")
                    }
                } catch (e: Exception) {
                    println(e.message)
                }
            }
            "show" -> {
                println("Input image file:")
                val input = readLine()!!
                println("Password:")
                val password = readLine()!!
                val message = show(input, password)
                println("Message: $message")
            }
            "exit" -> println("Bye!")
            else -> println("Wrong task: $task")
        }

    } while (task != "exit")
}

fun hide(input: String, output: String, message: String, password: String): Boolean {

    val image = ImageIO.read(File(input))

    val width = image.width
    val height = image.height

    if ((width * height) < message.length * 8) {
        throw Exception("The input image is not large enough to hold this message.")
    }

    var x = 0
    var y = 0

    xor(message.encodeToByteArray(), password.encodeToByteArray())
        .plus(0)
        .plus(0)
        .plus(3).forEach { byte ->
            byte.bitByBit { bit ->

                when {
                    x <= width && y < height -> {
                        when (x) {
                            width -> {
                                x = 0
                                y++
                            }
                        }
                        val color = Color(image.getRGB(x, y))
                        if (color.blue % 2 != bit) {
                            val newColor =
                                Color(color.red, color.green, if (bit == 1) color.blue.or(bit) else color.blue.xor(1))
                            image.setRGB(x, y, newColor.rgb)
                        }
                        x++
                    }

                }

            }
        }

    val newImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    newImage.data = image.data
    return ImageIO.write(newImage, "png", File(output))

}

fun xor(message: ByteArray, password: ByteArray): ByteArray {

    var encryptedMessage = byteArrayOf()
    var i = 0
    message.forEach { messageByte ->
        var passwordByte = password.elementAtOrNull(i)
        when (passwordByte) {
            null -> {
                i = 0
                passwordByte = password.elementAt(i)
            }
        }
        encryptedMessage += messageByte.xor(passwordByte!!)
        i++
    }
    return encryptedMessage
}

fun show(input: String, password: String): String {

    val image = ImageIO.read(File(input))
    var message = byteArrayOf()

    val width = image.width
    val height = image.height

    var bitCount = 7
    var sum = 0

    loop@ for (y in 0 until height) {
        for (x in 0 until width) {
            val color = Color(image.getRGB(x, y))
            val bit = color.blue % 2
            sum += 1.shl(bitCount) * bit
            bitCount--
            if (bitCount == -1) {
                message += sum.toByte()
                bitCount = 7
                sum = 0
            }
            if (message.size >= 3) {
                if (message.takeLast(3).sum() == 3) break@loop
            }
        }
    }

    message = message.copyOf(message.size - 3)

    message = xor(message, password.encodeToByteArray())

    return message.toString(Charsets.UTF_8)

}