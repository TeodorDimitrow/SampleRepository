package com.example.teodordimitrov.sampleapplication.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.example.teodordimitrov.sampleapplication.R;
import com.example.teodordimitrov.sampleapplication.application.BaseApplication;
import com.example.teodordimitrov.sampleapplication.util.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

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

	private boolean isPasswordVisible;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		BaseApplication.getBaseComponent().inject(this);
		ButterKnife.bind(this);
		setListeners();
	}

	private void setListeners () {
		emailEditText.addTextChangedListener(emailTextChangeListener);
		emailEditText.setOnTouchListener(emailTouchListener);
		passwordEditText.addTextChangedListener(passwordTextChangeListener);
		passwordEditText.setOnTouchListener(passwordTouchListener);
		rememberMeCheckBox.setOnCheckedChangeListener(checkBoxOnCheckListener);
	}

	//TODO Smooth at tested devices. Check android profiler for more performance info.
	private TextWatcher emailTextChangeListener = new TextWatcher() {
		@Override
		public void beforeTextChanged (CharSequence s, int start, int count, int after) {
			//No implementation needed.
		}

		@Override
		public void onTextChanged (CharSequence s, int start, int before, int count) {
			if (s.length() == 1) {
				emailEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_email, 0, R.drawable.ic_clear, 0);
			}
			if (s.length() == 0) {
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
			if (s.length() == 1) {
				if (passwordEditText.getCompoundDrawables()[TEXT_VIEW_DRAWABLE_RIGHT_POSITION] == null) {
					passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_visible, 0);
				}
			}
			if (s.length() == 0) {
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
				if (emailEditText.getCompoundDrawables()[TEXT_VIEW_DRAWABLE_RIGHT_POSITION] != null) {
					if (event.getRawX() >= (emailEditText.getRight() - emailEditText.getCompoundDrawables()[TEXT_VIEW_DRAWABLE_RIGHT_POSITION].getBounds().width())) {
						emailEditText.setText(StringUtils.EMPTY);

						return true;
					}
				}
			}
			return false;
		}
	};

	//TODO research if it recreates the whole view.
	private View.OnTouchListener passwordTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch (View v, MotionEvent event) {

			if (event.getAction() == MotionEvent.ACTION_UP) {
				if (passwordEditText.getCompoundDrawables()[TEXT_VIEW_DRAWABLE_RIGHT_POSITION] != null) {
					if (event.getRawX() >= (passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[TEXT_VIEW_DRAWABLE_RIGHT_POSITION].getBounds().width())) {

						int selectionStart = passwordEditText.getSelectionStart();
						int selectionEnd = passwordEditText.getSelectionEnd();
						if (isPasswordVisible) {
							passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_visible, 0);
							passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
							isPasswordVisible = false;
						} else {
							passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_invisible, 0);
							passwordEditText.setTransformationMethod(null);
							isPasswordVisible = true;
						}
						passwordEditText.setSelection(selectionStart, selectionEnd);

						return true;
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