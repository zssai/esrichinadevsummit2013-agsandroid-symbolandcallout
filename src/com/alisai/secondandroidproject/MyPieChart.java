package com.alisai.secondandroidproject;

import org.achartengine.ChartFactory;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;

public class MyPieChart {
	private long[] popValues;
	private String name;
	private double[] popPercentage;
	private double sumPops;
	public MyPieChart(String name, long[] pops) {
		// TODO Auto-generated constructor stub
		this.name = name;
		this.popValues = new long[pops.length];
		popPercentage = new double[pops.length];
		sumPops = 0;
		for(int i=0; i<pops.length; i++){
			popValues[i] = pops[i];
			sumPops = sumPops + pops[i];
		}
		
		for(int i=0; i<pops.length; i++){
			popPercentage[i] = popValues[i]/sumPops;
			System.out.println(popValues[i] + "," + sumPops + "," + popPercentage[i]);
		}
		
		
	}
	
	public View execute(Context context){
		int[] colors = new int[]{Color.RED, Color.CYAN, Color.YELLOW, Color.GREEN, Color.BLUE};
		DefaultRenderer renderer = buildCategoryRenderer(colors); 
		CategorySeries categorySeries = new CategorySeries(name);
		
		categorySeries.add("White ", popPercentage[0]); 
		categorySeries.add("Black ", popPercentage[1]); 
		categorySeries.add("Asian", popPercentage[2]);
		categorySeries.add("Hispanic", popPercentage[3]);
		categorySeries.add("Other", popPercentage[4]);
		
		return ChartFactory.getPieChartView(context, categorySeries, renderer);
	}
	
	public DefaultRenderer buildCategoryRenderer(int[] colors){
		
		DefaultRenderer renderer = new DefaultRenderer(); 
		renderer.setBackgroundColor(Color.LTGRAY);
		renderer.setApplyBackgroundColor(true);
		renderer.setLabelsTextSize(10);
		renderer.setChartTitle(name  + "�˿ڱ�״ͼ");
		renderer.setChartTitleTextSize(16);
		renderer.setShowLegend(false);
		renderer.setLabelsColor(Color.BLACK);
		renderer.setMargins(new int[]{5, 5, 5, 5});
		for (int color : colors) { 
			SimpleSeriesRenderer r = new SimpleSeriesRenderer(); 
			r.setColor(color); 
			renderer.addSeriesRenderer(r); 
		} 
		return renderer; 
		
	}
	

}
