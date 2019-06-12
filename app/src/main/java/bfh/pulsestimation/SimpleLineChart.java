package bfh.pulsestimation;

import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.widget.LinearLayout;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.ScatterChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;


public class SimpleLineChart {

    private LinearLayout chartLayout;
    protected GraphicalView mChartView;


    private XYMultipleSeriesRenderer renderer;

    protected XYMultipleSeriesDataset mDataset;
    private String title;


    protected MainActivity activity;

    /* Channel Properties */
    protected int n_series;
    private String[] ch_titles;
    private int[] ch_colors;
    private PointStyle[]  ch_styles;
    String[] types;


    SimpleLineChart(MainActivity a, String[] chartTypes, String t,  int layout_id){

        activity = a;
        title = t;
        n_series = chartTypes.length;
        types = chartTypes;

        initSeriesProperties();
        buildRenderer();
        setChartSettings();
        setRanges(0,5,0,2);
        initDataSet();
        setupChart(layout_id);
        plot();

    }




    protected void plot(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mChartView.repaint();
            }
        });
    }

    public void clearChart(){
        /* Clear Series */
        for (int i = 0; i < mDataset.getSeriesCount(); i++){
            mDataset.getSeriesAt(i).clear();
        }
        plot();
    }



    protected void setDataToPlot(int serie, double[] DataValues){

        /* Clear the Data Points in this Serie */
        mDataset.getSeriesAt(serie).clear();

        /* Add the new Dataplot in this Serie */
        for (int i = 0; i < DataValues.length; i++)
            mDataset.getSeriesAt(serie).add( i, DataValues[i]);

    }



    private void initSeriesProperties(){

        ch_colors = new int[n_series];
        ch_titles = new String[n_series];
        ch_styles = new PointStyle[n_series];


        for(int i =0; i < ch_colors.length; i++){
            ch_titles[i] = "Channel ";
            ch_colors[i] = getColor(i);
            ch_styles[i] = PointStyle.POINT;

        }

        ch_styles[1] = PointStyle.CIRCLE;

    }

    private void buildRenderer(){

        renderer = new XYMultipleSeriesRenderer();

        /* Set View related Params*/
        renderer.setChartTitleTextSize(30);
        renderer.setLabelsTextSize(30);
        renderer.setLegendTextSize(25);
        renderer.setAxisTitleTextSize(40);
        renderer.setPointSize(2 * 5f);


        for (int i = 0; i < n_series; i++) {
            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setColor(ch_colors[i]);
            r.setPointStyle(ch_styles[i]);
            renderer.addSeriesRenderer(r);
        }

    }


    private void setChartSettings(){

        renderer.setChartTitle(title);
        renderer.setXTitle("");
        renderer.setYTitle("");


        renderer.setAxesColor(Color.WHITE);
        renderer.setLabelsColor(Color.WHITE);

        renderer.setYLabelsAlign(Paint.Align.RIGHT);

        int[] margins = {60,100,20,20};
        renderer.setMargins(margins);
        renderer.setMarginsColor(Color.BLACK);

        renderer.setBackgroundColor(Color.BLACK);

        renderer.setShowGrid(true);
        renderer.setShowLegend(false);
        renderer.setXLabelsColor(Color.WHITE);
        renderer.setYLabelsColor(0, Color.WHITE);
        renderer.setXLabels(11);
        renderer.setYLabels(11);
    }

    public XYMultipleSeriesRenderer getRenderer() {
        return renderer;
    }

    public void setRanges(double xMin, double xMax, double yMin, double yMax){
        renderer.setXAxisMin(xMin);
        renderer.setXAxisMax(xMax);
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);
    }

    protected void setAutoRangeX(final int ch_number, final double sizeX){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                double yPadding = 0.1 *( mDataset.getSeriesAt(ch_number).getMaxY() - mDataset.getSeriesAt(ch_number).getMinY());
                if(yPadding < 0.01)yPadding = 0.01;
                /* Set Y Range*/
                renderer.setYAxisMax(mDataset.getSeriesAt(ch_number).getMaxY() + yPadding);
                renderer.setYAxisMin(mDataset.getSeriesAt(ch_number).getMinY() - yPadding);

                /* Set X Range */
                double minX = mDataset.getSeriesAt(ch_number).getMinX();
                renderer.setXAxisMin(minX);
                renderer.setXAxisMax(minX + sizeX);
            }
        });
    }

    protected void fixXaxis(){
        renderer.setPanEnabled(false,true);
        renderer.setZoomEnabled(false,true);
    }

    private void initDataSet(){

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDataset = new XYMultipleSeriesDataset();
                for (int ch=0; ch < n_series; ch++){
                    mDataset.addSeries(new XYSeries(ch_titles[ch]));
                    mDataset.getSeriesAt(ch).add(0,0);
                }
            }
        });


    }

    private void setupChart(final int layout_id){

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mChartView = ChartFactory.getCombinedXYChartView(activity,mDataset,renderer,types);
                mChartView.setBackgroundColor(Color.BLACK);

                chartLayout = (LinearLayout) activity.findViewById(layout_id);
                chartLayout.removeAllViews();

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                chartLayout.addView(mChartView, layoutParams);


            }
        });

    }

    static int getColor(int index) {
        int color = Color.CYAN;
        switch(index % 5) {
            case 0:
                color = Color.CYAN;
                break;
            case 1:
                color = Color.YELLOW;
                break;
            case 2:
                color = Color.GREEN;
                break;
            case 3:
                color = Color.BLUE;
                break;
            case 4:
                color = Color.RED;
                break;
        }
        return color;
    }


}
