package com.sloy.photowidget;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;

public class PhotoWidgetActivity extends Activity {

	public static enum TabType {
		FUENTES(FuentesListFragment.class.getName()), WIDGETS(WidgetListFragment.class.getName());

		private String mName;

		TabType(String classname) {
			mName = classname;
		}

		public String getClassName() {
			return mName;
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		final ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
		bar.addTab(bar.newTab().setText("Fuentes").setTabListener(new TabListener(this, "fuentes", TabType.FUENTES)));
		bar.addTab(bar.newTab().setText("Widgets").setTabListener(new TabListener(this, "widgets", TabType.WIDGETS)));
	}

	public class TabListener implements ActionBar.TabListener {
		private Fragment mFragment;
		private Activity mActivity;
		private final String mTag;
		private final TabType mClass;
		private final Bundle mArgs;

		public TabListener(Activity activity, String tag, TabType clz) {
			this(activity, tag, clz, null);
		}

		public TabListener(Activity activity, String tag, TabType clz, Bundle args) {
			mActivity = activity;
			mTag = tag;
			mClass = clz;
			mArgs = args;

			// Check to see if we already have a fragment for this tab, probably
			// from a previously saved state. If so, deactivate it, because our
			// initial state is that a tab isn't shown.
			mFragment = mActivity.getFragmentManager().findFragmentByTag(mTag);
			if(mFragment != null && !mFragment.isDetached()){
				FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
				ft.detach(mFragment);
				ft.commit();
			}
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			if(mFragment == null){
				mFragment = Fragment.instantiate(mActivity, mClass.getClassName(), mArgs);
				ft.add(R.id.titles, mFragment, mTag);
			}else{
				ft.attach(mFragment);
			}

		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			if(mFragment != null){
				ft.detach(mFragment);
			}
		}

	}

	public static class DetailsActivity extends Activity {

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
				// If the screen is now in landscape mode, we can show the
				// dialog in-line with the list so we don't need this activity.
				finish();
				return;
			}

			if(savedInstanceState == null){
				// During initial setup, plug in the details fragment.
				ConfigCarpetaFragment details = new ConfigCarpetaFragment();
				details.setArguments(getIntent().getExtras());
				getFragmentManager().beginTransaction().add(android.R.id.content, details).commit();
			}
		}
	}

}