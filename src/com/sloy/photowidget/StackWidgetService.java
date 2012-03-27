package com.sloy.photowidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.android.dataframework.DataFramework;
import com.android.dataframework.Entity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private List<String> photos;
	private Map<String, Bitmap> bitmapsCache;
	private Bitmap tmpBitmap;

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
		photos = new ArrayList<String>();
		bitmapsCache = new HashMap<String, Bitmap>();
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
		RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
		// rv.setTextViewText(R.id.widget_item,
		// mWidgetItems.get(position).text);

		// ---------------

		String foto = photos.get(position);
		if(bitmapsCache.containsKey(foto)){
			// ya la tengo lista
			tmpBitmap = bitmapsCache.get(foto);
		}else{
			// tengo que prepararla
			
			try{
				tmpBitmap = decodeFile(foto);
			}catch(NullPointerException e){
				// si hay un nullpointerexception porque alguna foto ya no
				// está, recarga la lista de fotos
				onDataSetChanged();
			}
			// tmpBitmap = BitmapFactory.decodeFile(foto);

			int maxvalue = 500;
			// Coge las dimensiones de la imagen
			int x = tmpBitmap.getWidth();
			int y = tmpBitmap.getHeight();
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
			tmpBitmap = Bitmap.createScaledBitmap(tmpBitmap, xP, yP, false);
			// guarda el bitmap
			bitmapsCache.put(foto, tmpBitmap);
		}

		rv.setImageViewBitmap(R.id.widget_item, tmpBitmap);
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

	private Bitmap decodeFile(String fp) {
		Bitmap b = null;
		try{
			// Get the file
			File f = new File(fp);
			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;

			FileInputStream fis = new FileInputStream(f);
			BitmapFactory.decodeStream(fis, null, o);
			fis.close();

			int scale = 1;
			int IMAGE_MAX_SIZE = 700; // TODO global
			if(o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE){
				scale = (int)Math.pow(2, (int)Math.round(Math.log(IMAGE_MAX_SIZE / (double)Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
			}

			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			fis = new FileInputStream(f);
			b = BitmapFactory.decodeStream(fis, null, o2);
			fis.close();
		}catch(IOException e){
			Log.e("photowidget", "Error cargando la foto " + fp, e);
		}
		return b;
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

		DataFramework db = null;
		try{
			db = DataFramework.getInstance();
			db.open(mContext, mContext.getPackageName());
			Entity widg = db.getTopEntity("widgets", "widgetid = " + mAppWidgetId, null);
			photos = ProviderHelper.getPhotosFromAlbum(mContext, widg.getString("acceso_fuente"));
		}catch(Exception tr){
			Log.e("PhotoWidgetService", "Error obteniendo fuente del widget " + mAppWidgetId, tr);
		}
	}
}