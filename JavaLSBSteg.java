import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.DataInputStream;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

public class JavaLSBSteg {

    public static void main(String[] args) throws IOException {

        // Reading the Input BitMap image. This is the image that we will
        // inject with our payload file
        BufferedImage img = null;
        img = ImageIO.read(new File(args[0]));


        // Reading the payload file. this file will be injected in our image
        // When extracting the file from a stegonographed image we do not need to know
        // the type of file we are exporting from it (as any file that is given as a
        // payload...
        // will be treated as an array of bytes) BUT we DO need to know it's size
        File file = new File(args[1]);
        byte[] fileData = new byte[(int) file.length()];
        DataInputStream dis = new DataInputStream(new FileInputStream(file));
        dis.readFully(fileData);
        dis.close();

        // After we import the payload file we create the new image that we will need to
        // export all our information in
        BufferedImage imgOut = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);

        // LSB STEGANOGRAPHY
        int count = 0; // this is the counter for the payload file, so that it injects the bytes one by one
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                if (count < file.length()) {
                    int rgbIn = img.getRGB(i, j); // Get the 24-Bit Pixel
                    // while injecting the file, mask the byte into the new 24-Bit Pixel, two at a time
                    // Ok look, I was High af when I wrote this ok I'll exaplain later
                    int nib_LL = fileData[count] & 0x3;
                    int nib_LH = fileData[count] >> 2 & 0x3;
                    int nib_HL = fileData[count] >> 4 & 0x3;
                    int nib_HH = fileData[count] >> 6 & 0x3;
                    int rgbOut = (rgbIn & 0xfcfcfcfc) | ((nib_HH << 24) | (nib_HL << 16) | (nib_LH << 8) | (nib_LL)); // nibble a byte into 2-bit sub nibbles and replace the last two bits of every channel (Red Green Blue Alpha) 
                    imgOut.setRGB(i, j, rgbOut);
                    count++;
                } else {
                    //when file is finished reading, then load the rest of the image normaly
                    int rgbIn = img.getRGB(i, j);
                    imgOut.setRGB(i, j, rgbIn);
                }
            }
        }
        ImageIO.write(imgOut, "png", new File("./out_s_" + file.length() + "_s_.png"));
        System.out.println("Steganography of file size: " + file.length());
    }
}
