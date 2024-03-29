package bfh.pulsestimation;

import android.graphics.Paint;
import android.widget.LinearLayout;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;


public class MultiSerieChart {

    private LinearLayout chartLayout;
    protected GraphicalView mChartView;


    XYMultipleSeriesRenderer renderer;

    protected XYMultipleSeriesDataset mDataset;
    private String title;


    protected MainActivity activity;

    /* Channel Properties */
    protected int n_series;
    private String[] ch_titles;
    private int[] ch_colors;
    private PointStyle[]  ch_styles;
    String[] types;


    MultiSerieChart(MainActivity a, ChannelProperties chProps, String t, int layout_id){


        activity = a;

        title = t;

        n_series = chProps.getNSeries();
        types = chProps.getChartTypes();
        ch_styles = chProps.getPointStyles();
        ch_colors = chProps.getColors();
        ch_titles = chProps.getTitles();


        buildRenderer();
        setChartSettings();
        setRanges(0,5,0,2);
        initDataSet();
        setupChart(layout_id);


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


    private void buildRenderer(){

        renderer = new XYMultipleSeriesRenderer();

        /* Set View related Params*/
        renderer.setChartTitleTextSize(30);
        renderer.setLabelsTextSize(30);
        renderer.setLegendTextSize(30);
        renderer.setAxisTitleTextSize(30);
        renderer.setPointSize(2 * 5f);

        renderer.setShowCustomTextGrid(true);


        for (int i = 0; i < n_series; i++) {
            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setColor(ch_colors[i]);
            r.setPointStyle(ch_styles[i]);
            r.setLineWidth(5);
            renderer.addSeriesRenderer(r);
        }

    }


    private void setChartSettings(){

        int textColor = activity.getColor(R.color.textColor);
        int backgroundColor = activity.getColor(R.color.backgroundColor);
        renderer.setChartTitle(title);
        renderer.setXTitle("");
        renderer.setYTitle("");

        renderer.setAxesColor(textColor);
        renderer.setLabelsColor(textColor);

        renderer.setYLabelsAlign(Paint.Align.RIGHT);

        int[] margins = {25,120, 50 ,20};
        renderer.setMargins(margins);
        renderer.setMarginsColor(backgroundColor);

        renderer.setBackgroundColor(backgroundColor);


        renderer.setShowGridX(true);
        renderer.setShowGridY(true);

        renderer.setShowLegend(false);
        renderer.setXLabelsColor(textColor);
        renderer.setYLabelsColor(0, textColor);
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
                if(yPadding < 0.01) yPadding = 0.01;

                /* Set Y Range*/
                renderer.setYAxisMax(mDataset.getSeriesAt(ch_number).getMaxY() + yPadding);
                renderer.setYAxisMin(mDataset.getSeriesAt(ch_number).getMinY() - yPadding);

                /* Set X Range */
                double minX = mDataset.getSeriesAt(ch_number).getMinX();
                renderer.setXAxisMin(minX);
                renderer.setXAxisMax(minX + sizeX);

                //Log.v("Simple Line Chart","Auto range, rendererYMax: " + renderer.getYAxisMax() + "dataset Y Max: "+mDataset.getSeriesAt(ch_number).getMaxY());
            }
        });
    }

    protected void disableInteractivity(){
        renderer.setPanEnabled(false,false);
        renderer.setZoomEnabled(false,false);
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
                mChartView.setBackgroundColor(activity.getColor(R.color.backgroundColor));


                chartLayout = (LinearLayout) activity.findViewById(layout_id);
                chartLayout.removeAllViews();

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                chartLayout.addView(mChartView, layoutParams);


            }
        });

    }

}
