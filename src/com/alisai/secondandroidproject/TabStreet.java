package com.alisai.secondandroidproject;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.renderer.ClassBreak;
import com.esri.core.renderer.ClassBreaksRenderer;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class TabStreet extends Fragment {

	private MapView mMapView;
	private ArcGISTiledMapServiceLayer tiledLayer;
	private RadioGroup drawOptionsRadioGroup;
	private ArcGISFeatureLayer featureLayer;
	private String queryServiceUrl;
	private AsyncQueryTask asyncQuery;
	
	private GraphicsLayer gLayerSymbol;
	private GraphicsLayer gLayerRenderer;
	private boolean featureLayerLoaded;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mMapView = (MapView)getView().findViewById(R.id.mapstreet);
		
		drawOptionsRadioGroup = (RadioGroup)getView().findViewById(R.id.drawoptions);
		drawOptionsRadioGroup.setVisibility(View.GONE);		
		
		tiledLayer = new ArcGISTiledMapServiceLayer("http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer");
		//tiledLayer = new ArcGISTiledMapServiceLayer("http://192.168.88.1:7080/PBS/rest/services/worldstreetmap/MapServer");
		mMapView.addLayer(tiledLayer);
		featureLayerLoaded = true;
		
		//queryServiceUrl = "http://192.168.100.118:8399/arcgis/rest/services/hurricane/MapServer/0";
		queryServiceUrl = "http://192.168.88.1:8066/arcgis/services/hurricane/MapServer/0";
		featureLayer = new ArcGISFeatureLayer(queryServiceUrl, ArcGISFeatureLayer.MODE.ONDEMAND);
		mMapView.addLayer(featureLayer);
		featureLayerLoaded = true;

		mMapView.setOnSingleTapListener(new OnSingleTapListener(){

			@Override
			public void onSingleTap(float x, float y) {
				// TODO Auto-generated method stub
				drawOptionsRadioGroup.setVisibility(View.VISIBLE);	
				// mMapView.removeLayer(featureLayer);
			}	
		});
		
		drawOptionsRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				String drawOption;
				if(featureLayerLoaded){
					mMapView.removeLayer(featureLayer);
					featureLayerLoaded = false;
				}
				if(checkedId == R.id.symboldraw){
					drawOption = "1";
					gLayerSymbol = new GraphicsLayer();
					if(gLayerRenderer!=null && gLayerRenderer.getGraphicIDs().length>0){
						mMapView.removeLayer(gLayerRenderer);
					}
					mMapView.addLayer(gLayerSymbol);
					asyncQuery = new AsyncQueryTask();
				}else{
					drawOption = "2";
					gLayerRenderer = new GraphicsLayer();
					mMapView.removeLayer(gLayerSymbol);
					mMapView.addLayer(gLayerRenderer);
					asyncQuery = new AsyncQueryTask();
				}
				String[] queryParams = {queryServiceUrl, drawOption}; 
				asyncQuery.execute(queryParams);
			}
			
		});
	}
	
	private class AsyncQueryTask extends AsyncTask<String, Void, FeatureSet>{
		public int radioButtonStatus;
		@Override
		protected FeatureSet doInBackground(String... queryParams) {
			// TODO Auto-generated method stub
			if(queryParams==null || queryParams.length<=1)
				return null;
			String url = queryParams[0];
			radioButtonStatus = Integer.parseInt(queryParams[1]);
			Query query = new Query();
			query.setOutSpatialReference(SpatialReference.create(102100));
			query.setReturnGeometry(true);
			String[] outFields = new String[]{"WINDSPEED"};
			query.setOutFields(outFields);
			query.setWhere("1=1");
			
			QueryTask qTask = new QueryTask(url);
			FeatureSet fs = null;
			
			try{
				fs = qTask.execute(query);
				
			}catch(Exception e){
				e.printStackTrace();
				return fs;
			}
			return fs;
		}
		@Override
		protected void onPostExecute(FeatureSet result) {
			// TODO Auto-generated method stub
			if(result != null){
				Graphic[] grs = result.getGraphics();
				if(grs.length > 0){
					if(radioButtonStatus==1){
						for(int i=0; i<grs.length; i++){
							Point point = (Point)grs[i].getGeometry();
							Drawable picMarkerSymbol = TabStreet.this.getResources().getDrawable(R.drawable.hurisymbol);
							Graphic graphic = new Graphic(point, new PictureMarkerSymbol(picMarkerSymbol));
							gLayerSymbol.addGraphic(graphic);
						}
						
					}else{
						ClassBreaksRenderer breaksRenderer=new ClassBreaksRenderer();
						breaksRenderer.setField("WINDSPEED");
						breaksRenderer.setMinValue(0);

						ClassBreak cb1=new ClassBreak();
						cb1.setClassMaxValue(45);
						cb1.setSymbol(new SimpleMarkerSymbol(Color.RED,12,SimpleMarkerSymbol.STYLE.CIRCLE));
						breaksRenderer.addClassBreak(cb1);

						ClassBreak cb2=new ClassBreak();
						cb2.setClassMaxValue(75);
						cb2.setSymbol(new SimpleMarkerSymbol(Color.GREEN,12,SimpleMarkerSymbol.STYLE.CIRCLE));
						breaksRenderer.addClassBreak(cb2);
						
						ClassBreak cb3=new ClassBreak();
						cb3.setClassMaxValue(120);
						cb3.setSymbol(new SimpleMarkerSymbol(Color.BLUE,12,SimpleMarkerSymbol.STYLE.CIRCLE));
						breaksRenderer.addClassBreak(cb3);

						gLayerRenderer.setRenderer(breaksRenderer);
						gLayerRenderer.addGraphics(grs);
						
					}
				}
			}
		}
		
		
		
		
	}
	
	/*
	 * 添加飓风数据，利用 PictureMarkerSymbol 实现对飓风数据的绘制
	 */
	
	private void addHurricane(){
		
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.tabstreet, container, false);
		return view;
	}

}
