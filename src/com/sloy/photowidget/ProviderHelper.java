package com.sloy.photowidget;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class ProviderHelper {

	public static List<Album> getAlbumList(Context ctx) {
		List<Album> res = new ArrayList<Album>();

		// Query for all images on external storage
		String[] projection = {MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA};
		String selection = "";
		String[] selectionArgs = null;
		Cursor mImageCursor = ctx.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);

		if(mImageCursor.moveToFirst()){
			int bucketColumn = mImageCursor.getColumnIndex(projection[0]);
			int dataColumn = mImageCursor.getColumnIndex(projection[1]);
			do{
				// Get the field values
				Album a = new Album(mImageCursor.getString(bucketColumn));
				if(res.contains(a)){
					res.get(res.indexOf(a)).count++;
				}else{
					String dir = mImageCursor.getString(dataColumn);
					a.directory = dir.substring(0, dir.lastIndexOf('/'));
					res.add(a);
				}
			}while(mImageCursor.moveToNext());
		}
		mImageCursor.close();
		/*
		 * for(Album a : res){
		 * Log.i("album", a.name + ": " + a.count);
		 * }
		 */
		return res;

	}

	public static List<String> getPhotosFromAlbum(Context ctx, String albumName) {
		List<String> res = new ArrayList<String>();

		String searchParams = "bucket_display_name = \"" + albumName + "\"";
		String[] projection = {MediaStore.Images.Media.DATA};
		Cursor mImageCursor = ctx.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, searchParams, null, null);
		if(mImageCursor.moveToFirst()){
			do{
				// Get the field values
				res.add(mImageCursor.getString(mImageCursor.getColumnIndex(projection[0])));
			}while(mImageCursor.moveToNext());
		}
		mImageCursor.close();
		return res;
	}
}
