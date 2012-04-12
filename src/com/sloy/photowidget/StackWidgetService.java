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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StackWidgetService extends RemoteViewsService {
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		Log.d("PhotoWidgetService", "--- Creando la factor�a ---");
		return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
	}
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
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
		bitmapsCache.clear();
	}

	@Override
	public int getCount() {
		return photos.size();
	}

	@Override
	public RemoteViews getViewAt(int position) {
		Log.i("PhotoWidgetService", "getViewAt " + "(" + position + ")");
		// position will always range from 0 to getCount() - 1.

		// We construct a remote views item based on our widget item xml file,
		RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);

		// Obtiene la foto. Si est� guardada en cach� la coger� de ah�, si no la
		// crear�
		String foto = photos.get(position);
		tmpBitmap = getBitmap(foto);

		// Coloca la foto en el ImageView
		rv.setImageViewBitmap(R.id.widget_item, tmpBitmap);

		// Ahora hace la tonter�a de respuesta al hacer clic en la foto
		// Next, we set a fill-intent which will be used to fill-in the pending
		// intent template
		// which is set on the collection view in StackWidgetProvider.
		Bundle extras = new Bundle();
		extras.putInt(StackWidgetProvider.EXTRA_ITEM, position);
		Intent fillInIntent = new Intent();
		fillInIntent.putExtras(extras);
		rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);

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
		// on the collection view corresponding to this factory.

		// Vac�a la lista de fotos
		photos.clear();
		// TODO vac�a la cach� (o no...)

		// Coge la lista de fotos seg�n la configuraci�n
		// TODO modularizar esto, seg�n el tipo de �lbum y tal
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

	public Bitmap getBitmap(String nombre) {
		Bitmap res = null;
		if(bitmapsCache.containsKey(nombre)){
			// ya la tengo lista
			Log.v("PhotoWidgetService", "Cached");
			res = bitmapsCache.get(nombre);
		}else{
			Log.w("PhotoWidgetService", "New");
			// tengo que prepararla
			// TODO comprobar que el archivo exista y actualizar la lista si no
			
			//calcula las dimensiones m�ximas adecuadas para el dispositivo
			int maxvalue = 500;
			
			//Obtiene la imagen
			res = decodeSampledBitmapFromFile(nombre, maxvalue, maxvalue);
			
			// guarda el bitmap
			bitmapsCache.put(nombre, res);
		}
		return res;
	}

	/*
	 * Using powers of 2 for inSampleSize values is faster and more efficient
	 * for the decoder. However, if you plan to cache the resized versions in
	 * memory or on disk, it�s usually still worth decoding to the most
	 * appropriate image dimensions to save space.
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if(height > reqHeight || width > reqWidth){
			if(width > height){
				inSampleSize = Math.round((float)height / (float)reqHeight);
			}else{
				inSampleSize = Math.round((float)width / (float)reqWidth);
			}
		}
		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromFile(String pathName, int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName, options);
	}

	// My old method
	/*
	 * private Bitmap decodeFile(String fp) {
	 * Bitmap b = null;
	 * try{
	 * // Get the file
	 * File f = new File(fp);
	 * // Decode image size
	 * BitmapFactory.Options o = new BitmapFactory.Options();
	 * o.inJustDecodeBounds = true;
	 * FileInputStream fis = new FileInputStream(f);
	 * BitmapFactory.decodeStream(fis, null, o);
	 * fis.close();
	 * int scale = 1;
	 * int IMAGE_MAX_SIZE = 700; // TODO global
	 * if(o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE){
	 * scale = (int)Math.pow(2, (int)Math.round(Math.log(IMAGE_MAX_SIZE /
	 * (double)Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
	 * }
	 * // Decode with inSampleSize
	 * BitmapFactory.Options o2 = new BitmapFactory.Options();
	 * o2.inSampleSize = scale;
	 * fis = new FileInputStream(f);
	 * b = BitmapFactory.decodeStream(fis, null, o2);
	 * fis.close();
	 * }catch(IOException e){
	 * Log.e("photowidget", "Error cargando la foto " + fp, e);
	 * }
	 * return b;
	 * }
	 */
}