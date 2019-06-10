import javafx.util.Pair;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Steganography {

    private static final String delimiter = "11111110";

    private static Pair<ArrayList<String>, Map<String, Character>> convertMessageToBinary(String name) {
        final ArrayList<String> binaryNameArrayList = new ArrayList<>(0);
        final HashMap<String, Character> charBinMap = new HashMap<>(0);

        System.out.println("Name: " + name);
        System.out.println("Binary Representation: ");

        for (char c : name.toCharArray()) {
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

    public static BufferedImage embedMessage(BufferedImage picture, String name) throws FileNotFoundException, IOException {
        final BufferedImage originalBufferedImg = picture;
        final int picWidth = originalBufferedImg.getWidth();
        final int picHeight = originalBufferedImg.getHeight();
        final BufferedImage newBufferedImg = new BufferedImage(picWidth, picHeight, originalBufferedImg.getType());

        final Pair<ArrayList<String>, Map<String, Character>> convertMessageToBinaryResult = convertMessageToBinary(name);
        final ArrayList<String> binConvertedNameArrayList = convertMessageToBinaryResult.getKey();
        final Map<String, Character> binConvertedNameCharBinMap = convertMessageToBinaryResult.getValue();

        final String separator = System.lineSeparator();
        final File file = new File(".\\debug.txt");
        final FileOutputStream outputStream = new FileOutputStream(file);

        outputStream.write(("Message: " + name + separator + separator).getBytes());
        outputStream.write(("Binary Representation: " + separator).getBytes());

        final StringBuilder binConvertedNameStringBuilder = new StringBuilder();
        for (String it : binConvertedNameArrayList) {
            binConvertedNameStringBuilder.append(it);
            outputStream.write((it + " ").getBytes());
        }
        binConvertedNameStringBuilder.append(delimiter);

        final char[] binConvertedNameCharArray = binConvertedNameStringBuilder.toString().toCharArray();

        outputStream.write((separator + separator).getBytes());
        outputStream.write(("Image in Binary..." + separator).getBytes());

        int count = 0;
        for (int yIndex = 0; yIndex < picHeight; yIndex++) {
            for (int xIndex = 0; xIndex < picWidth; xIndex++) {
                final int pixel = originalBufferedImg.getRGB(xIndex, yIndex);
                final String rgbBinStr = Integer.toBinaryString(pixel);
                outputStream.write(("Pixel (x:" + xIndex + ", y:" + yIndex + "): " + separator).getBytes());

                if (count < binConvertedNameCharArray.length) {
                    final int currLetterIndex = count / 8;
                    final int currBitIndex = count % 8;

                    if (currLetterIndex < binConvertedNameArrayList.size()) {
                        final String currLetterBin = binConvertedNameArrayList.get(currLetterIndex);
                        final char currLetter = binConvertedNameCharBinMap.get(currLetterBin);

                        final StringBuilder currLetterBinStrBuilder = new StringBuilder(currLetterBin);
                        final char tempChar = currLetterBinStrBuilder.toString().toCharArray()[currBitIndex];
                        currLetterBinStrBuilder.replace(currBitIndex, currBitIndex + 1, "[");
                        currLetterBinStrBuilder.insert(currBitIndex + 1, tempChar + "]");

                        outputStream.write(("Current Letter: " + (Character.isWhitespace(currLetter) ? "[SPACE]" : currLetter) + " - " + currLetterBinStrBuilder + separator).getBytes());
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

                    final StringBuilder newRgbBinStrBuilder = new StringBuilder();
                    for (char c : rgbBinCharArray) {
                        newRgbBinStrBuilder.append(c);
                    }
                    final String newRgbBinStr = newRgbBinStrBuilder.toString();
                    final int newPixel = Integer.parseUnsignedInt(newRgbBinStr, 2);

                    outputStream.write((newRgbBinStr + " -> " + newRgbBinStrBuilder + separator).getBytes());

                    newBufferedImg.setRGB(xIndex, yIndex, newPixel);
                    count++;
                }
                else {
                    newBufferedImg.setRGB(xIndex, yIndex, pixel);
                    outputStream.write(("NO ENCODING" + separator + rgbBinStr + separator).getBytes());
                }

                outputStream.write(separator.getBytes());
            }
        }

        outputStream.flush();
        outputStream.close();

        return newBufferedImg;
    }

    public static String retrieveEncodedMessageFromImage(String picturePath) throws IOException {
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

        return encodedMessageStringBuilder.toString();
    }
}