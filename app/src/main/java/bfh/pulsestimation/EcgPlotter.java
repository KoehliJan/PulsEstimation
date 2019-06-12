package bfh.pulsestimation;

import android.app.Activity;
import android.graphics.Color;

import org.achartengine.chart.LineChart;
import org.achartengine.chart.ScatterChart;
import org.achartengine.model.XYSeries;

import java.util.Timer;
import java.util.TimerTask;

import bfh.pulsestimation.R;
import bfh.pulsestimation.RealtimeDataChart;

public class EcgPlotter {

    RealtimeDataChart ecgChannel_1_Chart;
    RealtimeDataChart ecgChannel_2_Chart;
    RealtimeDataChart ecgChannel_3_Chart;


    private int updateRateMs = 150;



    Timer updateTimer;
    TimerTask updatePlot;


    MainActivity activity;

    EcgPlotter(MainActivity a){

        activity = a;

        /* Setup Chart properties */
        String[] chartTypes = new String[2];
        chartTypes[0] = LineChart.TYPE;     // Linechart for Ecg
        chartTypes[1] = ScatterChart.TYPE;     // Scatterchart for Peaks

        /* Create Data Chart Object for Ecg Signal*/
        ecgChannel_1_Chart = new RealtimeDataChart(a, "Channel 1", chartTypes, R.id.layout_ecg_ch1_plot, 3*256);
        ecgChannel_2_Chart = new RealtimeDataChart(a, "Channel 2", chartTypes, R.id.layout_ecg_ch2_plot, 3*256);
        ecgChannel_3_Chart = new RealtimeDataChart(a, "Channel 3", chartTypes, R.id.layout_ecg_ch3_plot, 3*256);

        initEcgChart(ecgChannel_1_Chart,1);
        initEcgChart(ecgChannel_2_Chart,2);
        initEcgChart(ecgChannel_3_Chart,3);

        updateTimer = new Timer();

    }

    private void initEcgChart(RealtimeDataChart chart, int ch_numb){
        chart.getRenderer().setXLabels(0);
        chart.getRenderer().setXTitle("Seconds");
        chart.getRenderer().setYTitle("Volts");

        chart.getRenderer().getSeriesRendererAt(1).setColor(activity.getColor(R.color.colorAccent));
    }


    public void reset(){
        ecgChannel_1_Chart.reset();
        ecgChannel_2_Chart.reset();
        ecgChannel_3_Chart.reset();
    }

    public void addData(Segment s, XYSample[] peaks){
        synchronized (this){

            /* Add Ecg Signal for the Plot */
            ecgChannel_1_Chart.addSegmentToBuffer(0,s.getSamples(0));
            ecgChannel_2_Chart.addSegmentToBuffer(0,s.getSamples(1));
            ecgChannel_3_Chart.addSegmentToBuffer(0,s.getSamples(2));

            /* add Peaks to plot */
            ecgChannel_1_Chart.setBuffer(1, peaks);




        }
    }



    public void run(){
        updatePlot = new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int csc = activity.getDataScan().getCurrentSampleCount();
                        synchronized (activity.getEcgPlotter()){

                            ecgChannel_1_Chart.syncSerieMaster(0, csc);
                            ecgChannel_2_Chart.syncSerieMaster(0, csc);
                            ecgChannel_3_Chart.syncSerieMaster(0, csc);

                            ecgChannel_1_Chart.syncSerieSlave(1,0);

                            ecgChannel_1_Chart.plot();
                            ecgChannel_2_Chart.plot();
                            ecgChannel_3_Chart.plot();
                        }

                    }
                });
            }
        };

        updateTimer.schedule(updatePlot, updateRateMs, updateRateMs);
        //softStop = false;
        //delayHandler.postDelayed(updatePlot, updateRateMs);
    }

    public void stop(){
        if(updatePlot != null){
            updatePlot.cancel();
        }
    }


}
