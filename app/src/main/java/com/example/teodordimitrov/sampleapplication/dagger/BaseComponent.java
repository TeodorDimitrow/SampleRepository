package com.example.teodordimitrov.sampleapplication.dagger;

import com.example.teodordimitrov.sampleapplication.activities.InstrumentsActivity;
import com.example.teodordimitrov.sampleapplication.activities.LoginActivity;
import com.example.teodordimitrov.sampleapplication.providers.InstrumentProvider;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Classes which will use injection members are defined here.
 *
 * @author teodor.dimitrov on 17.3.2018 г..
 */
@Singleton
@Component (modules = {BaseModule.class})
public interface BaseComponent {

	void inject (LoginActivity loginActivity);

	void inject (InstrumentsActivity instrumentsActivity);

	void inject (InstrumentProvider instrumentProvider);

}