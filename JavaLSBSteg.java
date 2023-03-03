import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;

import java.awt.image.BufferedImage;
import java.awt.FileDialog;
import java.io.FilenameFilter;
import java.awt.Frame;
import javax.imageio.ImageIO;

/**
 * This class provides two tools for injecting and extracting files into images using LSB Steganography
 *
 * The use of the singleton patern was done because the method itself is not complicated and
 * it does not need to many parts in order to work
 */
public class JavaLSBSteg {

    private static final JavaLSBSteg instance = new JavaLSBSteg();

    private JavaLSBSteg() {}

    public static JavaLSBSteg getInstance() {
        return instance;
    }

    /**
     * This function runs the inject function to inject a file into an image
     *
     * The purpose of this function is to give the user a UI option to select the files that they want to hide 
     * withought the need to insert the file paths into program arguments.
     *
     * Also it lets you see the image...so mostly just that.
     *
     */
    public static void hideFileDialog() throws IOException {
        Frame frm = new Frame();
        FileDialog fileDialog = new FileDialog(frm,"Select image to hide file in");
        fileDialog.setVisible(true);
        String imageFile = fileDialog.getDirectory() + fileDialog.getFile();
        
        fileDialog.setTitle("Select file to hide");
        fileDialog.setFile("");
        fileDialog.setVisible(true);
        String payloadFile = fileDialog.getDirectory() + fileDialog.getFile();
        if (imageFile == null || payloadFile == null) {
            System.out.println("Files not selected! please select files");
            System.exit(1);
        } else {
            inject(imageFile, payloadFile);
        }
        fileDialog.dispose();
        frm.dispose();
        
    }

    /**
     * This function runs the extract function to extract a file hidden in an image 
     *
     * The purpose of this function is to give the user a UI option to select the payload PNG image and where the file is to be saved after being extracted.
     * This gives the user the ability to select their own file extention and to find the image easier
     * 
     * This function as well as the hideFileDialog() as just for extra utility/debuging purposes
     */
    public static void extractFileDialog() throws IOException {
        Frame frm = new Frame();
        FileDialog fileDialog = new FileDialog(frm,"Select image");
        fileDialog.setVisible(true);
        String imageFile = fileDialog.getDirectory() + fileDialog.getFile();
        
        fileDialog.setTitle("Select file to hide");
        fileDialog.setFile("");
        fileDialog.setVisible(true);
        String payloadFile = fileDialog.getDirectory() + fileDialog.getFile();
        if (imageFile == null || payloadFile == null) {
            System.out.println("Files not selected! please select files");
            System.exit(1);
        } else {
            inject(imageFile, payloadFile);
        }
        fileDialog.dispose();
        frm.dispose();
        
    }

    /**
       * This function selects a file to hide inside an image using Least Significant Bit steganography.
       * By importing an image as a bitmap image we can hide a file inside it's pixels
       *
       * Pixel(32 bits):
       *  A(lpha)   R(ed)   G(reen)  B(lue)
       * 10010101 01101010 01010110 01011001

       * By nibbling a byte into just two bits (00, 01, 10 or 11) we can evenly distribute them into a single pixel.
       * Using the alpha channel forces the code to export the image as a PNG image
       *
       * NOTE: the exported file contains the size of the payload that was inserted. 
       * This is needed in order to extract the file after it has been injected
       *
       * @param imageFile the full file path for the image to hide the payload in
       * @param payloadFile the full file path for the payload file that is to be hidden in the image
     */
    private static void inject(String imageFile, String payloadFile) throws IOException {

        // Reading the Input BitMap image. This is the image that we will
        // inject with our payload file
        BufferedImage img = null;
        img = ImageIO.read(new File(imageFile));


        // Reading the payload file. this file will be injected in our image
        // When extracting the file from a stegonographed image we do not need to know
        // the type of file we are exporting from it (as any file that is given as a
        // payload...
        // will be treated as an array of bytes) BUT we DO need to know it's size
        File file = new File(payloadFile);
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
                    int rgbIn = img.getRGB(i, j); // Get the 32-Bit Pixel
                    // while injecting the file, mask the byte into the new 32-Bit Pixel,
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

    /**
       * This function selects a PNG image that has hidden information in it and extracts the information by giving it the size of the payload.
       *
       * The function works by in the reverse process that the inject functino works. 
       * By selecting the last two bits of a given pixel, it then reconstructs a single byte and once all 
       * the bytes as defined by the payloadSize variable have been recovered it exports it to a file defined by the outputFile path
       *
       * NOTE: The function needs the size of the payload in order to know how many bytes to extract
       * NOTE: There is nothing that checks for corupted data, the payloads integrity is NOT guarantied
       *
       * @param payloadFile the full file path for the PNG image with hidden information in it
       * @param outputFile the full file path for the file to be saved as
       * @param payloadSize the size of the payload so that the function knows when to stop extracting bytes
     */
    private static void extract(String payloadFile, String outputFile, int payloadSize) throws IOException {
        File in = new File(payloadFile);
        int fileSize = payloadSize;

        BufferedImage img = null;
        img = ImageIO.read(in);

        FileOutputStream fout = new FileOutputStream(outputFile);

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
        System.out.println("Succesfully extracted: " + count + " bytes of information");
    }
}
