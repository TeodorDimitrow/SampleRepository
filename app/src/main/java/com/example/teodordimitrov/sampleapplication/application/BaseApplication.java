package com.example.teodordimitrov.sampleapplication.application;

import android.app.Application;
import android.content.Context;

import com.example.teodordimitrov.sampleapplication.dagger.BaseComponent;
import com.example.teodordimitrov.sampleapplication.dagger.BaseModule;
import com.example.teodordimitrov.sampleapplication.dagger.DaggerBaseComponent;

/**
 * BaseApplication class.
 *
 * @author teodor.dimitrov on 17.3.2018 г..
 */

public class BaseApplication extends Application {

	private static Context context;
	private static BaseComponent baseComponent;

	@Override
	public void onCreate () {
		super.onCreate();

		BaseApplication.context = getApplicationContext();

		baseComponent = DaggerBaseComponent.builder()
				.baseModule(new BaseModule(this))
				.build();

	}

	public static Context getContext () {
		return BaseApplication.context;
	}

	public static BaseComponent getBaseComponent () {
		return baseComponent;
	}

}
