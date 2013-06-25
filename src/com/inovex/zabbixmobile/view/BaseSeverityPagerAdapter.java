package com.inovex.zabbixmobile.view;

import java.util.Collection;
import java.util.TreeSet;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.inovex.zabbixmobile.activities.fragments.OnListItemSelectedListener;
import com.inovex.zabbixmobile.model.TriggerSeverity;

public abstract class BaseSeverityPagerAdapter<T> extends PagerAdapter
		implements OnListItemSelectedListener {
	private TreeSet<T> mObjects;
	private static final String TAG = BaseSeverityPagerAdapter.class
			.getSimpleName();
	private static final boolean DEBUG = false;

	private FragmentManager mFragmentManager;
	private FragmentTransaction mCurTransaction = null;

	private Fragment mCurrentPrimaryItem = null;

	public BaseSeverityPagerAdapter(TriggerSeverity severity) {
		this(null, severity);
	}

	public BaseSeverityPagerAdapter(FragmentManager fm, TriggerSeverity severity) {
		mFragmentManager = fm;
		Log.d(TAG, "creating SeverityPagerAdapter for severity " + severity);

		mObjects = new TreeSet<T>();
	}

	public void setFragmentManager(FragmentManager fm) {
		this.mFragmentManager = fm;
	}

	@Override
	public void startUpdate(ViewGroup container) {
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		if (mCurTransaction == null) {
			mCurTransaction = mFragmentManager.beginTransaction();
		}

		final long itemId = getItemId(position);

		// Do we already have this fragment?
		String name = makeFragmentName(container.getId(), itemId);
		Fragment fragment = mFragmentManager.findFragmentByTag(name);
		if (fragment != null) {
			if (DEBUG)
				Log.v(TAG, "Attaching item #" + itemId + ": f=" + fragment);
			mCurTransaction.attach(fragment);
		} else {
			fragment = getPage(position);
			if (DEBUG)
				Log.v(TAG, "Adding item #" + itemId + ": f=" + fragment);
			mCurTransaction.add(container.getId(), fragment,
					makeFragmentName(container.getId(), itemId));
		}
		if (fragment != mCurrentPrimaryItem) {
			fragment.setMenuVisibility(false);
			fragment.setUserVisibleHint(false);
		}

		return fragment;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		if (mCurTransaction == null) {
			mCurTransaction = mFragmentManager.beginTransaction();
		}
		if (DEBUG)
			Log.v(TAG, "Detaching item #" + getItemId(position) + ": f="
					+ object + " v=" + ((Fragment) object).getView());
		mCurTransaction.detach((Fragment) object);
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		Fragment fragment = (Fragment) object;
		if (fragment != mCurrentPrimaryItem) {
			if (mCurrentPrimaryItem != null) {
				mCurrentPrimaryItem.setMenuVisibility(false);
				mCurrentPrimaryItem.setUserVisibleHint(false);
			}
			if (fragment != null) {
				fragment.setMenuVisibility(true);
				fragment.setUserVisibleHint(true);
			}
			mCurrentPrimaryItem = fragment;
		}
	}

	@Override
	public void finishUpdate(ViewGroup container) {
		if (mCurTransaction != null) {
			mCurTransaction.commitAllowingStateLoss();
			mCurTransaction = null;
			mFragmentManager.executePendingTransactions();
		}
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return ((Fragment) object).getView() == view;
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
	}

	/**
	 * Return a unique identifier for the item at the given position.
	 * 
	 * <p>
	 * The default implementation returns the given position. Subclasses should
	 * override this method if the positions of items can change.
	 * </p>
	 * 
	 * @param position
	 *            Position within this adapter
	 * @return Unique identifier for the item at position
	 */
	public long getItemId(int position) {
		return position;
	}

	private static String makeFragmentName(int viewId, long id) {
		return "android:switcher:" + viewId + ":" + id;
	}

	/**
	 * Creates a page (fragment) for a certain position.
	 * 
	 * @param position
	 *            the position within the adapter
	 * @return the created fragment
	 */
	protected abstract Fragment getPage(int position);

	@Override
	public int getCount() {
		return mObjects.size();
	}

	@Override
	public void onListItemSelected(int position, TriggerSeverity severity,
			long id) {
		// TODO Auto-generated method stub

	}

	public void addAll(Collection<? extends T> objects) {
		this.mObjects.addAll(objects);
	}

	@SuppressWarnings("unchecked")
	public T getItem(int position) {
		return (T) mObjects.toArray()[position];
	}

	/**
	 * Clears the collection of events.
	 */
	public void clear() {
		mObjects.clear();
	}
}
