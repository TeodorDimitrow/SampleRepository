package com.example.teodordimitrov.sampleapplication.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.example.teodordimitrov.sampleapplication.R;
import com.example.teodordimitrov.sampleapplication.application.BaseApplication;
import com.example.teodordimitrov.sampleapplication.managers.SharedPreferencesManager;
import com.example.teodordimitrov.sampleapplication.util.StringUtils;
import com.example.teodordimitrov.sampleapplication.util.ValidationUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * LoginActivity representing the login screen.
 *
 * @author teodor.dimitrov
 */
public class LoginActivity extends Activity {

	private final int TEXT_VIEW_DRAWABLE_RIGHT_POSITION = 2;

	@BindView (R.id.login_edit_text_email)
	protected EditText emailEditText;

	@BindView (R.id.login_edit_text_password)
	protected EditText passwordEditText;

	@BindView (R.id.login_check_box_remember_me)
	protected CheckBox rememberMeCheckBox;

	@BindView (R.id.login_email_underline)
	protected View emailUnderlineView;

	@BindView (R.id.login_password_underline)
	protected View passwordUnderlineView;

	@Inject
	protected SharedPreferencesManager sharedPreferencesManager;

	private boolean isPasswordVisible;
	private boolean isEmailUnderlineRed;
	private boolean isPasswordUnderlineRed;
	private boolean hasUserLoggedOut;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		baseActivitySetup();
		checkForLogout();
		setListeners();
	}

	private void baseActivitySetup () {
		BaseApplication.getBaseComponent().inject(this);
		ButterKnife.bind(this);
	}

	private void checkForLogout () {
		Intent intent = getIntent();
		if (intent.hasExtra(InstrumentsActivity.EXTRA_HAS_LOGGED_OUT)) {
			hasUserLoggedOut = intent.getBooleanExtra(InstrumentsActivity.EXTRA_HAS_LOGGED_OUT, false);
		}
		if (!hasUserLoggedOut) {
			checkForStoredUser();
		} else {
			sharedPreferencesManager.clearCredentials();
		}
	}

	private void checkForStoredUser () {
		String email = sharedPreferencesManager.getStoredEmail();
		String password = sharedPreferencesManager.getStoredPassword();
		if (!email.isEmpty() && !password.isEmpty()) {
			startInstrumentActivity();
		}
	}

	private void startInstrumentActivity () {
		Intent intent = new Intent(LoginActivity.this, InstrumentsActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.enter_transition, R.anim.exit_transition);
		finish();
	}

	@SuppressLint ("ClickableViewAccessibility")
	private void setListeners () {
		emailEditText.addTextChangedListener(emailTextChangeListener);
		emailEditText.setOnTouchListener(emailTouchListener);
		passwordEditText.addTextChangedListener(passwordTextChangeListener);
		passwordEditText.setOnTouchListener(passwordTouchListener);
		rememberMeCheckBox.setOnCheckedChangeListener(checkBoxOnCheckListener);
	}

	@OnClick (R.id.login_button)
	public void login (View view) {
		String email = emailEditText.getText().toString();
		String password = passwordEditText.getText().toString();

		if (!areCredentialsValid(email, password)) {
			return;
		}
		if (rememberMeCheckBox.isChecked()) {
			sharedPreferencesManager.saveUserCredentials(email, password);
		}
		startInstrumentActivity();
	}

	private boolean areCredentialsValid (String email, String password) {
		if (!ValidationUtils.isEmailValid(email)) {
			emailEditText.requestFocus();
			Toast.makeText(this, getString(R.string.error_invalid_email), Toast.LENGTH_SHORT).show();
			emailUnderlineView.setBackgroundColor(Color.RED);
			isEmailUnderlineRed = true;
			return false;
		}
		if (!ValidationUtils.isPasswordValid(password)) {
			Toast.makeText(this, getString(R.string.error_invalid_password), Toast.LENGTH_SHORT).show();
			passwordUnderlineView.setBackgroundColor(Color.RED);
			return false;
		}

		return true;
	}

	//TODO Smooth at tested devices. Check android profiler for more performance info.
	private TextWatcher emailTextChangeListener = new TextWatcher() {
		@Override
		public void beforeTextChanged (CharSequence s, int start, int count, int after) {
			//No implementation needed.
		}

		@Override
		public void onTextChanged (CharSequence s, int start, int before, int count) {
			String input = s.toString();

			if (isEmailUnderlineRed) {
				emailUnderlineView.setBackgroundColor(Color.WHITE);
				isEmailUnderlineRed = false;
			}
			if (input.length() == 1) {
				emailEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_email, 0, R.drawable.ic_clear, 0);
			}
			if (input.isEmpty()) {
				emailEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_email, 0, 0, 0);
			}
		}

		@Override
		public void afterTextChanged (Editable s) {
			//No implementation needed.
		}
	};

	private TextWatcher passwordTextChangeListener = new TextWatcher() {
		@Override
		public void beforeTextChanged (CharSequence s, int start, int count, int after) {
			//No implementation needed.
		}

		@Override
		public void onTextChanged (CharSequence s, int start, int before, int count) {
			String input = s.toString();
			if (isPasswordUnderlineRed) {
				passwordUnderlineView.setBackgroundColor(Color.WHITE);
				isPasswordUnderlineRed = false;
			}
			if (input.length() == 1) {
				if (passwordEditText.getCompoundDrawables()[TEXT_VIEW_DRAWABLE_RIGHT_POSITION] == null) {
					passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_visible, 0);
				}
			}
			if (input.isEmpty()) {
				passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, 0, 0);
			}
		}

		@Override
		public void afterTextChanged (Editable s) {
			//No implementation needed.
		}
	};

	private View.OnTouchListener emailTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch (View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				v.performClick();
				if (emailEditText.getCompoundDrawables()[TEXT_VIEW_DRAWABLE_RIGHT_POSITION] != null) {
					passwordEditText.clearFocus();
					if (event.getRawX() >= (emailEditText.getRight() - emailEditText.getCompoundDrawables()[TEXT_VIEW_DRAWABLE_RIGHT_POSITION].getBounds().width())) {
						emailEditText.setText(StringUtils.EMPTY);
						emailEditText.performClick();
						return false;
					}
				}
			} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
				emailEditText.performClick();
			}
			return false;
		}
	};

	//TODO research if it recreates the whole view.
	private View.OnTouchListener passwordTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch (View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				v.performClick();
				if (passwordEditText.getCompoundDrawables()[TEXT_VIEW_DRAWABLE_RIGHT_POSITION] != null) {
					if (event.getRawX() >=
							(passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[TEXT_VIEW_DRAWABLE_RIGHT_POSITION]
									.getBounds().width())) {

						emailEditText.clearFocus();
						if (isPasswordVisible) {
							passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_visible, 0);
							passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
							isPasswordVisible = false;
						} else {
							passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_invisible, 0);
							passwordEditText.setTransformationMethod(null);
							isPasswordVisible = true;
						}

						return false;
					}
				}
			}
			return false;
		}
	};

	private CompoundButton.OnCheckedChangeListener checkBoxOnCheckListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged (CompoundButton buttonView, boolean isChecked) {
			if (isChecked) {
				emailEditText.clearFocus();
				passwordEditText.clearFocus();
			}
		}
	};

}