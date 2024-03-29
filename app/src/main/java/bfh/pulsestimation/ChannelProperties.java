package bfh.pulsestimation;

import org.achartengine.chart.PointStyle;

public class ChannelProperties {

    private int nSeries = 0;

    private String[] chartTypes;
    private PointStyle[] pointStyles;
    private int[] colors;
    private String[] titles;

    ChannelProperties(int nS){
        nSeries = nS;
        chartTypes = new String[nS];
        titles = new String[nS];
        colors = new int[nS];
        pointStyles = new PointStyle[nS];
    }

    /* Chart Types */
    public String[] getChartTypes() {
        return chartTypes;
    }
    public void setChartTypes(String chartType) {
        for (int s = 0; s < nSeries; s++){
            this.chartTypes[s] = chartType;
        }
    }
    public void setChartType(String chartType, int serie) {
            this.chartTypes[serie] = chartType;
    }

    /* Point Styles */
    public PointStyle[] getPointStyles() {
        return pointStyles;
    }

    public void setPointStyle(PointStyle pointSytyle, int serie) {
            this.pointStyles[serie] = pointSytyle;
    }
    public void setPointStyles(PointStyle pointStyle) {
        for (int s = 0; s < nSeries; s++){
            this.pointStyles[s] = pointStyle;
        }
    }

    /* Colors */
    public int[] getColors() {
        return colors;
    }

    public void setColor(int color, int serie) {
        this.colors[serie] = color;
    }

    /* Titles */
    public String[] getTitles() {
        return titles;
    }

    public void setTitle(String title, int serie) {
        this.titles[serie] = title;
    }

    public int getNSeries(){
        return nSeries;
    }
}
