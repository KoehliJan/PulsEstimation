package bfh.pulsestimation;


import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;

import Jama.Matrix;

public class PulseEstimationClass {

    MainActivity activity;

    private PeakDetection peakDetection;
    private RRProcessing rrProcessing;


    PulseEstimationClass(MainActivity a, int l_sig, int fa){

        activity = a;

        peakDetection = new PeakDetection(a,l_sig,fa);
        rrProcessing = new RRProcessing(l_sig);

        /* Setup Chart settings */
        String[] chartType = new String[1];
        chartType[0] = LineChart.TYPE;
        PointStyle[] pointStyle = new PointStyle[1];
        pointStyle[0] = PointStyle.POINT;
    }

    public void process(Matrix x){
        peakDetection.process( x);
        rrProcessing.process(peakDetection.getR_Peaks(), peakDetection.getAnzPeaks());

        /* Plot the heart rate */
        activity.getHeartRatePlotter().update();

    }

    public XYSample[] getPeaks(){
        return rrProcessing.getPeaks();
    }

    public XYSample[] getHeartRate(){
        return rrProcessing.getHeartRate();
    }


    public void reset(){
        peakDetection.reset();
        rrProcessing.reset();
    }



}
