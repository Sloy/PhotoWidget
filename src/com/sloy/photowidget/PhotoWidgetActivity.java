package com.sloy.photowidget;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.view.View;
import android.widget.Button;

public class PhotoWidgetActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ((Button)findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//seleccionar fotos
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent, "Select Picture"),1);
			}
		});
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1 && data != null && data.getData() != null){
           Debug.waitForDebugger();
        	Uri _uri = data.getData();

            if (_uri != null) {
                //User had pick an image.
                Cursor cursor = getContentResolver().query(_uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
                cursor.moveToFirst();

                //Link to the image
                final String imageFilePath = cursor.getString(0);
                cursor.close();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}