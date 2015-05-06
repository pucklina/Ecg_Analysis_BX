package com.ecg.assist;

import org.achartengine.chart.PointStyle;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.graphics.Color;
import android.graphics.Paint.Align;

public class ChartBuilder {
    public static void buildRenderer(XYMultipleSeriesRenderer renderer,int[] colors,
                                     PointStyle[] styles) {
        int length = colors.length;
        for (int i = 0; i < length; i++) {
            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setColor(colors[i]);
            r.setPointStyle(styles[i]);
            r.setLineWidth(i + 1);
            r.setAnnotationsColor(Color.RED);
            r.setAnnotationsTextAlign(Align.CENTER);
            r.setAnnotationsTextSize(20);
            r.setChartValuesTextAlign(Align.CENTER);
            renderer.addSeriesRenderer(r);
        }
    }
    public static void setChartSettings(XYMultipleSeriesRenderer renderer,
                                        String chartTitle, String xTitle, String yTitle, double xMin,
                                        double xMax, double yMin, double yMax, int axesColor,
                                        int labelsColor, int GirdColor, int MarginsColor,
                                        int BackGroundColor, int[] Margins, boolean ShowLeagend) {
        renderer.setChartTitle(chartTitle);
        renderer.setXTitle(xTitle); // 设置title
        renderer.setYTitle(yTitle);
        renderer.setXAxisMin(xMin);
        renderer.setXAxisMax(xMax);
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);
        renderer.setAxesColor(axesColor); // 坐标轴颜色
        renderer.setLabelsColor(labelsColor); // 标签颜色
        renderer.setGridColor(GirdColor); // 网格的颜色
        renderer.setMarginsColor(MarginsColor); // 边缘外部颜色
        renderer.setMargins(Margins); // 设置图表的边距
        renderer.setBackgroundColor(BackGroundColor); // Color.parseColor("#00000000")
        renderer.setShowLegend(ShowLeagend); // 设置是否显示标注
        // 无参数设置
        renderer.setShowGrid(true);
        renderer.setYLabelsAlign(Align.LEFT); // Y周文字对齐方式
        renderer.setAxisTitleTextSize(20);
        renderer.setChartTitleTextSize(20);
        renderer.setLabelsTextSize(20);
        renderer.setLegendTextSize(15);
        renderer.setPointSize(3);
        renderer.setXLabels(10);
        renderer.setYLabels(5);
        renderer.setApplyBackgroundColor(true);
        renderer.setInScroll(true);
    }

}
