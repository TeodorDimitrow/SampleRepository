package com.example.teodordimitrov.sampleapplication.dagger;

import com.example.teodordimitrov.sampleapplication.activities.LoginActivity;
import com.example.teodordimitrov.sampleapplication.activities.MainActivity;
import com.example.teodordimitrov.sampleapplication.fragments.InstrumentsFragment;
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

	void inject (MainActivity mainActivity);

	void inject (InstrumentProvider instrumentProvider);

	void inject (InstrumentsFragment instrumentsFragment);

	void inject (LoginActivity loginActivity);
}