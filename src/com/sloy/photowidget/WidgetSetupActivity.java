package com.sloy.photowidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.dataframework.DataFramework;
import com.android.dataframework.Entity;

import java.util.ArrayList;
import java.util.List;

public class WidgetSetupActivity extends Activity {

	private int mWidgetId;
	private List<Album> mAlbums = new ArrayList<Album>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//Widget stuff
		setResult(RESULT_CANCELED);
		Bundle extras = getIntent().getExtras();
		if(extras != null){
			mWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		// Get the list of albums
		mAlbums = ProviderHelper.getAlbumList(this);

		// Crea el adapter y lo asigna a la lista
		AlbumAdapter adapter = new AlbumAdapter(this);
		ListView list = (ListView)findViewById(R.id.listView1);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				 guardarAsociacion("album", mAlbums.get(position).name);
//				Toast.makeText(getApplicationContext(), mAlbums.get(position).id.toString(), Toast.LENGTH_SHORT).show();
			}
		});

	}

	private void guardarAsociacion(String tipo, String acceso) {
		DataFramework db = null;
		try{
			db = DataFramework.getInstance();
			db.open(this, getPackageName());
			Entity widget = new Entity("widgets");
			widget.setValue("widgetid", mWidgetId);
			widget.setValue("tipo_fuente", tipo);
			widget.setValue("acceso_fuente", acceso);
			widget.save();
		}catch(Exception e){
			Log.e("PhotoWidget", "Error al guardar el widget en la bd", e);
			finish();
		}
		if(db != null){
			db.close();
		}

		// configuration complete
		AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
		Intent result = new Intent();
		result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
		setResult(RESULT_OK, result);
		finish();
	}

	private class AlbumAdapter extends BaseAdapter {

		private Context ctx;

		public AlbumAdapter(Context contexto) {
			ctx = contexto;
		}

		@Override
		public int getCount() {
			return mAlbums.size();
		}

		@Override
		public Album getItem(int position) {
			return mAlbums.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Album a = getItem(position);
			View v = convertView;
			if(v == null){
				v = LayoutInflater.from(ctx).inflate(android.R.layout.simple_list_item_2, parent, false);
			}
			TextView tv1 = ((TextView)v.findViewById(android.R.id.text1));
			TextView tv2 = ((TextView)v.findViewById(android.R.id.text2));

			tv1.setText(a.name + " ("+a.count+")");
			tv2.setText(a.directory);
			return v;
		}

	}

}
