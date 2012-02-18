package com.sloy.photowidget;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.android.dataframework.DataFramework;
import com.android.dataframework.Entity;

public class ConfigCarpetaFragment extends Fragment {
	/**
	 * Create a new instance of DetailsFragment, initialized to
	 * show the text at 'index'.
	 */
	public static ConfigCarpetaFragment newInstance(int index, Entity fuente) {
		ConfigCarpetaFragment f = new ConfigCarpetaFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putLong("id", fuente.getId());
		args.putInt("index", index);
		args.putString("nombre", fuente.getString("nombre"));
		args.putString("direccion", fuente.getString("direccion"));
		f.setArguments(args);
		return f;
	}

	public int getShownIndex() {
		return getArguments().getInt("index", 0);
	}

	public long getShownId() {
		return getArguments().getLong("id", 0);
	}

	public String getNombre() {
		return getArguments().getString("nombre");
	}

	public String getDireccion() {
		return getArguments().getString("direccion");
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

		View v = inflater.inflate(R.layout.config_carpeta, container, false);
		final EditText nombre = ((EditText)v.findViewById(R.id.editText1));
		nombre.setText(getNombre());
		
		final EditText direccion = ((EditText)v.findViewById(R.id.editText2));
		direccion.setText(getDireccion());
		
		((Button)v.findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DataFramework db = null;
				try{
					db = DataFramework.getInstance();
					db.open(getActivity(), getActivity().getPackageName());
					Entity e = new Entity("fuentes_carpeta", getShownId());
					e.setValue("nombre", nombre.getText().toString());
					e.setValue("direccion", direccion.getText().toString());
					e.save();
				}catch(Exception e){
					Log.e("PhotoWidget", "Error guardando la configuración del widget", e);
				}
				if(db != null){
					db.close();
				}
				//TODO actualizar todos los widgets que usen esta fuente
			}
		});

		return v;
	}
}