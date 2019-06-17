package bfh.pulsestimation;

import android.graphics.Paint;

import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;

public class HeartRatePlotter {

    UpdatingPanChart heartRateChart;
    MainActivity activity;



    HeartRatePlotter(MainActivity a ){
        activity = a;

        /* Setup Chart properties */
        ChannelProperties chProps = new ChannelProperties(3);

            // Set all Chart Types to Scatter
        chProps.setChartTypes(LineChart.TYPE);

            // Set different Point Style for each channel.
        chProps.setPointSytyle(PointStyle.DIAMOND, 0);
        chProps.setPointSytyle(PointStyle.SQUARE, 1);
        chProps.setPointSytyle(PointStyle.TRIANGLE, 2);

            // Set different Color for each channel.
        chProps.setColor(activity.getColor(R.color.ch1color), 0);
        chProps.setColor(activity.getColor(R.color.ch2color), 1);
        chProps.setColor(activity.getColor(R.color.ch3color), 2);

            // Set titles of the channel. Leading and following spaces for better visual appearance.
        chProps.setTitle("  Lead 1 ",0);
        chProps.setTitle("  Lead 2 ",1);
        chProps.setTitle("  Lead 3 ",2);

        heartRateChart = new UpdatingPanChart(a, chProps, "", R.id.layout_heartrateplot, 20 * 256);

        /* Customize Axis Titles */
        heartRateChart.getRenderer().setChartTitleTextSize(30);
        heartRateChart.getRenderer().setXTitle("t (min:s)");
        heartRateChart.getRenderer().setYTitle("bpm");

        /* Customize Legend */
        heartRateChart.getRenderer().setShowLegend(true);
        heartRateChart.getRenderer().setFitLegend(true);
        heartRateChart.getRenderer().setXLabels(0);
        heartRateChart.getRenderer().setXLabelsAlign(Paint.Align.CENTER);

        /* Set margins */
        int[] margins = {60,100,50,20};
        heartRateChart.getRenderer().setMargins(margins);

        /**/


    }

    public void update(){

        heartRateChart.updateChannelData(activity.getPulseEstimationCh1().getHeartRate(), 0);
        heartRateChart.updateChannelData(activity.getPulseEstimationCh2().getHeartRate(), 1);
        heartRateChart.updateChannelData(activity.getPulseEstimationCh3().getHeartRate(), 2);

        heartRateChart.updateXaxis();

        heartRateChart.plot();
    }

    public void reset(){
        heartRateChart.reset();
    }

}
