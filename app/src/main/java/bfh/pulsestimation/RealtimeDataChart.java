package bfh.pulsestimation;

import android.util.Log;

import org.achartengine.model.XYSeries;

import java.util.ArrayList;


public class RealtimeDataChart extends MultiSerieChart {

    private int xLabelsDist = 256 / 2;
    private int length_plot;
    private int length_break;
    private int plotted_samples;

    private ArrayList<XYSample>[] Buffer;

    private int plotOffset;


    RealtimeDataChart(MainActivity a, String t, ChannelProperties chProps,  int layout_id, int l_plot){
        super(a, chProps, t, layout_id);

        disableInteractivity();

        /* Setup Buffer */
        Buffer = new ArrayList[n_series];
        for (int i = 0; i < n_series; i++){
            Buffer[i] = new ArrayList<XYSample>();
        }

        length_plot = l_plot;
        length_break = l_plot/4;
        plotted_samples = 0;

        initDataset();

        setPlotOffset(3000,256);

    }

    private void initDataset(){
        XYSeries[] series = mDataset.getSeries();

        for (XYSeries s: series
        ) {
            s.clear();
            s.add(0,0);
            //for (int i = 0; i < length_plot;i++ ){
            //    s.add(i,0);
            //}
        }

        //Log.v("initDataset","init");

    }

    public void addSegmentToBuffer(int serie, XYSample[] seg){

        for (int i = 0; i < seg.length; i++ ){
            Buffer[serie].add(seg[i]);
        }
        //Log.v("Realtime Data Chart","Segment Ready to Plot at Time: " + System.currentTimeMillis() +" ms");
    }

    public void setBuffer(int serie, XYSample[] seg){

        Buffer[serie].clear();
        for (int i = 0; i < seg.length; i++ ){
            Buffer[serie].add(seg[i]);
            //Log.v("Realtime data chart", "Peak at " + seg[i].getX());
        }

        //Log.v("Realtime Data Chart","Segment Ready to Plot at Time: " + System.currentTimeMillis() +" ms");
    }


    public void syncSerieMaster(int serie, int sampleCount){


        //Log.v("Realtime Data Chart","syncPlot");
        int plotSample = sampleCount - plotOffset;

        for(int i = plotted_samples; i < plotSample; i++){
            //Log.v("Realtime Data Chart","lineBuffer length "+lineBuffer.size()+" plotted samples: " + plotted_samples);
            if (!Buffer[serie].isEmpty()){
                if (Buffer[serie].get(0) != null){

                    /* Remove first sample and add the new Sample from the Buffer*/
                    if(mDataset.getSeriesAt(serie).getItemCount() > length_plot){
                        mDataset.getSeriesAt(serie).remove(0);
                    }

                    mDataset.getSeriesAt(serie).add(Buffer[serie].get(0).getX() % length_plot, Buffer[serie].get(0).getY());

                    /* Add break */
                    /* Remove first sample and add the new Sample from the Buffer*/
                    if(mDataset.getSeriesAt(serie).getItemCount() > length_plot){
                        mDataset.getSeriesAt(serie).remove(0);
                    }
                    mDataset.getSeriesAt(serie).add((Buffer[serie].get(0).getX() + length_break) % length_plot, 0);


                    /* Remove Sample from waiting Buffer*/
                    Buffer[serie].remove(0);

                    plotted_samples++;

                }else {
                    Log.e("Realtime Datachart","lineBuffer error");
                }
            }


        }

        /* Update Range */
        this.setAutoRangeX(0, length_plot );


        /* Update X Labels */
        renderer.clearXTextLabels();
        for(int i = plotSample - length_plot ; i < plotSample; i++){
            /* Add Label */
            if(i % xLabelsDist == xLabelsDist/2){
                renderer.addXTextLabel(i % length_plot , "" + (double)i / 256 + " s");
            }
        }


    }


    public void syncSerieSlave(int serie, int masterserie){

        /* Clear Serie */
        mDataset.getSeriesAt(serie).clear();

        if (mDataset.getSeriesAt(masterserie).getItemCount() != 0){


            int xRangeStart = plotted_samples - length_plot + length_break;
            if (xRangeStart < 0) xRangeStart = 0;
            int xRangeEnd = plotted_samples;
            if (xRangeEnd < 0) xRangeEnd = 0;

            /* Add Samples in Range to Dataset */
            for (int i = 0; i < Buffer[serie].size(); i++){

                if ( Buffer[serie].get(i).getX() > xRangeStart &&  Buffer[serie].get(i).getX() < xRangeEnd ){
                    mDataset.getSeriesAt(serie).add(Buffer[serie].get(i).getX() % length_plot, Buffer[serie].get(i).getY());
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

        initDataset();

    }



}
