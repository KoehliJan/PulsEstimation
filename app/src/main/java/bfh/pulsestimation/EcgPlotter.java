package bfh.pulsestimation;

import android.util.Log;

import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.ScatterChart;

import java.util.Timer;
import java.util.TimerTask;

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
        ChannelProperties chProps = new ChannelProperties(2);

            // Chart Types
        chProps.setChartType(LineChart.TYPE,0);     // Line Chart for Ecg
        chProps.setChartType(ScatterChart.TYPE,1);  // Scatter Chart for Peaks

            // Point Styles
        chProps.setPointStyle(PointStyle.POINT, 0);    // Points for Ecg
        chProps.setPointStyle(PointStyle.CIRCLE, 1);   // Circles for Peaks

            // Set theme related color for peaks.
        chProps.setColor(activity.getColor(R.color.colorAccent), 1);

            // Set Titles ( Legend )
        chProps.setTitle("Ekg",0);
        chProps.setTitle("R Peaks",1);


        /* Create Data Chart Object for each channel of the ecg signal*/
        chProps.setColor(activity.getColor(R.color.ch1color), 0);
        ecgChannel_1_Chart = new RealtimeDataChart(a, "", chProps, R.id.layout_ecg_ch1_plot, 3*256);
        chProps.setColor(activity.getColor(R.color.ch2color), 0);
        ecgChannel_2_Chart = new RealtimeDataChart(a, "", chProps, R.id.layout_ecg_ch2_plot, 3*256);
        chProps.setColor(activity.getColor(R.color.ch3color), 0);
        ecgChannel_3_Chart = new RealtimeDataChart(a, "", chProps, R.id.layout_ecg_ch3_plot, 3*256);

        /* Initialize each ecg chart */
        initEcgChart(ecgChannel_1_Chart,1);
        initEcgChart(ecgChannel_2_Chart,2);
        initEcgChart(ecgChannel_3_Chart,3);

        /* Create timer for updating plots */
        updateTimer = new Timer();

    }

    private void initEcgChart(RealtimeDataChart chart, int ch_numb){
        chart.getRenderer().setXLabels(0);
        chart.getRenderer().setXTitle("t");
        chart.getRenderer().setYTitle("Lead " + ch_numb);
    }


    public void reset(){
        synchronized (this){
            ecgChannel_1_Chart.reset();
            ecgChannel_2_Chart.reset();
            ecgChannel_3_Chart.reset();
        }
    }

    public void addData(Segment s, XYSample[] peaksCh1,XYSample[] peaksCh2,XYSample[] peaksCh3){
        synchronized (this){

            /* Add Ecg Signal data to Plot */
            ecgChannel_1_Chart.addSegmentToBuffer(0,s.getSamples(0));
            ecgChannel_2_Chart.addSegmentToBuffer(0,s.getSamples(1));
            ecgChannel_3_Chart.addSegmentToBuffer(0,s.getSamples(2));

            /* Add Peaks data to plot */
            ecgChannel_1_Chart.setBuffer(1, peaksCh1);
            ecgChannel_2_Chart.setBuffer(1, peaksCh2);
            ecgChannel_3_Chart.setBuffer(1, peaksCh3);

            Log.v("EcPlotter", "addData()");
        }
    }


    private void updatePlots(int csc){
        // Adjust the xRange of the axes to the data delivered
        ecgChannel_1_Chart.syncSerieMaster(0, csc);
        ecgChannel_2_Chart.syncSerieMaster(0, csc);
        ecgChannel_3_Chart.syncSerieMaster(0, csc);

        // Adding Samples within the xRange of the axes.
        ecgChannel_1_Chart.syncSerieSlave(1,0);
        ecgChannel_2_Chart.syncSerieSlave(1,0);
        ecgChannel_3_Chart.syncSerieSlave(1,0);

        // Reprint the plots
        ecgChannel_1_Chart.plot();
        ecgChannel_2_Chart.plot();
        ecgChannel_3_Chart.plot();
    }

    public void run(){
        /* Be sure scan is stopped */
        stop();

        /* Define Timer Task*/
        updatePlot = new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        /* Get current samples count */
                        int csc = activity.getDataScan().getCurrentSampleCount();

                        synchronized (activity.getEcgPlotter()){

                            /* Update Plots */
                            updatePlots(csc);


                        }
                    }
                });
            }
        };

        /* Scheduling Timer Task */
        updateTimer.schedule(updatePlot, updateRateMs, updateRateMs);
    }

    public void stop(){
        if(updatePlot != null){
            updatePlot.cancel();
        }
    }


}
