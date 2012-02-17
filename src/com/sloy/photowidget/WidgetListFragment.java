package com.sloy.photowidget;

import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.dataframework.DataFramework;
import com.android.dataframework.Entity;
import com.sloy.photowidget.PhotoWidgetActivity.DetailsActivity;

import java.util.List;

public class WidgetListFragment extends ListFragment {
		boolean mDualPane;
		int mCurCheckPosition = 0;
		WidgetsAdapter mAdapter;

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			// Populate list
			mAdapter = new WidgetsAdapter(this.getActivity());
			setListAdapter(mAdapter);
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			// Check to see if we have a frame in which to embed the details
			// fragment directly in the containing UI.
			View detailsFrame = getActivity().findViewById(R.id.details);
			mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

			if(savedInstanceState != null){
				// Restore last state for checked position.
				mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
			}

			if(mDualPane){
				// In dual-pane mode, the list view highlights the selected
				// item.
				getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
				// Make sure our UI is in the correct state.
				// showDetails(mCurCheckPosition);
			}
		}

		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putInt("curChoice", mCurCheckPosition);
		}

		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			showDetails(position);
		}

		/**
		 * Helper function to show the details of a selected item, either by
		 * displaying a fragment in-place in the current UI, or starting a
		 * whole new activity in which it is displayed.
		 */
		void showDetails(int index) {
			mCurCheckPosition = index;

			if(mDualPane){
				// We can display everything in-place with fragments, so update
				// the list to highlight the selected item and show the data.
				getListView().setItemChecked(index, true);

				// Check what fragment is currently shown, replace if needed.
				ConfigCarpetaFragment details = (ConfigCarpetaFragment)getFragmentManager().findFragmentById(R.id.details);
				if(details == null || details.getShownIndex() != index){
					// Make new fragment to show this selection.
					details = ConfigCarpetaFragment.newInstance(index,mAdapter.getItem(index));

					// Execute a transaction, replacing any existing fragment
					// with this one inside the frame.
					FragmentTransaction ft = getFragmentManager().beginTransaction();
					ft.replace(R.id.details, details);
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					ft.commit();
				}

			}else{
				// Otherwise we need to launch a new activity to display
				// the dialog fragment with selected text.
				Intent intent = new Intent();
				intent.setClass(getActivity(), DetailsActivity.class);
				intent.putExtra("index", index);
				startActivity(intent);
			}
		}

		private class WidgetsAdapter extends BaseAdapter {

			List<Entity> mWidgets;
			Context mContext;

			public WidgetsAdapter(Context context){
				mContext = context;
				DataFramework db = null;
				try{
					db = DataFramework.getInstance();
					db.open(context,context.getPackageName());
					
/*					db.emptyTables();
					Entity e = new Entity("fuentes_carpeta");
					e.setValue("nombre", "Default");
					e.setValue("direccion", "/mnt/sdcard/PhotoWidget/");
					e.save();
					e=new Entity("fuentes_carpeta");
					e.setValue("nombre", "Cámara");
					e.setValue("direccion", "/mnt/sdcard/DCIM/Camera/");
					e.save();*/
					
					mWidgets = db.getEntityList("widgets");
				}catch(Exception e){
					Log.e("PhotoWidget", "Error cargando la lista de fuentes", e);
				}
				if(db!=null){
					db.close();
				}
			}

			@Override
			public int getCount() {
				if(mWidgets!=null){
					return mWidgets.size();
				}else{
					return 0;
				}
			}

			@Override
			public Entity getItem(int arg0) {
				return mWidgets.get(arg0);
			}

			@Override
			public long getItemId(int arg0) {
				return getItem(arg0).getId();
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				Entity item = getItem(position);

		        View v = View.inflate(mContext, android.R.layout.simple_list_item_activated_1, null);

		        TextView lTitle = (TextView)v.findViewById(android.R.id.text1);
		        lTitle.setText(item.getString("widgetid"));

		        return v;
			}

		}

	}