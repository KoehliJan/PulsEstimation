package bfh.pulsestimation;

import android.util.Log;
import android.view.View;

import org.achartengine.chart.PointStyle;
import org.achartengine.tools.PanListener;

import java.awt.font.NumericShaper;
import java.text.DecimalFormat;

public class UpdatingPanChart extends SimpleLineChart {

    double xLabelsDist = 5 * 256;
    double xLabelCounter = 0;
    double xRange;
    double xMax, yMax, yMin;

    private boolean updateRange = true;


    UpdatingPanChart(MainActivity a, ChannelProperties chProps, String t, int layout_id, double xRange) {
        super(a, chProps, t, layout_id);
        this.xRange = xRange;
        reset();

        mChartView.addPanListener(new PanListener() {
            @Override
            public void panApplied() {
                updateRange = false;
            }
        });

    }

    public  void reset(){
        xMax = 0;
        xLabelCounter = 0;
        setRanges(0,xRange,60,120);
        renderer.clearXTextLabels();
    }

    public void updateChannelData(final XYSample[] samples, final int channel){

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDataset.getSeriesAt(channel).clear();
                for(int i = 0; i < samples.length; i++){
                    mDataset.getSeriesAt(channel).add(samples[i].getX(),samples[i].getY());
                }
            }
        });



    }

    public void updateXaxis( ){

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {



                if (renderer.getXAxisMax() >= xMax){
                    updateRange = true;
                }
                Log.v("Updating chart","update, update range: "+ updateRange);
                Log.v("Updating chart","axis max: "+ renderer.getXAxisMax() + "xMax" + xMax);

                /* Find Max and Min over all series */
                for(int i = 0; i < mDataset.getSeriesCount(); i++){

                    double temp_xMax = mDataset.getSeriesAt(i).getMaxX();


                    if(temp_xMax > xMax){
                        xMax = temp_xMax;
                    }
                }

                /* Adjust xMax */
                if(xMax < xRange){
                    xMax = xRange;
                }



                /* Set Range*/
                if (updateRange){
                    if (renderer.getXAxisMax() < xMax){
                        renderer.setXAxisMax(xMax);
                        renderer.setXAxisMin(xMax-xRange);
                    }
                }
                if (renderer.getXAxisMin() >= xMax){
                    renderer.setXAxisMax(xMax - xRange);
                    renderer.setXAxisMin(xMax - 2*xRange);
                }

                /* Labels */
                for(double labelPos = xLabelCounter; labelPos < xMax; labelPos = labelPos + xLabelsDist){
                    int min = (int) (labelPos/256) / 60;
                    double s =  (labelPos/256) % 60;
                    DecimalFormat formatter = new DecimalFormat("00.00");
                    renderer.addXTextLabel(labelPos , "" + min + ":" + formatter.format(s));
                }
            }
        });
    }
}
