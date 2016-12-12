package facedetection;

import java.lang.*;
import java.io.*;

public class PPMFile implements xxxFile{


    private byte bytes[]=null;      // bytes which make up binary PPM image
    private double doubles[] = null;
    private String filename=null;     // filename for PPM image
    private int height = 0;
    private int width = 0;

    public PPMFile(String filename)  throws FileNotFoundException, IOException{
        this.filename = filename;
        readImage();
    }

    public int getHeight() {
      return height;
    }

    public int getWidth() {
      return width;
    }
    public byte[] getBytes() {
        return bytes;
    }
    public double[] getDouble() {
      return doubles;
    }
  public static void writeImage(String fn, byte[] data, int width, int height)
    throws FileNotFoundException, IOException {

        if (data != null) {

                FileOutputStream fos = new FileOutputStream(fn);
                fos.write(new String("P6\n").getBytes());
                fos.write(new String( width + " " + height + "\n").getBytes());
                fos.write(new String("255\n").getBytes());
                System.out.println(data.length);
                fos.write(data);
                fos.close();
          }
    }

    private void readImage()  throws FileNotFoundException, IOException, NumberFormatException {

        // read PPM format image
           bytes=null;
            char buffer;                   // character in PPM header
            String id = new String();      // PPM magic number ("P6")
            String dim = new String();     // image dimension as a string
            int count = 0;
            File f = new File(filename);
            FileInputStream isr = new FileInputStream(f);
            boolean weird = false;

            do {
                buffer = (char)isr.read();
                id = id + buffer;
                count ++;
            } while (buffer != '\n' && buffer != ' ');

            if (id.charAt(0) == 'P') {


                buffer = (char)isr.read();count ++;
                if (buffer == '#') {
                  do {
                    buffer = (char)isr.read();count ++;
                  } while (buffer != '\n');
                  count ++;
                  buffer = (char)isr.read();
                }
                // second header line is "width height\n"
                do {
                    dim = dim + buffer;
                    buffer = (char)isr.read();count ++;
                } while (buffer != ' ' && buffer != '\n');

                width = Integer.parseInt(dim);
                //System.out.print(width);
                //System.out.flush();
                dim = new String();
                buffer = (char)isr.read();count ++;
                do {
                    dim = dim + buffer;
                    buffer = (char)isr.read();count ++;
                } while (buffer != ' ' && buffer != '\n');
                height = Integer.parseInt(dim);
                //System.out.println(" X " + height + " pixels.");
                do {                          // third header line is max RGB value, e.g., "255\n"
                    buffer = (char)isr.read();count ++;
                } while (buffer != ' ' && buffer != '\n');

                //System.out.print("Reading image...");
                //System.out.flush();

                // remainder of file is width*height*3 bytes (red/green/blue triples)

                bytes = new byte[height*width];
                doubles = new double[height*width];

                /*
                 Check for weird stuff
                */
                if ((height*width + count*2) < f.length())
                  weird = true;

                if ((id.charAt(1) == '5') || (id.charAt(1) == '6')) {
                  if (!weird)
                    isr.read(bytes,0,height*width);
                  // Now read in as double
                  else {
                    int v =0;
                    for (int i =0; i< height*width; i++) {
                      v = isr.read();
                      v = v + isr.read();
                      v = v + isr.read();
                      v = v / 3;
                      bytes[i] = (byte)( v & 0xFF);
                    }
                  }
                }
                if (id.charAt(1) == '2') {
                  int i = 0;
                  for (i =0; i < width*height;i++) {
                    dim = new String();
                    do {
                      buffer = (char)isr.read();
                      if (buffer != ' ' && buffer != '\n')
                        dim = dim +  buffer;
                    } while (buffer != ' ' && buffer != '\n');
                    bytes[i] = (byte)(Integer.parseInt(dim) & 0xFF);

                  }
                }
                // Convert to double.
                for (int i=0; i < height*width;i++)
                    doubles[i] = (double)(bytes[i] & 0xFF);

                isr.close();
            }
            else {
                width = height = 0;
                doubles = new double[0]; bytes= new byte[0];
                throw new NumberFormatException("Wrong header information!");
            }
    }
}
