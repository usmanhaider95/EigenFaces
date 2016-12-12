package facedetection;
import javax.media.*;
import javax.media.format.*;
import java.awt.*;
public class MotionDetectionEffect implements Effect {

    public int OPTIMIZATION = 0;
    public int THRESHOLD_MAX = 10000;
    public int THRESHOLD_INC = 1000;
    public int THRESHOLD_INIT = 5000;



    private Format inputFormat;
    private Format outputFormat;
    private Format[] inputFormats;
    private Format[] outputFormats;

    private byte[] refData;
    private byte[] bwData;

    private int avg_ref_intensity;
    private int avg_img_intensity;
    public int threshold = 30;
    public int blob_threshold = THRESHOLD_INIT;

    public boolean debug = false;
    public MotionDetectionEffect() {

        inputFormats = new Format[] {
            new RGBFormat(null,
                          Format.NOT_SPECIFIED,
                          Format.byteArray,
                          Format.NOT_SPECIFIED,
                          24,
                          3, 2, 1,
                          3, Format.NOT_SPECIFIED,
                          Format.TRUE,
                          Format.NOT_SPECIFIED)
        };

        outputFormats = new Format[] {
            new RGBFormat(null,
                          Format.NOT_SPECIFIED,
                          Format.byteArray,
                          Format.NOT_SPECIFIED,
                          24,
                          3, 2, 1,
                          3, Format.NOT_SPECIFIED,
                          Format.TRUE,
                          Format.NOT_SPECIFIED)
        };

    }
    public Format[] getSupportedInputFormats() {
	return inputFormats;
    }
    public Format [] getSupportedOutputFormats(Format input) {
        if (input == null) {
            return outputFormats;
        }

        if (matches(input, inputFormats) != null) {
            return new Format[] { outputFormats[0].intersects(input) };
        } else {
            return new Format[0];
        }
    }
    public Format setInputFormat(Format input) {

	inputFormat = input;
	return input;
    }
    public Format setOutputFormat(Format output) {

        if (output == null || matches(output, outputFormats) == null)
            return null;

        RGBFormat incoming = (RGBFormat) output;

        Dimension size = incoming.getSize();
        int maxDataLength = incoming.getMaxDataLength();
        int lineStride = incoming.getLineStride();
        float frameRate = incoming.getFrameRate();
        int flipped = incoming.getFlipped();
        int endian = incoming.getEndian();

        if (size == null)
            return null;
        if (maxDataLength < size.width * size.height * 3)
            maxDataLength = size.width * size.height * 3;
        if (lineStride < size.width * 3)
            lineStride = size.width * 3;
        if (flipped != Format.FALSE)
            flipped = Format.FALSE;

        outputFormat = outputFormats[0].intersects(new RGBFormat(size,
                                                        maxDataLength,
                                                        null,
                                                        frameRate,
                                                        Format.NOT_SPECIFIED,
                                                        Format.NOT_SPECIFIED,
                                                        Format.NOT_SPECIFIED,
                                                        Format.NOT_SPECIFIED,
                                                        Format.NOT_SPECIFIED,
                                                        lineStride,
                                                        Format.NOT_SPECIFIED,
                                                        Format.NOT_SPECIFIED));

        return outputFormat;
    }

   /**
    */

