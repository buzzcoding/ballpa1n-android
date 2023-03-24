package com.llsc12.ballpa1n;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.llsc12.ballpa1n.databinding.RespringFragBinding;

import pl.droidsonroids.gif.GifImageView;

public class Respring extends Fragment {

	private RespringFragBinding binding;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = RespringFragBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		//super.onViewCreated(view, savedInstanceState);
		GifImageView respring = getActivity().findViewById(R.id.respring);
		respring.setImageResource(R.drawable.respring);

		new Handler().postDelayed(() -> {
			Intent startMain = new Intent(Intent.ACTION_MAIN);
			startMain.addCategory(Intent.CATEGORY_HOME);
			startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(startMain);
			System.exit(0);
		}, (int) (jelbraik.randomDouble(0.2, 3) * 1000));
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

}