package com.example.teodordimitrov.sampleapplication.dagger;

import com.example.teodordimitrov.sampleapplication.activities.LoginActivity;

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
}