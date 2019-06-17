package bfh.pulsestimation;

import android.util.Log;

import org.achartengine.model.XYSeries;

public class SampleCounter {

    int count;

    SampleCounter(){
        reset();
    }

    public void reset(){
        count = 0;
    }

    public Segment stampSegment(Jama.Matrix X){

        int length = X.getRowDimension();

        int[] c = new int[length];

        for (int i = 0; i < length; i++){
            c[i] = count + i;
        }

        count = count + length;

        Log.v("Sample Counter","count: "+count);


        return new Segment(X,c);

    }
}
