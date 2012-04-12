package com.sloy.photowidget;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AutoUpdateActivity extends Activity {

	// Handler para la actualización de la interfaz desde el hilo en segundo
	// plano
	private Handler handler = new Handler();

	// Runnable que se ejecutará desde el hilo principal cuando acabe la
	// obtención de datos en segundo plano
	private Runnable finishBackgroundDownload = new Runnable() {
		@Override
		public void run() {
			mProgressBar.setVisibility(View.GONE);
			if(mVC.isNewVersionAvailable()){
				// Hay una nueva versión disponible
				mActualizacion.setVisibility(View.VISIBLE);
				mActualizada.setVisibility(View.GONE);
				// Mostramos los datos
				mVersionActual.setText("Versión actual: " + mVC.getCurrentVersionName());
				mVersionNueva.setText("Versión disponible: " + mVC.getLatestVersionName());
			}else{
				// La aplicación está actualizada
				mActualizacion.setVisibility(View.GONE);
				mActualizada.setVisibility(View.VISIBLE);
			}
		}
	};

	// Runnable encargado de descargar los datos en un hilo en segundo plano
	private Runnable backgroundDownload = new Runnable() {
		@Override
		public void run() {
			mVC.getData(AutoUpdateActivity.this);
			// Cuando acabe la descarga actualiza la interfaz
			handler.post(finishBackgroundDownload);
		}
	};
	// Objeto de nuestra clase de utilidad
	private VersionChecker mVC = new VersionChecker();

	// Elementos de la interfaz
	private View mActualizada, mActualizacion, mProgressBar;
	private TextView mVersionActual, mVersionNueva;
	private Button mObtenerDatos, mDescargar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Inicializa la interfaz
		setContentView(R.layout.update);
		mActualizada = findViewById(R.id.actualizada);
		mActualizacion = findViewById(R.id.actualizacion);
		mProgressBar = findViewById(R.id.progress_datos);
		mDescargar = (Button)findViewById(R.id.bt_descarga);
		mObtenerDatos = (Button)findViewById(R.id.bt_obtener_datos);
		mVersionActual = (TextView)findViewById(R.id.actualizacion_version_actual);
		mVersionNueva = (TextView)findViewById(R.id.actualizacion_version_nueva);

		// Coloca listeners en los dos botones
		mObtenerDatos.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Crea un nuevo hilo para descargar los datos
				mProgressBar.setVisibility(View.VISIBLE);
				Thread downloadThread = new Thread(backgroundDownload, "VersionChecker");
				downloadThread.start();;
			}
		});
		mDescargar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Lanza un Intent con el enlace de la descarga, Android se
				// encargará del resto
				startActivity(new Intent("android.intent.action.VIEW", Uri.parse(mVC.getDownloadURL())));
			}
		});

	}
}