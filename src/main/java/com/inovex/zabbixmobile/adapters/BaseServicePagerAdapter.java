/*
This file is part of ZAX.

	ZAX is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	ZAX is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with ZAX.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.inovex.zabbixmobile.adapters;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;
import java.util.TreeSet;

/**
 * Base class for a pager adapter maintained by a service. The base
 * functionality is similar to {@link FragmentPagerAdapter}.
 *
 * @param <T>
 *            class of the items in this adapter's data set
 */
public abstract class BaseServicePagerAdapter<T> extends PagerAdapter {

	protected TreeSet<T> mObjects;
	private static final String TAG = BaseServicePagerAdapter.class
			.getSimpleName();
	private static final boolean DEBUG = true;
	protected int mCurrentPosition;
	protected FragmentManager mFragmentManager;
	protected FragmentTransaction mCurTransaction = null;
	private Fragment mCurrentPrimaryItem = null;
	private boolean mRefresh;

	public BaseServicePagerAdapter() {
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
		if (mFragmentManager == null)
			return null;
		if (mCurTransaction == null) {
			mCurTransaction = mFragmentManager.beginTransaction();
		}

		final Long itemId = getItemId(position);
		if (itemId == null)
			return null;

		// Do we already have this fragment?
		String name = makeFragmentName(container.getId(), itemId);
		Fragment fragment = mFragmentManager.findFragmentByTag(name);
		if (fragment != null && !mRefresh) {
			if (DEBUG)
				Log.v(TAG, "Attaching item #" + itemId + ": f=" + fragment);
			mCurTransaction.attach(fragment);
		} else {
			fragment = getItem(position);
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
		// if the fragment was detached, the fragment manager should be null
		if (mFragmentManager == null)
			return;
		if (mCurTransaction == null) {
			mCurTransaction = mFragmentManager.beginTransaction();
		}
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
		if (mFragmentManager == null)
			return;
		if (mCurTransaction != null) {
			try {
				mCurTransaction.commitAllowingStateLoss();
				mCurTransaction = null;
				// Commenting out the following two lines fixes a
				// "Recursive entry to executePendingTransactions" exception
				// I don't know why and hope this doesn't break anything
//				if (mFragmentManager != null)
//					mFragmentManager.executePendingTransactions();
			} catch (IllegalStateException e) {
				// this exception is thrown if the activity has been destroyed
				// which unfortunately happens from time to time
				e.printStackTrace();
				mCurTransaction = null;
			}
		}
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		if (object == null || view == null)
			return false;
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
	public abstract Long getItemId(int position);

	protected static String makeFragmentName(int viewId, long id) {
		return "android:switcher:" + viewId + ":" + id;
	}

	/**
	 * Creates a page (fragment) for a certain position.
	 *
	 * @param position
	 *            the position within the adapter
	 * @return the created fragment
	 */
	protected abstract Fragment getItem(int position);

	@Override
	public int getCount() {
		return mObjects.size();
	}

	/**
	 * Wrapper for {@link TreeSet#addAll(Collection)}.
	 *
	 * @param objects
	 */
	public void addAll(Collection<? extends T> objects) {
		this.mObjects.addAll(objects);
	}

	/**
	 * Returns an item from the underlying data set.
	 *
	 * @param position
	 *            position of the item
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T getObject(int position) {
		if (position >= mObjects.size())
			return null;
		return (T) mObjects.toArray()[position];
	}

	/**
	 * Returns the current item.
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T getCurrentObject() {
		if (mObjects.size() <= mCurrentPosition) {
			if (mObjects.size() > 0)
				return (T) mObjects.toArray()[0];
			return null;
		}
		return (T) mObjects.toArray()[mCurrentPosition];
	}

	/**
	 * Wrapper for {@link TreeSet#clear()}.
	 */
	public void clear() {
		mObjects.clear();
		mRefresh = true;
	}

	public int getCurrentPosition() {
		return mCurrentPosition;
	}

	public void setCurrentPosition(int position) {
		this.mCurrentPosition = position;
	}

}
