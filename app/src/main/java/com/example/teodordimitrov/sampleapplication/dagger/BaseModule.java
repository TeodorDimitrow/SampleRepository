package com.example.teodordimitrov.sampleapplication.dagger;

import android.app.Application;

import com.example.teodordimitrov.sampleapplication.managers.SharedPreferencesManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger Base Module.
 *
 * @author teodor.dimitrov on 17.3.2018 г..
 */
@Module
public class BaseModule {

	private Application application;

	public BaseModule (Application application) {
		this.application = application;
	}

	@Provides
	@Singleton
	SharedPreferencesManager providesSharedPreferencesManager () {
		return new SharedPreferencesManager(application);
	}

	@Provides
	@Singleton
	Gson providesGson () {
		return new GsonBuilder().create();
	}

}