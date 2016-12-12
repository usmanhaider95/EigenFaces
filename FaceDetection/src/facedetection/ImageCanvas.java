package facedetection;
import java.awt.*;
import java.awt.image.*;
class ImageCanvas extends Canvas  {


    private Image memImage=null;      // image constructed from PPM data
    public void readImage(byte[] bytes, int width, int height) {

	int pix[] = new int[width * height];
	int index = 0;
        int ofs = 0;

        for (index = 0; index < pix.length-2; index++) {
          pix[index] = 255 << 24 /*alpha*/ |
                    (int)(bytes[ofs] & 0xFF) << 16 /*R*/ |
                    (int)(bytes[ofs] & 0xFF) << 8 /*G*/ |
                    (int)(bytes[ofs] & 0xFF) /*B*/;
          ofs += 1;
        }

        memImage = createImage(new MemoryImageSource(width, height, pix, 0, width));
	repaint();
    }

    public void readImage(double[] doubles,  int width, int height) {
        int w = width;
	int h = height;

	int pix[] = new int[w * h];
	int index = 0;
        int avg = 0;
        for (index = 0; index < pix.length-2; index++) {
          //avg = (int) ((doubles[index] + doubles[index+1] + doubles[index+2]) / 3);
          avg = (int)doubles[index];
          pix[index] = 255 << 24 |/* avg << 16 | avg << 8 |*/ avg;


        }

        memImage = createImage(new MemoryImageSource(width, height, pix, 0, width));
	repaint();
    }

    public void readImage(int[] ints, int width, int height) {

        memImage = createImage(new MemoryImageSource(width, height, ints, 0, width));
	repaint();
    }

    public void paint(Graphics g) {
	Dimension d = getSize();      // get size of drawing area
	g.setColor(getBackground());  // clear drawing area
	g.fillRect(0, 0, d.width, d.height);
	g.setColor(getForeground());

	if (memImage != null) {
	    g.drawImage(memImage, 0, 0, this);
	}
    }
}
