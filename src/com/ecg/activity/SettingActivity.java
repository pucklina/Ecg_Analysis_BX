package com.ecg.activity;

import com.ecg_analysis.R;
import android.app.Activity;
import android.os.Bundle;

public class SettingActivity extends Activity{
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acitvity_set);
	}
	public void onDestroy(){ 
		super.onDestroy();  
	}  
}