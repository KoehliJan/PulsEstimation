package bfh.pulsestimation;


import android.util.Log;

public class ChannelNormalizer {

    private int nChannels;
    private double[] normalizingFactor;

    /*Indicator for first segment */
    private boolean isFirstSegment = true;

    ChannelNormalizer(int nCh){
        reset();
        nChannels = nCh;
        normalizingFactor = new double[nChannels];
    }

    public void reset(){
        /* Set indicator for first segment */
        isFirstSegment = true;
    }


    public Jama.Matrix normalize(Jama.Matrix X){

        /* Check dimensions */
        if (X.getColumnDimension() == nChannels){

            for(int channel = 0; channel < nChannels; channel++){

                /* Calculate factors in the first segment */
                if(isFirstSegment){
                    normalizingFactor[channel] = 1/max(X,channel)[0][0];//1/mean(X.transpose().getArray()[channel]);
                    Log.v("Channel Normalizer","f: " + normalizingFactor[channel]);
                }

                /* Normalize Channel */
                X.setMatrix(0,X.getRowDimension()-1,channel,channel,X.getMatrix(0,X.getRowDimension()-1, channel,channel).times(normalizingFactor[channel]));
            }
        }else{
            Log.e("Channel Normalizer", "Error occurred. Matrix dimensions must agree.");
        }

        /* Set indicator first segment processed */
        isFirstSegment = false;
        return X;
    }


    /* Absolute maximum in a Jama.Matrix column */
    private double[][] max (Jama.Matrix x, int column){
        double[][] Max= {{0,0}};

        for (int i=0 ; i<x.getRowDimension() ; i++) {
            if((Math.abs(Max[0][0]))<(Math.abs(x.get(i, column)))) {
                Max[0][0]=Math.abs(x.get(i, column));
                Max[0][1]=i;
            }
        }
        return Max;
    }

    // Funktion zum berechnen des Mittelwertes
    private double mean(double[] x) {

        Log.v("Channel Normalizer",""+x.length);
        double mean=0;
        for(int i=0 ; i<x.length ; i++) {
            mean = mean + x[i];
        }
        return mean/x.length;
    }
}
