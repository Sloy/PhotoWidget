package com.sloy.photowidget;

import android.app.Fragment;
import android.appwidget.AppWidgetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.dataframework.DataFramework;
import com.android.dataframework.Entity;

import java.util.List;

public class ConfigWidgetFragment extends Fragment {

	ArrayAdapter<Entity> mAdapter;

	/**
	 * Create a new instance of DetailsFragment, initialized to
	 * show the text at 'index'.
	 */
	public static ConfigWidgetFragment newInstance(int index, Entity widget) {
		ConfigWidgetFragment f = new ConfigWidgetFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putLong("id", widget.getId());
		args.putInt("index", index);
		args.putInt("widgetid", widget.getInt("widgetid"));
		args.putInt("fuente", widget.getInt("fuente"));
		f.setArguments(args);
		return f;
	}

	public int getShownIndex() {
		return getArguments().getInt("index", 0);
	}

	public long getShownId() {
		return getArguments().getLong("id", 0);
	}

	public int getWidgetId() {
		return getArguments().getInt("widgetid");
	}

	public int getFuenteId() {
		return getArguments().getInt("fuente");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(container == null){
			// We have different layouts, and in one of them this
			// fragment's containing frame doesn't exist. The fragment
			// may still be created from its saved state, but there is
			// no reason to try to create its view hierarchy because it
			// won't be displayed. Note this is not needed -- we could
			// just run the code below, where we would create and return
			// the view hierarchy; it would just never be used.
			return null;
		}

		View v = inflater.inflate(R.layout.config_widget, container, false);
		final Spinner sp = (Spinner)v.findViewById(R.id.spinner1);
		((TextView)v.findViewById(R.id.textView1)).setText("Widget ID: " + getWidgetId());

		DataFramework db = null;
		List<Entity> fuentes = null;
		try{
			db = DataFramework.getInstance();
			db.open(getActivity(), getActivity().getPackageName());
			fuentes = db.getEntityList("fuentes_carpeta");
		}catch(Exception e){
			Log.e("PhotoWidget", "Error cargando la lista de fuentes", e);
		}
		if(db != null){
			db.close();
		}

		mAdapter = new ArrayAdapter<Entity>(getActivity(), android.R.layout.simple_spinner_item, fuentes);
		mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp.setAdapter(mAdapter);

		int pos = 0;
		long fuenteID = (long)getFuenteId();
		for(int i = 0; i < mAdapter.getCount(); i++){
			long currentID = mAdapter.getItem(i).getId();
			if(currentID == fuenteID){
				pos = i;
				break;
			}
		}
		sp.setSelection(pos);

		((Button)v.findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DataFramework db = null;
				try{
					db = DataFramework.getInstance();
					db.open(getActivity(), getActivity().getPackageName());
					Entity e = new Entity("widgets", getShownId());
					e.setValue("fuente", mAdapter.getItem(sp.getSelectedItemPosition()).getId());
					e.save();
				}catch(Exception e){
					Log.e("PhotoWidget", "Error guardando la configuración del widget", e);
				}
				if(db != null){
					db.close();
				}
				AppWidgetManager.getInstance(getActivity()).notifyAppWidgetViewDataChanged(getWidgetId(), R.id.stack_view);
			}
		});
		return v;
	}
}