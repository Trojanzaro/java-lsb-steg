import java.awt.image.BufferedImage;
import java.io.*;

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
        BufferedImage imgOut = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);

        // LSB STEGANOGRAPHY
        int count = 0; // this is the counter for the payload file, so that it injects the bytes one by one
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                if (count < file.length()) {
                    int rgbIn = img.getRGB(i, (j++)); // Get the 24-Bit Pixel
                    int rgbIn2 = img.getRGB(i, j); // Get Reference bellow comment
                    // while injecting the file, mask the byte into the new 24-Bit Pixel
                    // Ok look, I was High af when I wrote this ok I'll exaplain later
                    int nib_LL = fileData[count] & 0x03;
                    int nib_LH = fileData[count] >> 2 & 0x03;
                    int nib_HL = fileData[count] >> 4 & 0x03;
                    int nib_HH = fileData[count] >> 6 & 0x03;
                    int rgbOut = (0xff000000 | (nib_LH << 8 | nib_LL) | (rgbIn & 0xfffffcfc)); // x->ff `ff fc /*3 Low bits on 'g' and 'b'*/`  
                    int rgbOut2 = (0xff000000 | (nib_HH << 8 | nib_HL) | (rgbIn2 & 0xffffcfc)); // x->ff `ff fc /*3 Low bits on 'g' and 'b'*/`  of next pixel
                    imgOut.setRGB(i, j-1, rgbOut);
                    imgOut.setRGB(i, j, rgbOut2);
                    //System.out.printf("d: %x\nPixel1: %x sPixel1: %x\n", fileData[count], rgbIn, rgbOut);
                    //System.out.printf("Pixel2: %x sPixel2: %x\n", rgbIn2, rgbOut2);
                    //System.out.println();
                    count++;
                } else {
                    //when file is finished reading, then load the rest of the image normaly
                    int rgbIn = img.getRGB(i, j);
                    imgOut.setRGB(i, j, rgbIn);
                }
            }
        }

        ImageIO.write(imgOut, "BMP", new File("./out_s_" + file.length() + "_s_.bmp"));
        System.out.println("Steganography of file size: " + file.length());
    }
}