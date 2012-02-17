package com.sloy.photowidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;

import com.android.dataframework.DataFramework;
import com.android.dataframework.Entity;

public class WidgetSetupActivity extends Activity {

	private int mWidgetId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED);
		Bundle extras = getIntent().getExtras();
		Debug.waitForDebugger();
		if(extras != null){
			mWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		DataFramework db = null;
		try{
			db = DataFramework.getInstance();
			db.open(this, getPackageName());
			Entity widget = new Entity("widgets");
			widget.setValue("widgetid", mWidgetId);
			widget.setValue("fuente", 1);
			widget.save();
		}catch(Exception e){
			Log.e("PhotoWidget", "Error al guardar el widget en la bd", e);
			finish();
		}
		if(db != null){
			db.close();
		}
		
		//configuration complete
		AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
		
		Intent result = new Intent();
		result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
		setResult(RESULT_OK, result);
		finish();

	}

}
