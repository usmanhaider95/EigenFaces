package facedetection;
import facedetection.EigenFaceCreator;
import java.lang.*;
import java.io.*;



public class TestFaceRecognition  {

    public static void main(String args[]) {

        if (args.length != 2) {
            prUsage();
            System.exit(0);
        }

        String dir = args[0];
        String file = args[1];

      try {
        EigenFaceCreator creator = new EigenFaceCreator();

        //creator.USE_CACHE = -1;
        System.out.println("Constructing face-spaces from "+dir+" ...");
        creator.readFaceBundles(dir);

        System.out.println("Comparing "+file+" ...");
        String result = creator.checkAgainst(file);

        System.out.println("Most closly reseambling: "+result+" with "+creator.DISTANCE+" distance.");

      } catch (Exception e) { e.printStackTrace(); }
    }

    static void prUsage() {
        System.err.println("Usage: java TestFaceRecognition <directory of training images> <image to test against>");
    }


}

