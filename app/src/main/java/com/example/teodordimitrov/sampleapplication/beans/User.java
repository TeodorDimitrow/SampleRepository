package com.example.teodordimitrov.sampleapplication.beans;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * User bean representing the current user logged in the application.
 *
 * @author teodor.dimitrov on 18.3.2018 г..
 */

public class User {

	@SerializedName ("userInstrumentsIdsList")
	private List<Long> userInstrumentsIdsList;

	public List<Long> getUserInstrumentsIdsList () {
		return userInstrumentsIdsList;
	}

	public void setUserInstrumentsIdsList (List<Long> userInstrumentsIdsList) {
		this.userInstrumentsIdsList = userInstrumentsIdsList;
	}
}
