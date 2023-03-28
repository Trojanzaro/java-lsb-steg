import java.io.IOException;
import java.lang.IllegalArgumentException;

public class MainClass {
    public static void main(String[] args) throws IOException {
        try {
            if(args.length < 1 || args.length > 1) {
                throw new IllegalArgumentException("Only one argument REQUIRED! -h for hiding -e for extracting");
            } else {
                switch(args[0]) {
                    case "-h":
                        JavaLSBSteg.hideFileDialog();
                        break;
                    case "-e":
                        JavaLSBSteg.extractFileDialog();
                        break;
                    default:
                        throw new IllegalArgumentException("Usage 'java MainClass <option>' option: -h for hiding -e for extracting");
                }
            }
        } catch(Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, e.toString());
            System.exit(-1);
        }
    }
}
