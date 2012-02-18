package com.sloy.photowidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.android.dataframework.DataFramework;
import com.android.dataframework.Entity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StackWidgetService extends RemoteViewsService {
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
	}
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
	// private List<WidgetItem> mWidgetItems = new ArrayList<WidgetItem>();
	private Context mContext;
	private int mAppWidgetId;
	private List<Uri> photos;

	public StackRemoteViewsFactory(Context context, Intent intent) {
		mContext = context;
		mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
	}

	@Override
	public void onCreate() {
		Log.d("PhotoWidgetService", "onCreate " + mAppWidgetId);
		// In onCreate() you setup any connections / cursors to your data
		// source. Heavy lifting,
		// for example downloading or creating content etc, should be deferred
		// to onDataSetChanged()
		// or getViewAt(). Taking more than 20 seconds in this call will result
		// in an ANR.

		// -------
		photos = new ArrayList<Uri>();
	}

	@Override
	public void onDestroy() {
		Log.d("PhotoWidgetService", "onDestroy " + mAppWidgetId);
		// In onDestroy() you should tear down anything that was setup for your
		// data source,
		// eg. cursors, connections, etc.
		photos.clear();
	}

	@Override
	public int getCount() {
		return photos.size();
	}

	@Override
	public RemoteViews getViewAt(int position) {
		Log.d("PhotoWidgetService", "getViewAt " + mAppWidgetId + "(" + position + ")");
		// position will always range from 0 to getCount() - 1.

		// We construct a remote views item based on our widget item xml file,
		// and set the
		// text based on the position.
		// Debug.waitForDebugger();
		RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
		// rv.setTextViewText(R.id.widget_item,
		// mWidgetItems.get(position).text);

		// ---------------
		Uri foto = photos.get(position);
		Bitmap bitmap = BitmapFactory.decodeFile(foto.toString());
		int maxvalue = 500;
//		Debug.waitForDebugger();
		// Coge las dimensiones de la imagen
		int x = bitmap.getWidth();
		int y = bitmap.getHeight();
		int xP = x, yP = y; // iguales por defecto
		// Calcula el nuevo tamaño
		if(x > y){
			// Mayor es X
			xP = maxvalue;
			yP = y * xP / x;
		}else{
			// Mayor es Y (o son iguales)
			yP = maxvalue;
			xP = x * yP / y;
		}
		bitmap = Bitmap.createScaledBitmap(bitmap, xP, yP, false);

		// rv.setImageViewUri(R.id.widget_item, foto);
		rv.setImageViewBitmap(R.id.widget_item, bitmap);
		// --------------------

		// Next, we set a fill-intent which will be used to fill-in the pending
		// intent template
		// which is set on the collection view in StackWidgetProvider.
		Bundle extras = new Bundle();
		extras.putInt(StackWidgetProvider.EXTRA_ITEM, position);
		Intent fillInIntent = new Intent();
		fillInIntent.putExtras(extras);
		rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);

		// You can do heaving lifting in here, synchronously. For example, if
		// you need to
		// process an image, fetch something from the network, etc., it is ok to
		// do it here,
		// synchronously. A loading view will show up in lieu of the actual
		// contents in the
		// interim.
		/*
		 * try{
		 * System.out.println("Loading view " + position);
		 * Thread.sleep(500);
		 * }catch(InterruptedException e){
		 * e.printStackTrace();
		 * }
		 */

		// Return the remote views object.
		return rv;
	}

	@Override
	public RemoteViews getLoadingView() {
		// You can create a custom loading view (for instance when getViewAt()
		// is slow.) If you
		// return null here, you will get the default loading view.
		return null;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public void onDataSetChanged() {
		Log.d("PhotoWidgetService", "onDataSetChanged " + mAppWidgetId);
		// This is triggered when you call AppWidgetManager
		// notifyAppWidgetViewDataChanged
		// on the collection view corresponding to this factory. You can do
		// heaving lifting in
		// here, synchronously. For example, if you need to process an image,
		// fetch something
		// from the network, etc., it is ok to do it here, synchronously. The
		// widget will remain
		// in its current state while work is being done here, so you don't need
		// to worry about
		// locking up the widget.
		photos.clear();

		File directorio = null;

		DataFramework db = null;
		try{
			db = DataFramework.getInstance();
			db.open(mContext, mContext.getPackageName());
			Entity widg = db.getTopEntity("widgets", "widgetid = " + mAppWidgetId, null);
			Entity fuente = db.getTopEntity("fuentes_carpeta", "_id=" + widg.getInt("fuente"), null);
			directorio = new File(fuente.getString("direccion"));
		}catch(Exception tr){
			Log.e("PhotoWidgetService", "Error obteniendo fuente del widget " + mAppWidgetId, tr);
		}
		if(!directorio.exists()){
			Log.e("PhotoWidget", "No existe el directorio de fotos en la sd");
		}else{
			File[] files = directorio.listFiles();
			for(File f : files){
				String filePath = f.getAbsolutePath();
				photos.add(Uri.parse(filePath));
			}
		}
	}
}