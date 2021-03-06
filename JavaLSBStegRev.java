import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

public class JavaLSBStegRev {
    public static void main(String[] args) throws IOException {

        File in = new File(args[0]);
        int fileSize = Integer.parseInt(in.getName().split("_s_")[1]); // Get Payload file size from filename:
                                                                       // "xxxxxx_s_<<PAYLOAD_SIZE>>_s_.bmp"
        BufferedImage img = null;
        img = ImageIO.read(in);

        FileOutputStream fout = new FileOutputStream("./export.out");

        int count = 0;
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                if (count < fileSize) {
                    int rgb = img.getRGB(i, j);
                    int nib_LL = (rgb & 0x00000003);
                    int nib_LH = (rgb & 0x00000300) >> 8;
                    int nib_HL = (rgb & 0x00030000) >> 16;
                    int nib_HH = (rgb & 0x03000000) >> 24;
                    int data = 0x000000ff & ((nib_HH << 6) | 
                                             (nib_HL << 4) | 
                                             (nib_LH << 2) | 
                                             (nib_LL));
                                             
                    fout.write(data);
                    count++;
                }
            }
        }
        fout.close();
        System.out.println("Succesfully extracted: " + count + " bytes of hidden information");
    }
}
