import javafx.util.Pair;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Steganography {

    private static final String delimiter = "11111110";

    private static Pair<ArrayList<String>, Map<String, Character>> convertMessageToBinary(String msg) {
        final ArrayList<String> binaryNameArrayList = new ArrayList<>(0);
        final HashMap<String, Character> charBinMap = new HashMap<>(0);

        System.out.println("Message: " + msg);
        System.out.println("Binary Representation: ");

        for (char c : msg.toCharArray()) {
            final String currCharBinRepresentation = String.format("%08d", Integer.parseInt(Integer.toBinaryString((int)c)));
            System.out.print(currCharBinRepresentation + " ");
            charBinMap.put(currCharBinRepresentation, c);
            binaryNameArrayList.add(currCharBinRepresentation);
        }

        System.out.println();

        return new Pair<>(binaryNameArrayList, charBinMap);
    }

    public static String generateEncodedImage(BufferedImage image, String picturePath) throws IOException {
        final File picturePathFile = new File(picturePath);

        ImageIO.write(image, "png", picturePathFile);
        return picturePathFile.getAbsolutePath();
    }

    public static BufferedImage embedMessage(BufferedImage picture, String msg) throws FileNotFoundException, IOException {
        Date startTime = new Date();

        final BufferedImage originalBufferedImg = picture;
        final int picWidth = originalBufferedImg.getWidth();
        final int picHeight = originalBufferedImg.getHeight();
        final BufferedImage newBufferedImg = new BufferedImage(originalBufferedImg.getColorModel(), originalBufferedImg.getRaster(), originalBufferedImg.isAlphaPremultiplied(), null);

        final Pair<ArrayList<String>, Map<String, Character>> convertMessageToBinaryResult = convertMessageToBinary(msg);
        final ArrayList<String> binConvertedNameArrayList = convertMessageToBinaryResult.getKey();
        final Map<String, Character> binConvertedNameCharBinMap = convertMessageToBinaryResult.getValue();

        final String separator = System.lineSeparator();
        final File file = new File(".\\debug.txt");
        final FileOutputStream outputStream = new FileOutputStream(file);

        outputStream.write(("Message: " + msg + separator+ separator).getBytes());
        outputStream.write(("Binary Representation: " + separator).getBytes());

        final String binConvertedMsgString = String.join("", binConvertedNameArrayList) + delimiter;
        outputStream.write(String.join(" ", binConvertedNameArrayList).getBytes());

        final char[] binConvertedNameCharArray = binConvertedMsgString.toCharArray();

        outputStream.write((separator + separator).getBytes());
        outputStream.write(("Image in Binary..." + separator).getBytes());

        int count = 0;
        outer: for (int yIndex = 0; yIndex < picHeight; yIndex++) {
            for (int xIndex = 0; xIndex < picWidth; xIndex++) {
                final int pixel = originalBufferedImg.getRGB(xIndex, yIndex);
                final String rgbBinStr = Integer.toBinaryString(pixel);

                if (count < binConvertedNameCharArray.length) {
                    outputStream.write(("Pixel (x:"+ xIndex + ", y:" + yIndex+ "): "+ separator).getBytes());

                    final int currLetterIndex = count / 8;
                    final int currBitIndex = count % 8;

                    if (currLetterIndex < binConvertedNameArrayList.size()) {
                        final String currLetterBin = binConvertedNameArrayList.get(currLetterIndex);
                        final char currLetter = binConvertedNameCharBinMap.get(currLetterBin);

                        final StringBuilder currLetterBinStrBuilder = new StringBuilder(currLetterBin);
                        final char tempChar = currLetterBinStrBuilder.toString().toCharArray()[currBitIndex];
                        currLetterBinStrBuilder.replace(currBitIndex, currBitIndex + 1, "[");
                        currLetterBinStrBuilder.insert(currBitIndex + 1, tempChar + "]");

                        outputStream.write(("Current Letter: " + (Character.isWhitespace(currLetter) ? "[SPACE]" : currLetter + " - ") + currLetterBinStrBuilder + separator).getBytes());
                    }
                    else {
                        final StringBuilder delimiterStrBuilder = new StringBuilder(delimiter);
                        final char tempChar = delimiterStrBuilder.toString().toCharArray()[currBitIndex];
                        delimiterStrBuilder.replace(currBitIndex, currBitIndex + 1, "[");
                        delimiterStrBuilder.insert(currBitIndex + 1, tempChar + "]");

                        outputStream.write(("Current Letter: [DELIMITER] - " + delimiterStrBuilder + separator).getBytes());
                    }

                    final char[] rgbBinCharArray = rgbBinStr.toCharArray();
                    rgbBinCharArray[rgbBinCharArray.length - 1] = binConvertedNameCharArray[count];

                    final String newRgbBinStr = new String(rgbBinCharArray);
                    final int newPixel = Integer.parseUnsignedInt(newRgbBinStr, 2);

                    outputStream.write((rgbBinStr + " -> " + newRgbBinStr + separator).getBytes());

                    newBufferedImg.setRGB(xIndex, yIndex, newPixel);
                    count++;
                }
                else {
                    /*newBufferedImg.setRGB(xIndex, yIndex, pixel);
                    outputStream.write("NO ENCODING").append(separator).append(rgbBinStr).append(separator);*/
                    break outer;
                }

                outputStream.write(separator.getBytes());
            }
        }

        outputStream.flush();
        outputStream.close();

        Date endTime = new Date();
        double timeDiff = (endTime.getTime() - startTime.getTime()) + 0.0;
        System.out.println("\nEncoding Time: " + timeDiff +" milliseconds\n\n");

        return newBufferedImg;
    }

    public static String retrieveEncodedMessageFromImage(String picturePath) throws IOException {
        Date startTime = new Date();

        final BufferedImage bufferedImg = ImageIO.read(new File(picturePath));
        final int picWidth = bufferedImg.getWidth();
        final int picHeight = bufferedImg.getHeight();

        StringBuilder tempChar = new StringBuilder();
        final StringBuilder encodedMessageStringBuilder = new StringBuilder();

        int count = 0;
        outer: for(int yIndex = 0; yIndex < picHeight; yIndex++) {
            for(int xIndex = 0; xIndex < picWidth; xIndex++) {
                final int pixel = bufferedImg.getRGB(xIndex, yIndex);
                final String rgbBinStr = Integer.toBinaryString(pixel);

                final char[] rgbBinCharArray = rgbBinStr.toCharArray();

                if (count < 8) {
                    tempChar.append(rgbBinCharArray[rgbBinCharArray.length - 1]);
                    count++;

                    if (count == 8) {
                        count = 0;

                        if (tempChar.toString().equals(delimiter)) {
                            tempChar = new StringBuilder();
                            break outer;
                        }
                        else {
                            encodedMessageStringBuilder.append((char)(Integer.parseInt(tempChar.toString(), 2)));
                            tempChar = new StringBuilder();
                        }
                    }
                }
            }
        }

        Date endTime = new Date();
        double timeDiff = (endTime.getTime() - startTime.getTime()) + 0.0;
        System.out.println("\nDecoding Time: " + timeDiff +" milliseconds\n\n");

        return encodedMessageStringBuilder.toString();
    }
}