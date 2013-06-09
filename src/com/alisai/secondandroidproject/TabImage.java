package com.alisai.secondandroidproject;

import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;

import com.esri.core.geometry.Point;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.renderer.ClassBreak;
import com.esri.core.renderer.ClassBreaksRenderer;
import com.esri.core.renderer.Renderer;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TabImage extends Fragment {
	private MapView mMapView;
	private ArcGISTiledMapServiceLayer tiledLayer;
	private GraphicsLayer gLayer;
	private Button queryBtn;
	private Callout callout;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mMapView = (MapView)getView().findViewById(R.id.mapimage);
		tiledLayer = new ArcGISTiledMapServiceLayer("http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer");
		//tiledLayer = new ArcGISTiledMapServiceLayer("http://192.168.23.1:7080/PBS/rest/services/worldsimagemap/MapServer");
		mMapView.addLayer(tiledLayer);
		
		gLayer = new GraphicsLayer();
		gLayer.setRenderer(createClassBreaksRenderer());
		mMapView.addLayer(gLayer);
		
		queryBtn = (Button)getView().findViewById(R.id.querybtn);

		queryBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// Sets query parameter
				Query query = new Query();				
				query.setWhere("STATE_NAME='Kansas'");

				query.setReturnGeometry(true);
				String[] outfields = new String[] { "NAME", "POP07_SQMI", "WHITE", "BLACK", "ASIAN", "HISPANIC", "OTHER"};
				query.setOutFields(outfields);
				query.setOutSpatialReference(mMapView.getSpatialReference());

				Query[] queryParams = { query };

				AsyncQueryTask qt = new AsyncQueryTask();

				qt.execute(queryParams);

			}
		});
		
		mMapView.setOnSingleTapListener(new OnSingleTapListener() {

			private static final long serialVersionUID = 1L;

			public void onSingleTap(float x, float y) {

				if (!mMapView.isLoaded())
					return;
				int[] uids = gLayer.getGraphicIDs(x, y, 2);
				if (uids != null && uids.length > 0) {

					int targetId = uids[0];
					Graphic gr = gLayer.getGraphic(targetId);
				    callout = mMapView.getCallout();

				   callout.setStyle(R.xml.countypop);
				   String countyName = (String) gr.getAttributeValue("NAME");
				   String countyPop = gr.getAttributeValue("POP07_SQMI")
						.toString();
				   
				   long whitePops = Long.parseLong(gr.getAttributeValue("WHITE").toString());
				   long blackPops = Long.parseLong(gr.getAttributeValue("BLACK").toString());
				   long asianPops = Long.parseLong(gr.getAttributeValue("ASIAN").toString());
				   long hispanicPops = Long.parseLong(gr.getAttributeValue("HISPANIC").toString());
				   long otherPops = Long.parseLong(gr.getAttributeValue("OTHER").toString());
				   
				   long[] popValues = new long[]{whitePops, blackPops, asianPops, hispanicPops, otherPops};
				    // Sets custom content view to Callout
				   MyPieChart myPieChart = new MyPieChart(countyName, popValues);
				   View pieChartView = myPieChart.execute(getActivity());
				   
				   View calloutView = loadView(countyName, countyPop);
				   LinearLayout layout= (LinearLayout)calloutView.findViewById(R.id.piechartcallout);
				   layout.addView(pieChartView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				   callout.setContent(calloutView);
				   callout.show(mMapView.toMapPoint(new Point(x, y)));
				}else {
					if (callout != null && callout.isShowing()) {
						callout.hide();
					}
				}

			}
		});
		
	}
	
	private View loadView(String countyName, String countyPop){
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.sqmi, null);

		final TextView name = (TextView) view.findViewById(R.id.county_name);
		name.setText(countyName + "'s SQMI");

		final TextView number = (TextView) view.findViewById(R.id.pop_sqmi);
		number.setText("ÈË¿ÚÊý£º" + countyPop);

		return view;
	}

	private class AsyncQueryTask extends AsyncTask<Query, Void, FeatureSet> {

		protected FeatureSet doInBackground(Query... params) {

			if (params.length > 0) {
				Query query = params[0];
				QueryTask queryTask = new QueryTask("http://sampleserver1.arcgisonline.com/ArcGIS/rest/services/Demographics/ESRI_Census_USA/MapServer/3");
				try {
					FeatureSet fs = queryTask.execute(query);
					return fs;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}

			return null;
		}

		
		protected void onPostExecute(FeatureSet fs) {
			gLayer.addGraphics(fs.getGraphics());
			queryBtn.setEnabled(false);
		}

	}
	
	
	private Renderer<Graphic> createClassBreaksRenderer() {
		ClassBreaksRenderer renderer = new ClassBreaksRenderer();
		renderer.setMinValue(0.0);
		renderer.setField("POP07_SQMI");
		ClassBreak cb1 = new ClassBreak();
		cb1.setClassMaxValue(25);
		cb1.setSymbol(new SimpleFillSymbol(Color.argb(128, 56, 168, 0)));
		cb1.setLabel("First class");

		ClassBreak cb2 = new ClassBreak();
		cb2.setClassMaxValue(75);
		cb2.setSymbol(new SimpleFillSymbol(Color.argb(128, 139, 209, 0)));
		cb2.setLabel("Second class");

		ClassBreak cb3 = new ClassBreak();
		cb3.setClassMaxValue(175);
		cb3.setSymbol(new SimpleFillSymbol(Color.argb(128, 255, 255, 0)));
		cb3.setLabel("Third class");

		ClassBreak cb4 = new ClassBreak();
		cb4.setClassMaxValue(400);
		cb4.setSymbol(new SimpleFillSymbol(Color.argb(128, 255, 128, 0)));
		cb4.setLabel("Fourth class");

		ClassBreak cb5 = new ClassBreak();
		cb5.setClassMaxValue(Double.MAX_VALUE);
		cb5.setSymbol(new SimpleFillSymbol(Color.argb(128, 255, 0, 0)));

		renderer.addClassBreak(cb1);
		renderer.addClassBreak(cb2);
		renderer.addClassBreak(cb3);
		renderer.addClassBreak(cb4);
		renderer.addClassBreak(cb5);
		return renderer;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.tabimage, container, false);
		return view;
	}

}
