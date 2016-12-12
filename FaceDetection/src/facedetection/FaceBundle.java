package facedetection;
import java.io.*;
public class FaceBundle implements Serializable, Comparable {

  private double[] avgFace = null;
  private double[] cmpFace = null;
  private double[][] eigVector = null;
  private double[][] wk = null;
  private String[] id   = null;
  private transient double minD   = Double.MAX_VALUE;
  public int length    = Integer.MIN_VALUE;
  private transient boolean computed = false;
  private transient int idx       = Integer.MAX_VALUE;
  public FaceBundle(double[] avgF, double wk[][], double[][] eigV, String[] files) {

    avgFace = new double[avgF.length];
    this.wk = new double[wk.length][wk[0].length];
    eigVector = new double[eigV.length][eigV[0].length];
    //this.id = new String[files.length];
    length = avgFace.length;

    System.arraycopy(avgF,0,this.avgFace,0,avgFace.length);
    System.arraycopy(eigV,0,this.eigVector,0,eigVector.length);
    System.arraycopy(wk,  0,this.wk, 0, wk.length);
    //System.arraycopy(files,  0,this.id, 0, id.length);

    this.id = files;
  }

  public void submitFace(byte[] face) {
    // Convert it to double.
    for (int i = 0; i< length;i++)
      cmpFace[i] = (double) (face[i] & 0xFF);

    compute();
  }

  public void submitFace(int[] face) {

    for (int i = 0; i< length;i++)
      cmpFace[i] = face[i];

    compute();
  }

  public void submitFace(double[] face) {

      this.cmpFace = face;
      compute();


  }
  public void clearFace() {

    cmpFace = null;
    computed = false;
    idx = Integer.MAX_VALUE;
    minD = Double.MAX_VALUE;
  }
  public double distance() {

    return minD;
  }

  public String getID() {

    return this.id[idx];
  }

  public String[] getNames() {
    return id;
  }

  public int compareTo(Object o) {


    if (((FaceBundle)o).minD > minD)
      return 1;
    if (((FaceBundle)o).minD < minD)
      return -1;

    return 0;
  }
  public String toString() {

    if (computed)
      return "["+id[idx] + "] with "+minD;
    return "No image supplied";
  }
  private void compute() {
    double[] inputFace = new double[length];
    int nrfaces = eigVector.length;
    int MAGIC_NR = wk[0].length;
    int j, pix, image;

    computed = false;
    System.arraycopy(cmpFace,0,inputFace,0,length);

    for ( pix = 0; pix < inputFace.length; pix++) {
        inputFace[pix] = inputFace[pix] - avgFace[pix];
    }

    //System.out.println(nrfaces+" "+inputFace.length);
    double[] input_wk = new double[MAGIC_NR];
    double temp = 0;
    /* Subtract the image from the average image */
    for (j = 0; j < MAGIC_NR; j++) {
      temp = 0.0;
      for ( pix=0; pix <length; pix++)
        temp += eigVector[j][pix] * inputFace[pix];

      input_wk[j] = Math.abs( temp );
    }

    double[] distance = new double[MAGIC_NR];
    double[] minDistance = new double[MAGIC_NR];
    idx = 0;
    for (image = 0; image < nrfaces; image++) {
        temp = 0.0;
        for (j = 0; j < MAGIC_NR; j++) {
          distance[j] = Math.abs(input_wk[j] - wk[image][j]);
          //System.out.print(distance[j]+"\t");
        }
        //System.out.println();
        if (image == 0)
          System.arraycopy(distance,0,minDistance,0,MAGIC_NR);
        if (sum(minDistance) > sum(distance)) {

          this.idx = image;
          System.arraycopy(distance,0,minDistance,0,MAGIC_NR);
        }
    }

    if (max(minDistance) > 0.0)
      divide(minDistance, max(minDistance));

    minD = sum(minDistance);

    computed = true;


  }
    static void divide(double[] v, double b) {

    for (int i = 0; i< v.length; i++)
      v[i] = v[i] / b;


  }
  static double sum(double[] a) {

    double b = a[0];
    for (int i = 0; i < a.length; i++)
      b += a[i];

    return b;

  }
  static double max(double[] a) {
    double b = a[0];
    for (int i = 0; i < a.length; i++)
      if (a[i] > b) b = a[i];

    return b;
  }

}


