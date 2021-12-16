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

        FileOutputStream fout = new FileOutputStream("./testoutput");

        int count = 0;
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                if (count < fileSize) {
                    int rgb = img.getRGB(i, (j++));
                    int rgb2 = img.getRGB(i, j);
                    int nib_LL = (rgb & 0x00000003);
                    int nib_LH = (rgb & 0x00000300) >> 8;
                    int nib_HL = (rgb2 & 0x00000003);
                    int nib_HH = (rgb2 & 0x00000300) >> 8;
                    int data = 0x000000ff & ((nib_HH << 6) | 
                                             (nib_HL << 4) | 
                                             (nib_LH << 2) | 
                                             (nib_LL));
                    //System.out.printf("d: %x\nLL: %x LH: %x HL: %x HH: %x \n", data, nib_LL, nib_LH, nib_HL, nib_HH);
                                             
                    fout.write(data);
                    count++;
                }
            }
        }
        fout.close();
        System.out.println("Succesfully extracted: " + count + " bytes of hidden information");
    }
}
