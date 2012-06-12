package com.inovex.zabbixmobile.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.inovex.zabbixmobile.R;

public class BaseDetailsFragment extends Fragment {
	private Animation animationFadeIn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		animationFadeIn = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.loading_view, container);
	}

	protected void showLayout(int layoutRes) {
		ViewGroup root = (ViewGroup) getView();
		root.removeAllViews();
		getActivity().getLayoutInflater().inflate(layoutRes, root);
		root.startAnimation(animationFadeIn);
	}

	protected void showLoadingLayout() {
		showLayout(R.layout.loading_view);
	}
}

