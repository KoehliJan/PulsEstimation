package bfh.pulsestimation;

import android.util.Log;

import org.achartengine.model.XYSeries;

import java.util.ArrayList;
import java.util.List;


public class RealtimeDataChart extends SimpleLineChart {


    private int length_plot;
    private int plotted_samples;

    private ArrayList<XYSample>[] Buffer;

    private int plotOffset;


    RealtimeDataChart(MainActivity a, String t, String[] chartTypes,  int layout_id, int l_plot){
        super(a, chartTypes, t, layout_id);
        fixXaxis();

        /* Setup Buffer */
        Buffer = new ArrayList[n_series];
        for (int i = 0; i < n_series; i++){
            Buffer[i] = new ArrayList<XYSample>();
        }

        length_plot = l_plot;
        plotted_samples = 0;

        initDataset();

        setPlotOffset(3000,256);

    }

    private void initDataset(){
        XYSeries[] series = mDataset.getSeries();

        for (XYSeries s: series
        ) {
            s.clear();
            for (int i = 0; i<length_plot;i++ ){
                s.add(0,0);
            }
        }

        plot();
    }

    public void addSegmentToBuffer(int serie, XYSample[] seg){

        for (int i = 0; i < seg.length; i++ ){
            Buffer[serie].add(seg[i]);
        }
        Log.v("Realtime Data Chart","Segment Ready to Plot at Time: " + System.currentTimeMillis() +" ms");
    }

    public void setBuffer(int serie, XYSample[] seg){

        Buffer[serie].clear();
        for (int i = 0; i < seg.length; i++ ){
            Buffer[serie].add(seg[i]);
            Log.v("Realtime data chart", "Peak at " + seg[i].getX());
        }

        Log.v("Realtime Data Chart","Segment Ready to Plot at Time: " + System.currentTimeMillis() +" ms");
    }


    public void syncSerieMaster(int serie, int sampleCount){

        //Log.v("Realtime Data Chart","syncPlot");
        int plotSample = sampleCount - plotOffset;

        for(int i = plotted_samples ; i < plotSample; i++){

            //Log.v("Realtime Data Chart","lineBuffer length "+lineBuffer.size()+" plotted samples: " + plotted_samples);
            if (!Buffer[serie].isEmpty()){
                if (Buffer[serie].get(0) != null){

                    /* Remove first sample and add the new Sample from the Buffer*/
                    if(mDataset.getSeriesAt(serie).getItemCount() > length_plot){
                        mDataset.getSeriesAt(serie).remove(0);
                    }
                    mDataset.getSeriesAt(serie).add(Buffer[serie].get(0).getX(), Buffer[serie].get(0).getY());

                    /* Remove Sample from waiting Buffer*/
                    Buffer[serie].remove(serie);
                    plotted_samples++;

                }else {
                    Log.e("Realtime Datachart","lineBuffer error");
                }
            }

            /* Update Range */
            this.setAutoRangeX(0, length_plot );

        }
    }

    public void syncSerieSlave(int serie, int masterserie){

        /* Clear Serie */
        mDataset.getSeriesAt(serie).clear();

        if (mDataset.getSeriesAt(masterserie).getItemCount() != 0){

            double xRangeStart = mDataset.getSeriesAt(masterserie).getX(0);
            double xRangeEnd = mDataset.getSeriesAt(masterserie).getX(mDataset.getSeriesAt(masterserie).getItemCount()-1);

            /* Add Samples in Range to Dataset */
            for (int i = 0; i < Buffer[serie].size(); i++){

                if ( Buffer[serie].get(i).getX() > xRangeStart &&  Buffer[serie].get(i).getX() < xRangeEnd ){
                    mDataset.getSeriesAt(serie).add(Buffer[serie].get(i).getX(),Buffer[serie].get(i).getY());
                }
            }
        }

    }



    /* Set the Plotoffset in Samples */
    public void setPlotOffset(int pO){
        plotOffset = pO;
    }

    /* Set the PlotOffset according to Max Calculation duration, and sampling rate*/
    public void setPlotOffset(int calculationMs, double fa){
        plotOffset = (int)(calculationMs * fa / 1000 );
    }

    public void reset(){
        plotted_samples = 0;
        for (int i = 0; i < Buffer.length; i++){
            Buffer[i].clear();
        }
        clearChart();
    }


}
