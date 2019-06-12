package bfh.pulsestimation;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.mcc.ul.AiUnit;
import com.mcc.ul.Range;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.List;

public class DataChart
{

	private GraphicalView mChartView;
	private XYMultipleSeriesDataset mDataset;
	private LinearLayout chartLayout;
	private String title;

	private boolean showGrid = true;

	Activity activity;

	/* Thread for the Plots */
	private HandlerThread plotHandlerThread;
	private Looper plotLooper;
	private Handler plotHandler;


	DataChart(Activity a, LinearLayout cLayout, String t){


		chartLayout = cLayout;
		activity = a;
		title = t;

		/* Create Thread, Looper, and Handler */
		plotHandlerThread = new HandlerThread("HandlerThread_for_plots");
		plotHandlerThread.start();
		plotLooper = plotHandlerThread.getLooper();
		plotHandler = new Handler(plotLooper);

	}

	public void initOnPlotThread(final double minValue, final double maxValue, final int sampleCount, final int chanCount){
		plotHandler.post(new Runnable() {
			@Override
			public void run() {
				init(minValue,maxValue,sampleCount,chanCount);
			}
		});
	}

	private void init(final double minValue, final double maxValue, final int sampleCount, int chanCount) //double[][] dataValues)
	{
		/* Prepare Channel properties */
		final String[] titles = new String[chanCount];
		final int [] colors = new int[chanCount];
        final PointStyle[] styles = new PointStyle[chanCount];

		/* Set Channel properties */
		for(int i = 0; i < chanCount; i++) {
			titles[i] = "Chan " + (i);
			colors[i] = getColor(i);
			styles[i] = PointStyle.POINT;
		}

		/* Initialize Data */
        final List<double[]> x = new ArrayList<double[]>();
        final List<double[]> values = new ArrayList<double[]>();

        for(int ch = 0; ch < chanCount; ch++)
        	x.add(new double[sampleCount]);
        
        for (int i = 0; i < sampleCount; i++) 
        	for(int ch = 0; ch < chanCount; ch++)
        		x.get(ch)[i] = i;
     
        for(int i = 0; i < chanCount; i++) {
        	double dataValue[] = new double[sampleCount];
        	
        	for (int j = 0; j < sampleCount; j++) 
        		dataValue[j] = minValue;
        	
        	values.add(dataValue);
        }

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				/* Prepare Renderer */
				XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);

				int length = renderer.getSeriesRendererCount();
				for (int i = 0; i < length; i++) {
					((XYSeriesRenderer) renderer.getSeriesRendererAt(i)).setFillPoints(true);
				}

				setChartSettings(renderer, title, "", "Data", 0, sampleCount - 1, minValue, maxValue,
						Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, showGrid);

				mDataset =  buildDataset(titles, x, values);
				mChartView =  ChartFactory.getLineChartView(activity, mDataset, renderer);
				mChartView.setBackgroundColor(Color.BLACK);

				Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
				int height = display.getHeight();
				int width = display.getWidth();

				int chartMinHeight;

				if(height > width)
					chartMinHeight = height/3;
				else
					chartMinHeight = height/2;

				mChartView.setMinimumHeight(chartMinHeight);

				chartLayout.removeAllViews();

				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

				chartLayout.addView(mChartView, layoutParams);
			}
		});

	}


	static XYMultipleSeriesDataset buildDataset(String[] titles, List<double[]> xValues,
												List<double[]> yValues)
	{
  	    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
  	    addXYSeries(dataset, titles, xValues, yValues, 0);
  	    return dataset;
  	 }
  
  static void addXYSeries(XYMultipleSeriesDataset dataset, String[] titles, List<double[]> xValues,
						  List<double[]> yValues, int scale)
  {
  	    int length = titles.length;
  	    for (int i = 0; i < length; i++) {
  	      XYSeries series = new XYSeries(titles[i], scale);
  	      double[] xV = xValues.get(i);
  	      double[] yV = yValues.get(i);
  	      int seriesLength = xV.length;
  	      for (int k = 0; k < seriesLength; k++) {
  	        series.add(xV[k], yV[k]);
  	      }
  	      dataset.addSeries(series);    
  	    }
  }




  
  public void plotOnPlotThread(final double[][] DataValues)
  {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {

					for(int i = 0; i < mDataset.getSeries().length; i++)
						mDataset.getSeriesAt(i).clear();

					for(int i = 0; i < mDataset.getSeries().length; i++)
						for (int j = 0; j < DataValues.length; j++)
							mDataset.getSeriesAt(i).add(j, DataValues[j][i]);

					mChartView.repaint();
					Log.v("DataChart","Chart repainted");

				}
			});
  }


  
  static XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles)
  {
      XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
      
      setRenderer(renderer, colors, styles);
      return renderer;
    }
  
  static void setRenderer(XYMultipleSeriesRenderer renderer, int[] colors, PointStyle[] styles)
  {
      renderer.setAxisTitleTextSize(16);
      renderer.setChartTitleTextSize(20);
      renderer.setLabelsTextSize(15);
      renderer.setLegendTextSize(15);
      renderer.setPointSize(5f);
      renderer.setMargins(new int[] { 20, 30, 15, 20 });
      int length = colors.length;
      for (int i = 0; i < length; i++) {
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setColor(colors[i]);
        r.setPointStyle(styles[i]);
        renderer.addSeriesRenderer(r);
      }
    }
  
  static void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
							   String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor,
							   int labelsColor, int xLablesColor, int yLablesColor, boolean showGrid)
  {
  	    renderer.setChartTitle(title);
  	    renderer.setXTitle(xTitle);
  	    renderer.setYTitle(yTitle);
  	    renderer.setXAxisMin(xMin);
  	    renderer.setXAxisMax(xMax);
  	    renderer.setYAxisMin(yMin);
  	    renderer.setYAxisMax(yMax);
  	    renderer.setAxesColor(axesColor);
  	    renderer.setLabelsColor(labelsColor);
  	    
  	    renderer.setYLabelsAlign(Align.RIGHT);
  	       
  	    int[] margins = {40,70,10,50};
  	    renderer.setMargins(margins);
  	    renderer.setMarginsColor(Color.LTGRAY);
  	    
  	    renderer.setBackgroundColor(Color.BLACK);
  	    
  	    renderer.setShowGrid(showGrid);
  	    renderer.setXLabelsColor(xLablesColor);
  	    renderer.setYLabelsColor(0, yLablesColor); 
  	    renderer.setXLabels(11);
  	    renderer.setYLabels(11);
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

