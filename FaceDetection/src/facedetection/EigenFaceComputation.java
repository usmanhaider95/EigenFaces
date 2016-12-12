package facedetection; 
import Jama.*;

public class EigenFaceComputation {

  
  private final static int MAGIC_NR = 11;


  public static FaceBundle submit(double[][] face_v, int width, int height, String[] id) {

    int length = width*height;
    int nrfaces = face_v.length;//No of faces which are in the directory from witch we read images
    int i, j, col,rows, pix, image;
    double temp = 0.0;
    double[][] faces = new double[nrfaces][length];

    
    ImageFileViewer simple = new ImageFileViewer();
    simple.setImage(face_v[0],width,height);

    double[] avgF = new double[length];

    /*
     Compute average face of all of the faces. 1xN^2
     */
    for ( pix = 0; pix < length; pix++) {
      temp = 0;
      for ( image = 0; image < nrfaces; image++) {
        temp +=  face_v[image][pix];
      }
      avgF[pix] = temp / nrfaces;
    }

    simple.setImage(avgF, width,height);

    /*
     Compute difference.
    */

    for ( image = 0; image < nrfaces; image++) {

      for ( pix = 0; pix < length; pix++) {
        face_v[image][pix] = face_v[image][pix] - avgF[pix];
      }
    }
    /* Copy our face vector (MxN^2). We will use it later */

    //for (image = 0; image < nrfaces; image++)
    //  System.arraycopy(face_v[image],0,faces[image],0,length);
    System.arraycopy(face_v,0,faces,0,face_v.length);

    simple.setImage(face_v[0],width,height);

    /*
     Build covariance matrix. MxM
    */

    Matrix faceM = new Matrix(face_v, nrfaces,length);
    Matrix faceM_transpose = faceM.transpose();

    /*
     Covariance matrix - its MxM (nrfaces x nrfaces)
     */
    Matrix covarM = faceM.times(faceM_transpose);

    double[][] z = covarM.getArray();
    System.out.println("Covariance matrix is "+z.length+" x "+z[0].length);

    /*
     Compute eigenvalues and eigenvector. Both are MxM
    */
    EigenvalueDecomposition E = covarM.eig();

    double[] eigValue = diag(E.getD().getArray());
    double[][] eigVector = E.getV().getArray();

    int[] index = new int[nrfaces];
    double[][] tempVector = new double[nrfaces][nrfaces];  /* Temporary new eigVector */

    for ( i = 0; i <nrfaces; i++) /* Enumerate all the entries */
      index[i] = i;

    doubleQuickSort(eigValue, index,0,nrfaces-1);

    // Put the index in inverse
    int[] tempV = new int[nrfaces];
    for ( j = 0; j < nrfaces; j++)
      tempV[nrfaces-1-j] = index[j];
    
    index = tempV;

    for ( col = nrfaces-1; col >= 0; col --) {
      for ( rows = 0; rows < nrfaces; rows++ ){
        tempVector[rows][col] = eigVector[rows][index[col]];
      }
    }
    eigVector = tempVector;
    tempVector = null;
    eigValue = null;
     Matrix eigVectorM = new Matrix(eigVector, nrfaces,nrfaces);
     eigVector = eigVectorM.times(faceM).getArray();


     /* Normalize our eigen vector matrix.  */

     for ( image = 0; image < nrfaces; image++) {
      temp = max(eigVector[image]); // Our max
      for ( pix = 0; pix < eigVector[0].length; pix++)
       // Normalize
        eigVector[image][pix] = Math.abs( eigVector[image][pix] / temp);
    }

    double[][] wk = new double[nrfaces][MAGIC_NR]; // M rows, 11 columns


    for (image = 0; image < nrfaces; image++) {
      for (j  = 0; j <  MAGIC_NR; j++) {
        temp = 0.0;
        for ( pix=0; pix< length; pix++)
          temp += eigVector[j][pix] * faces[image][pix];
        wk[image][j] = Math.abs( temp );
      }
    }

    FaceBundle b = new FaceBundle(avgF, wk, eigVector ,id);
 return b;
  }

  /**
   * Find the diagonal of an matrix.
   *
   * @param m the number of rows and columns must be the same
   * @return  the diagonal of the matrix
   */
  static double[] diag(double[][] m) {

    double[] d = new double[m.length];
    for (int i = 0; i< m.length; i++)
      d[i] = m[i][i];
    return d;
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
    static void doubleQuickSort(double a[], int index[], int lo0, int hi0) {
        int lo = lo0;
        int hi = hi0;
        double mid;

        if ( hi0 > lo0) {

            /* Arbitrarily establishing partition element as the midpoint of
             * the array.
             */
            mid = a[ ( lo0 + hi0 ) / 2 ];
            // loop through the array until indices cross
            while( lo <= hi ) {
                /* find the first element that is greater than or equal to
                 * the partition element starting from the left Index.
                 */
                while( ( lo < hi0 ) && ( a[lo] < mid )) {
                    ++lo;
                }

                /* find an element that is smaller than or equal to
                 * the partition element starting from the right Index.
                 */
                while( ( hi > lo0 ) && ( a[hi] > mid )) {
                    --hi;
                }

                // if the indexes have not crossed, swap
                if( lo <= hi ) {
                    swap(a, index, lo, hi);
                    ++lo;
                    --hi;
                }
            }
            if( lo0 < hi ) {
                doubleQuickSort( a, index, lo0, hi );
            }
            if( lo < hi0 ) {
                doubleQuickSort( a, index,lo, hi0 );
            }
        }
    }

    static private void swap(double a[], int[] index, int i, int j) {
        double T;
        T = a[i];
        a[i] = a[j];
        a[j] = T;
        // Index
        index[i] = i;
        index[j] = j;
    }
}