    public int process(Buffer inBuffer, Buffer outBuffer) {
        int outputDataLength = ((VideoFormat)outputFormat).getMaxDataLength();

        validateByteArraySize(outBuffer, outputDataLength);

        outBuffer.setLength(outputDataLength);
        outBuffer.setFormat(outputFormat);
        outBuffer.setFlags(inBuffer.getFlags());

        byte [] inData = (byte[]) inBuffer.getData();
        byte [] outData = (byte[]) outBuffer.getData();

        RGBFormat vfIn = (RGBFormat) inBuffer.getFormat();
        Dimension sizeIn = vfIn.getSize();

        int pixStrideIn = vfIn.getPixelStride();
        int lineStrideIn = vfIn.getLineStride();

        int y, x;
        int width = sizeIn.width;
        int height = sizeIn.height;
        int r,g,b;
        int ip, op;
        byte result;
        int avg = 0;
        int refDataInt = 0;
        int inDataInt = 0;
        int correction;
        int blob_cnt = 0;


        if (refData == null) {
          refData = new byte[outputDataLength];
          bwData = new byte[outputDataLength];

          System.arraycopy (inData,0,refData,0,inData.length);
          System.arraycopy(inData,0,outData,0,inData.length);

      	  for (ip  = 0; ip < outputDataLength; ip++) {
        	avg += (int) (refData[ip] & 0xFF);
      	  }

      	  avg_ref_intensity =  avg / outputDataLength;
          return BUFFER_PROCESSED_OK;
        }

        if ( outData.length < sizeIn.width*sizeIn.height*3 ) {
            System.out.println("the buffer is not full");
            return BUFFER_PROCESSED_FAILED;
        }

        for (ip  = 0; ip < outputDataLength; ip++) {
          avg += (int) (inData[ip] & 0xFF);
        }

      avg_img_intensity = avg / outputDataLength;
      correction = (avg_ref_intensity < avg_img_intensity) ?
      avg_img_intensity - avg_ref_intensity :
      avg_ref_intensity - avg_img_intensity;
      avg_ref_intensity = avg_img_intensity;
      ip = op = 0;
      for (int ii=0; ii< outputDataLength/pixStrideIn; ii++) {

          refDataInt = (int) refData[ip] & 0xFF;
          inDataInt = (int) inData[ip++] & 0xFF;
          r =  (refDataInt > inDataInt) ? refDataInt - inDataInt : inDataInt - refDataInt;

          refDataInt = (int) refData[ip] & 0xFF;
          inDataInt = (int) inData[ip++] & 0xFF;
          g =  (refDataInt > inDataInt) ? refDataInt - inDataInt : inDataInt - refDataInt;

          refDataInt = (int) refData[ip] & 0xFF;
          inDataInt = (int) inData[ip++] & 0xFF;
          b =  (refDataInt > inDataInt) ? refDataInt - inDataInt : inDataInt - refDataInt;

          // intensity normalization
          r -= (r < correction) ? r : correction;
          g -= (g < correction) ? g : correction;
          b -= (b < correction) ? b : correction;

          result = (byte)(java.lang.Math.sqrt((double)( (r*r) + (g*g) + (b*b) ) / 3.0));
          /*
            black/white image now.
          */
	  if (result > (byte)threshold) {
             bwData[op++] = (byte)255;
             bwData[op++] = (byte)255;
             bwData[op++] = (byte)255;
       	  }  else {
	     bwData[op++] = (byte)result;
             bwData[op++] = (byte)result;
             bwData[op++] = (byte)result;
	   }
      }

      // blob elimination
      for (op = lineStrideIn + 3; op < outputDataLength - lineStrideIn-3; op+=3) {
        for (int i=0; i<1; i++) {
          if (((int)bwData[op+2] & 0xFF) < 255) break;
          if (((int)bwData[op+2-lineStrideIn] & 0xFF) < 255) break;
          if (((int)bwData[op+2+lineStrideIn] & 0xFF) < 255) break;
          if (((int)bwData[op+2-3] & 0xFF) < 255) break;
          if (((int)bwData[op+2+3] & 0xFF) < 255) break;
          if (((int)bwData[op+2-lineStrideIn + 3] & 0xFF) < 255) break;
          if (((int)bwData[op+2-lineStrideIn - 3] & 0xFF) < 255) break;
          if (((int)bwData[op+2+lineStrideIn - 3] & 0xFF) < 255) break;
          if (((int)bwData[op+2+lineStrideIn + 3] & 0xFF) < 255) break;
          bwData[op]  = (byte)0;
          bwData[op+1] = (byte)0;
          blob_cnt ++;
        }
      }


       // when we are finished with comparison we do this.
    if (blob_cnt > blob_threshold) {

	    if (debug) {
     		sample_down(inData,outData, 0, 0,sizeIn.width, sizeIn.height,
                  lineStrideIn, pixStrideIn);
     		sample_down(bwData,outData, sizeIn.width/2, 0,sizeIn.width,
                   sizeIn.height, lineStrideIn, pixStrideIn);
	    } else
      		System.arraycopy(inData,0,outData,0,inData.length);

       System.arraycopy(inData,0,refData,0,inData.length);
       return BUFFER_PROCESSED_OK;
    }
     return BUFFER_PROCESSED_FAILED;
    }

    public String getName() {
        return "Motion Detection Codec";
    }

    public void open() {
    }

    public void close() {
    }

    public void reset() {
    }

    public Object getControl(String controlType) {
        System.out.println(controlType);
	return null;
    }
    private Control[] controls;


    public Object[] getControls() {
      if (controls == null) {
        controls = new Control[1];
        controls[0] = new MotionDetectionControl(this);
      }
      return (Object[])controls;
    }


    // Utility methods.
    Format matches(Format in, Format outs[]) {
	for (int i = 0; i < outs.length; i++) {
	    if (in.matches(outs[i]))
		return outs[i];
	}

	return null;
    }


    void sample_down(byte[] inData, byte[] outData, int X, int Y, int width,
    int height, int lineStrideIn, int pixStrideIn) {

          int p1, p2, p3, p4, op,x,y;

      for ( y = 0; y < (height/2); y++) {

        p1 = (y * 2) * lineStrideIn ; // upper left cell
        p2 = p1 + pixStrideIn;                    // upper right cell
        p3 = p1 + lineStrideIn;         // lower left cell
        p4 = p3 + pixStrideIn;                    // lower right cell
        op = lineStrideIn * y + (lineStrideIn*Y) + (X*pixStrideIn);
        for ( int i =0; i< (width /2 );i++) {
          outData[op++] = (byte)(((int)(inData[p1++] & 0xFF) +
            ((int)inData[p2++] & 0xFF)+ ((int)inData[p3++] & 0xFF) +
            ((int)inData[p4++] & 0xFF))/4); // blue cells avg
          outData[op++] = (byte)(((int)(inData[p1++] & 0xFF) +
            ((int)inData[p2++] & 0xFF)+ ((int)inData[p3++] & 0xFF) +
              ((int)inData[p4++] & 0xFF))/4); // blue cells avg
          outData[op++] = (byte)(((int)(inData[p1++] & 0xFF) +
            ((int)inData[p2++] & 0xFF)+ ((int)inData[p3++] & 0xFF) +
              ((int)inData[p4++] & 0xFF))/4); // blue cells avg
          p1 += 3; p2 += 3; p3+= 3; p4 += 3;
        }

      }
    }

    byte[] validateByteArraySize(Buffer buffer,int newSize) {
        Object objectArray=buffer.getData();
        byte[] typedArray;

        if (objectArray instanceof byte[]) {     // is correct type AND not null
            typedArray=(byte[])objectArray;
            if (typedArray.length >= newSize ) { // is sufficient capacity
                return typedArray;
            }

            byte[] tempArray=new byte[newSize];  // re-alloc array
            System.arraycopy(typedArray,0,tempArray,0,typedArray.length);
            typedArray = tempArray;
        } else {
            typedArray = new byte[newSize];
        }

        buffer.setData(typedArray);
        return typedArray;
    }

}
