package com.implementhit.OptimizeHIT.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.implementhit.OptimizeHIT.OptimizeHIT;
import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.analytics.GAnalitycsEventNames;
import com.implementhit.OptimizeHIT.analytics.GAnalyticsScreenNames;
import com.implementhit.OptimizeHIT.database.DBTalker;
import com.implementhit.OptimizeHIT.database.ICDDatabase;
import com.implementhit.OptimizeHIT.dialogs.LoginWelcomeDialog;
import com.implementhit.OptimizeHIT.gcm.GCMSubscriber;
import com.implementhit.OptimizeHIT.util.CancelableTextWatcher;
import com.implementhit.OptimizeHIT.util.ClearButtonClickListener;
import com.implementhit.OptimizeHIT.util.ColorUtil;
import com.implementhit.OptimizeHIT.util.CustomEditText;
import com.implementhit.OptimizeHIT.util.NotificationHelper;
import com.implementhit.OptimizeHIT.util.FontsHelper;
import com.implementhit.OptimizeHIT.util.HelperConstants;
import com.implementhit.OptimizeHIT.util.Locker;
import com.implementhit.OptimizeHIT.models.Notification;
import com.implementhit.OptimizeHIT.models.User;
import com.implementhit.OptimizeHIT.api.APITalker;
import com.implementhit.OptimizeHIT.api.ChangePasswordRequestListener;
import com.implementhit.OptimizeHIT.api.NotificationsRequestListener;
import com.implementhit.OptimizeHIT.api.OnCheckDomainListener;
import com.implementhit.OptimizeHIT.api.SessionEstablishRequestListener;
import com.implementhit.OptimizeHIT.api.TalkersConstants;

import java.io.IOException;
import java.util.ArrayList;

public class LoginActivity extends SuperActivity implements SessionEstablishRequestListener, OnEditorActionListener,
        ChangePasswordRequestListener, NotificationsRequestListener, OnCheckDomainListener {

	private static final String PASSWORD = "password";
	private static final String ALREADY_TRIED = "alreadyTried";
	private static final String EMAIL = "email";
	private static final String DOMAIN = "domain";
	private static final String KEYBOARD_VISIBLE = "keyboardVisible";
	private static final String LAST_FOCUSED_VIEW_ID = "lastFocusedViewId";
	private static final String CURSOR_POSITION = "cursorPosition";
	private static final String SCREEN_ID = "screenId";

	private String password = "";
	private User user;
	private APITalker apiTalker;
	private LoginWelcomeDialog loginWelcomeDialog;
	private boolean alreadyTried = false;

	private boolean dispatchWithLogin = false;
	private boolean isKeyboardVisible = false;
	private boolean requestInProcess = false;

	private String emailSaved;
	private String domainSaved;

	private int savedLastFocusedViewId;
	private int savedCursorPosition;
	private int screenId; // 0 - domain check, 1 - enter username, 2 - forgot password

	private CustomEditText domainCustomEditText;
	private CustomEditText usernameCustomEditText;
	private CustomEditText passwordCustomEditText;
	private TextView tapToChangeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        user = User.sharedUser(this);
        apiTalker = APITalker.sharedTalker();

        OptimizeHIT.sendScreen(GAnalyticsScreenNames.LOGIN_SCREEN, null, null, null);

        setContentView(R.layout.activity_login);

        tapToChangeTextView = (TextView) findViewById(R.id.tap_to_change);

        tapToChangeTextView.setOnClickListener(tapToChangeClickListener);


		domainCustomEditText = (CustomEditText) findViewById(R.id.domain);
		usernameCustomEditText = (CustomEditText) findViewById(R.id.email);
		passwordCustomEditText = (CustomEditText) findViewById(R.id.password);
		Button cancelForDomainTextView = (Button) findViewById(R.id.cancel_for_domain);
		Button cancelForUsernameTextView = (Button) findViewById(R.id.cancel_for_email);
		Button cancelForPasswordTextView = (Button) findViewById(R.id.cancel_for_password);

		domainCustomEditText.setOnEditorActionListener(this);
		usernameCustomEditText.setOnEditorActionListener(this);
		passwordCustomEditText.setOnEditorActionListener(this);

		usernameCustomEditText.setOnFocusChangeListener(focusChangeListener);
		domainCustomEditText.setOnFocusChangeListener(focusChangeListener);
		passwordCustomEditText.setOnFocusChangeListener(focusChangeListener);

		usernameCustomEditText.setFocusable(true);
		usernameCustomEditText.setFocusableInTouchMode(true);
		domainCustomEditText.setFocusable(true);
		domainCustomEditText.setFocusableInTouchMode(true);
		passwordCustomEditText.setFocusable(true);
		passwordCustomEditText.setFocusableInTouchMode(true);

		/**
		 *  Set listeners to indicate back button press when one of CustomEditTexts is focused ( and keyboard is opened ).
		 *  It needed in order to make all the CustomEditTexts in activity not focusable.
		 */

		usernameCustomEditText.setOnHideKeyboardListener(hideKeyboardListener);
		domainCustomEditText.setOnHideKeyboardListener(hideKeyboardListener);
		passwordCustomEditText.setOnHideKeyboardListener(hideKeyboardListener);

        FontsHelper fontsHelper = FontsHelper.sharedHelper(this);
        cancelForDomainTextView.setTypeface(fontsHelper.fontello());
        cancelForUsernameTextView.setTypeface(fontsHelper.fontello());
        cancelForPasswordTextView.setTypeface(fontsHelper.fontello());


		domainCustomEditText.addTextChangedListener(new CancelableTextWatcher(cancelForDomainTextView, domainCustomEditText));
		usernameCustomEditText.addTextChangedListener(new CancelableTextWatcher(cancelForUsernameTextView, usernameCustomEditText));
		passwordCustomEditText.addTextChangedListener(new CancelableTextWatcher(cancelForPasswordTextView, passwordCustomEditText));

        cancelForDomainTextView.setOnClickListener(new ClearButtonClickListener(domainCustomEditText));
        cancelForUsernameTextView.setOnClickListener(new ClearButtonClickListener(usernameCustomEditText));
        cancelForPasswordTextView.setOnClickListener(new ClearButtonClickListener(passwordCustomEditText));


		if (user.username().length() > 0) {
			usernameCustomEditText.setText(user.username());
		}
		if (user.domain().length() > 0) {
			domainCustomEditText.setText(user.domain());
		}

        savedLastFocusedViewId = R.id.logo;
        savedCursorPosition = 0;
        screenId = 0;

		if (getIntent().getBooleanExtra(SuperActivity.FROM_LOG_OUT, false)) {
			screenId = 1;
		} else {
			screenId = 0;
		}

        if (savedInstanceState != null) {
            password = savedInstanceState.getString(PASSWORD);
            alreadyTried = savedInstanceState.getBoolean(ALREADY_TRIED);
            emailSaved = savedInstanceState.getString(EMAIL);
            domainSaved = savedInstanceState.getString(DOMAIN);
            screenId = savedInstanceState.getInt(SCREEN_ID);

			usernameCustomEditText.setText(emailSaved);
			domainCustomEditText.setText(domainSaved);

            if (savedInstanceState.getBoolean(KEYBOARD_VISIBLE)) {
                isKeyboardVisible = savedInstanceState.getBoolean(KEYBOARD_VISIBLE);
                savedLastFocusedViewId = savedInstanceState.getInt(LAST_FOCUSED_VIEW_ID);
                savedCursorPosition = savedInstanceState.getInt(CURSOR_POSITION, 0);
            }
        }

        switchScreen();

        if (getIntent().getBooleanExtra(SuperActivity.IS_STARTED_FOR_HASH_EXPIRE, false)) {
            NotificationHelper.showNotification(TalkersConstants.HASH_INVALID, this);
            getIntent().putExtra(SuperActivity.IS_STARTED_FOR_HASH_EXPIRE, false);
        }
    }

    @Override
    protected void refreshStateAfterLogin(boolean isInitialLogin) {
    }

    View.OnClickListener tapToChangeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onLogoClicked(null);
        }
    };

    OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {

        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (view instanceof CustomEditText && hasFocus) {
                if (savedLastFocusedViewId == R.id.email) {
                    findViewById(R.id.cancel_for_email).setVisibility(View.GONE);
                } else if (savedLastFocusedViewId == R.id.domain) {
                    findViewById(R.id.cancel_for_domain).setVisibility(View.GONE);
                } else if (savedLastFocusedViewId == R.id.password) {
                    findViewById(R.id.cancel_for_password).setVisibility(View.GONE);
                }

                CustomEditText edit = (CustomEditText) view;
                savedLastFocusedViewId = edit.getId();

                if (edit.getText().length() > 0) {
                    if (savedLastFocusedViewId == R.id.email) {
                        findViewById(R.id.cancel_for_email).setVisibility(View.VISIBLE);
                    } else if (savedLastFocusedViewId == R.id.domain) {
                        findViewById(R.id.cancel_for_domain).setVisibility(View.VISIBLE);
                    } else if (savedLastFocusedViewId == R.id.password) {
                        findViewById(R.id.cancel_for_password).setVisibility(View.VISIBLE);
                    }
                }
            } else if (view instanceof CustomEditText && !hasFocus) {
                if (savedLastFocusedViewId == R.id.email) {
                    findViewById(R.id.cancel_for_email).setVisibility(View.GONE);
                } else if (savedLastFocusedViewId == R.id.domain) {
                    findViewById(R.id.cancel_for_domain).setVisibility(View.GONE);
                } else if (savedLastFocusedViewId == R.id.password) {
                    findViewById(R.id.cancel_for_password).setVisibility(View.GONE);
                }
            }

//            ImageView poweredByImageView = (ImageView) findViewById(R.id.powered_by_image_view);
//
//            if (!poweredByImageView.hasFocus()) {
//                hideSystemUI();
//            } else {
//                showSystemUI();
//            }
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        password = passwordCustomEditText.getText().toString();

        View lastFocusedView = this.getCurrentFocus();

        if (lastFocusedView == null) {
            lastFocusedView = findViewById(R.id.logo);
        }
        int lastFocusedViewId = lastFocusedView.getId();

        int cursorPosition = -1;

        if (lastFocusedView instanceof CustomEditText) {
            CustomEditText lastFocusedCustomEditText = (CustomEditText) lastFocusedView;
            cursorPosition = lastFocusedCustomEditText.getSelectionStart();
        }

        outState.putString(PASSWORD, dispatchWithLogin ? "" : password);
        outState.putBoolean(ALREADY_TRIED, alreadyTried);
        outState.putString(DOMAIN, domainCustomEditText.getText().toString());
        outState.putString(EMAIL, usernameCustomEditText.getText().toString());
        outState.putBoolean(KEYBOARD_VISIBLE, isKeyboardVisible);
        outState.putInt(LAST_FOCUSED_VIEW_ID, lastFocusedViewId);
        outState.putInt(CURSOR_POSITION, cursorPosition);
        outState.putInt(SCREEN_ID, screenId);
    }

    @Override
    public void onBackPressed() {
        if (screenId == 2) {
            screenId = 1;
            switchScreen();
            return;
        } else if (screenId == 1) {
            screenId = 0;
            switchScreen();
            return;
        }

        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startActivity(startMain);

		View containingActivityRootView = findViewById(android.R.id.content);
		containingActivityRootView.requestFocus();
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onResume() {
		super.onResume();


		if (user.username().length() > 0) {
			usernameCustomEditText.setText(user.username());
		}
		if (user.domain().length() > 0) {
			domainCustomEditText.setText(user.domain());
		}
		if (getIntent().hasExtra(EMAIL)) {
			usernameCustomEditText.setText(getIntent().getStringExtra(EMAIL));
		}
		if (getIntent().hasExtra(DOMAIN)) {
			domainCustomEditText.setText(getIntent().getStringExtra(DOMAIN));
		}
		if (emailSaved != null && !emailSaved.isEmpty()) {
			usernameCustomEditText.setText(emailSaved);
		}
		if (domainSaved != null && !domainSaved.isEmpty()) {
			domainCustomEditText.setText(domainSaved);
		}

		password = dispatchWithLogin ? "" : password;
		dispatchWithLogin = false;

		passwordCustomEditText.setText(password);

		View savedLastFocusedPraView = findViewById(savedLastFocusedViewId);

		if (savedLastFocusedPraView instanceof CustomEditText && isKeyboardVisible) {
			CustomEditText savedLastFocusedView = (CustomEditText) savedLastFocusedPraView;
			showSoftKeyboard(savedLastFocusedView);

			if (savedCursorPosition >= 0
					&& savedCursorPosition <= savedLastFocusedView.getText()
					.length()) {
				savedLastFocusedView.setSelection(savedCursorPosition);
				savedLastFocusedView.setSelection(savedCursorPosition,
						savedCursorPosition);
			}
		} else {
			ImageView logoImage = (ImageView) findViewById(R.id.logo);
			logoImage.setFocusable(true);
			logoImage.setFocusableInTouchMode(true);
			logoImage.requestFocus();
		}

		isSoftKeyboardVisible();
	}

	@Override
	public void onPause() {
		getIntent().putExtra(DOMAIN, domainCustomEditText.getText().toString());
		getIntent().putExtra(EMAIL, usernameCustomEditText.getText().toString());

		if (loginWelcomeDialog != null) {
			loginWelcomeDialog.hide();
		}

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

	public void login(View view) {
		try {
			loginWelcomeDialog = new LoginWelcomeDialog(this);
			loginWelcomeDialog.show();
			loginWelcomeDialog.startIndeterminate();
		} catch (Exception e) {
			e.printStackTrace();
		}

        if (requestInProcess) {
            return;
        }

        if (System.currentTimeMillis() - savedLastClickTime < 200) {
            return;
        }

        savedLastClickTime = System.currentTimeMillis();
        requestInProcess = true;

        Locker.lock(this);

		String domainText = domainCustomEditText.getText().toString();
		String usernameText = usernameCustomEditText.getText().toString();
		String passwordText = passwordCustomEditText.getText().toString();


		if (screenId == 0) {
			if (domainText.isEmpty()) {
				loginWelcomeDialog.hide();
				NotificationHelper.showNotification(TalkersConstants.ALL_REQUIRED, this);

                Locker.unlock(this);

                requestInProcess = false;

                return;
            }

			loginWelcomeDialog.setForceDefaultColor(true);

			apiTalker.checkDomain(domainText, this);
			OptimizeHIT.sendEvent(
					GAnalitycsEventNames.DOMAIN_BUTTON_PRESSED.CATEGORY,
					GAnalitycsEventNames.DOMAIN_BUTTON_PRESSED.ACTION,
					GAnalitycsEventNames.DOMAIN_BUTTON_PRESSED.LABEL);
		} else if (screenId == 1) {
			if (usernameText.isEmpty()
					|| passwordText.isEmpty()) {
				loginWelcomeDialog.hide();
				NotificationHelper.showNotification(TalkersConstants.ALL_REQUIRED, this);

                Locker.unlock(this);

                requestInProcess = false;

                return;
            }


			apiTalker.login(usernameText, domainText, passwordText,
					Build.VERSION.RELEASE, this);
			OptimizeHIT.sendEvent(
					GAnalitycsEventNames.LOGIN_BUTTON_PRESSED.CATEGORY,
					GAnalitycsEventNames.LOGIN_BUTTON_PRESSED.ACTION,
					GAnalitycsEventNames.LOGIN_BUTTON_PRESSED.LABEL);
		} else {
			if (usernameText.isEmpty()
					|| domainText.isEmpty()) {
				loginWelcomeDialog.hide();
				NotificationHelper.showNotification(TalkersConstants.CHANGE_PASSWORD_FAILURE, this);

				Locker.unlock(this);

				requestInProcess = false;

				return;
			}

			apiTalker.changePassword(usernameText, domainText, Build.VERSION.RELEASE, this);
			OptimizeHIT.sendEvent(
					GAnalitycsEventNames.SUBMIT_RESET_BUTTON_PRESSED.CATEGORY,
					GAnalitycsEventNames.SUBMIT_RESET_BUTTON_PRESSED.ACTION,
					GAnalitycsEventNames.SUBMIT_RESET_BUTTON_PRESSED.LABEL);
		}

		hideKeyboard(this);
		isKeyboardVisible = false;
		ImageView logoImage = (ImageView) findViewById(R.id.logo);
		logoImage.setFocusable(true);
		logoImage.setFocusableInTouchMode(true);
		logoImage.requestFocus();
	}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        password = "";
        passwordCustomEditText.setText(password);
        if (requestCode == MenuActivity.REQUEST_CODE) {
            if (resultCode == HelperConstants.RESULT_HASH_EXPIRED) {
                NotificationHelper.showNotification(
                        getResources().getString(R.string.session_expired),
                        getResources().getString(
                                R.string.your_session_has_expired), true, this);
                user.logoutUser();
            } else if (resultCode == RESULT_OK) {
                finish();
            }
        }
    }

    /**
     * SessionHandler Methods
     */

    @Override
    public void onLoginSuccess(String username, String firstName, String lastName, String domain,
                               String domainLabel, String hash, long trigger, boolean voiceAccess,
                               boolean watsonAccess, boolean findACode) {

        final boolean needsDrop = !username.equals(user.username()) || !domain.equals(user.domain());

        user.saveUser(username, firstName, lastName, domain, domainLabel, hash,
                trigger, voiceAccess, watsonAccess,
                findACode);

		DBTalker dbTalker = DBTalker.sharedDB(this);

		if (needsDrop) {
			dbTalker.flushDatabase();
			ICDDatabase.sharedDatabase(LoginActivity.this).flushCodes();
		}

		dispatchWithLogin = true;
		password = "";

		savedLastFocusedViewId = R.id.logo;
		isKeyboardVisible = false;

		apiTalker.downloadData(this, user.hash(), needsDrop, dbTalker, null);
		apiTalker.getSuggestedLearning(user.hash(), getApplicationContext(), null);
		apiTalker.getPopularQuestions(user.hash(), getApplicationContext(), null);
		apiTalker.downloadSuperbill(user.hash(), ICDDatabase.sharedDatabase(this));
		apiTalker.getNotifications(getApplicationContext(), hash, null);
		apiTalker.getUserHistory(hash, dbTalker);
		apiTalker.getPeerFavorites(hash,dbTalker);

		try {
			GCMSubscriber.registerForGcm(getApplicationContext());
		} catch (Exception e) {
			e.printStackTrace();
		}

		processWelcomeAndDismissDialog();

		requestInProcess = false;
	}

    @Override
    public void onLoginFailure(String error) {
        proceedFails(error);

        requestInProcess = false;
    }

    @Override
    public void onSessionVerificationSuccess(long trigger, boolean voiceAccess,
                                             boolean watsonAccess, boolean findACode) {
        final boolean needsDbRefresh = user.updateTrigger(trigger);
        user.updatePermissions(voiceAccess, watsonAccess, findACode);

        if (needsDbRefresh) {
            apiTalker.downloadData(this, user.hash(), false, DBTalker.sharedDB(LoginActivity.this), null);
        }

        apiTalker.getSuggestedLearning(user.hash(), getApplicationContext(), null);
        apiTalker.getNotifications(getApplicationContext(), user.hash(), null);
        apiTalker.getPopularQuestions(user.hash(), getApplicationContext(), null);

        proceed();

        try {
            GCMSubscriber.registerForGcm(getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSessionVerificationFailure(String error) {
        proceedFails(error);
    }

    /**
     * OnEditorActionListener Methods
     */

    @Override
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            login(findViewById(R.id.login_button));

            return true;
        }

		if (event == null || event.getKeyCode() == KeyEvent.KEYCODE_NAVIGATE_NEXT) {
			if (view.getId() == R.id.domain) {
				usernameCustomEditText.setFocusableInTouchMode(true);
				usernameCustomEditText.setFocusable(true);
				usernameCustomEditText.requestFocus();
			} else if(view.getId() == R.id.email) {
				passwordCustomEditText.setFocusableInTouchMode(true);
				passwordCustomEditText.setFocusable(true);
				passwordCustomEditText.requestFocus();
			}
		}

        return false;
    }

	public void isSoftKeyboardVisible() {
		final View activityRootView = findViewById(R.id.login_scroll);
		activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						Rect r = new Rect();
						activityRootView.getWindowVisibleDisplayFrame(r);

						int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);

						if (heightDiff > 70) {
							isKeyboardVisible = true;
							View view = findViewById(savedLastFocusedViewId);

							if (view != null) {
								view.requestFocus();
							}
						} else {
							isKeyboardVisible = false;
							ImageView logoImage = (ImageView) findViewById(R.id.logo);
							logoImage.setFocusable(true);
							logoImage.setFocusableInTouchMode(true);
							logoImage.requestFocus();
						}
					}
				});
	}

	public void showSoftKeyboard(final View view) {
		final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		view.postDelayed(new Runnable() {
			@Override
			public void run() {
				view.requestFocus();
				imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
			}
		}, 100);
	}

	public void hideKeyboard(Activity activity) {
		InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		// Find the currently focused view, so we can grab the correct window
		// token from it.
		View view = activity.getCurrentFocus();
		// If no view currently has focus, create a new one, just so we can grab
		// a window token from it
		if (view == null) {
			view = new View(activity);
		}

		if( view instanceof CustomEditText ){
			// Removes focus from all EditTexts in the screen.
			hideKeyboardListener.onHideKeyboard();
		}

        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Helpers
     */

    private void proceed() {
        ActivityOptions options = ActivityOptions.makeCustomAnimation(this, 0, R.anim.slide_down_out);

        if (getIntent().getBooleanExtra(SuperActivity.RETURN_BACK, false)) {
            apiTalker.getNotifications(getApplicationContext(), user.hash(), null);
            finish();
        } else {
            final String SPLASH_PREFERENCES = "SPLASHPREFS";
            final String IS_FIRST_LAUNCH = "IS_FIRST_LAUNCH";

            SharedPreferences sharedPreferences = getSharedPreferences(SPLASH_PREFERENCES, 0);

            if (sharedPreferences.getBoolean(IS_FIRST_LAUNCH, true)
                    && Build.VERSION.SDK_INT >= 23
                    && PermissionsActivity.needsDisplay(this)) {
                apiTalker.getNotifications(getApplicationContext(), user.hash(), null);


				sharedPreferences
						.edit()
						.putBoolean(IS_FIRST_LAUNCH, false)
						.apply();

                Intent intent = new Intent(LoginActivity.this, PermissionsActivity.class);
                startActivity(intent, options.toBundle());
            } else {
                apiTalker.getNotifications(getApplicationContext(), user.hash(), this);
                return;
            }

            finish();
        }

		loginWelcomeDialog.dismiss();

		Locker.unlock(this);
	}

	private void proceedFails(String error) {
		loginWelcomeDialog.dismiss();

        if (error.equals(TalkersConstants.JUST_FAILURE)) {
            NotificationHelper.showNotification(error, this);
        } else {
            NotificationHelper.showNotification(
                    getString(R.string.error),
                    error,
                    true,
                    this);
        }

        Locker.unlock(this);
    }

    public void onForgotPassword(View view) {
        if (System.currentTimeMillis() - savedLastClickTime < 200) {
            return;
        }

        savedLastClickTime = System.currentTimeMillis();

        hideKeyboard(this);

        if (screenId == 1) {
            OptimizeHIT.sendScreen(
                    GAnalyticsScreenNames.FORGOT_PASSWORD_SCREEN, null, null, null);
            OptimizeHIT
                    .sendEvent(
                            GAnalitycsEventNames.FORGOT_PASSWORD_BUTTON_PRESSED.CATEGORY,
                            GAnalitycsEventNames.FORGOT_PASSWORD_BUTTON_PRESSED.ACTION,
                            GAnalitycsEventNames.FORGOT_PASSWORD_BUTTON_PRESSED.LABEL);

            screenId = 2;
            switchScreen();
        } else {
            screenId = 1;
            switchScreen();

            OptimizeHIT
                    .sendEvent(
                            GAnalitycsEventNames.CANCEL_FORGOT_PASSWORD_BUTTON_PRESSED.CATEGORY,
                            GAnalitycsEventNames.CANCEL_FORGOT_PASSWORD_BUTTON_PRESSED.ACTION,
                            GAnalitycsEventNames.CANCEL_FORGOT_PASSWORD_BUTTON_PRESSED.LABEL);

        }
    }


	@Override
	public void changePasswordSuccess(String error, String message) {
		loginWelcomeDialog.dismiss();

		screenId = 1;
		switchScreen();

        NotificationHelper.showNotification(error, message, true, this);

        requestInProcess = false;

        Locker.unlock(this);
    }


	@Override
	public void changePasswordFail(String error, String message) {
		loginWelcomeDialog.dismiss();

        if (message != null) {
            NotificationHelper.showNotification(error, message, true, this);
        } else {
            NotificationHelper.showNotification(error, this);
        }

        requestInProcess = false;

        Locker.unlock(this);
    }

    public void switchScreen() {
        RelativeLayout usernamePanel = (RelativeLayout) usernameCustomEditText.getParent();
        RelativeLayout passwordPanel = (RelativeLayout) passwordCustomEditText.getParent();
        RelativeLayout domainPanel = (RelativeLayout) domainCustomEditText.getParent();

        Button loginButton = (Button) findViewById(R.id.login_button);
        Button forgotPasswordButton = (Button) findViewById(R.id.forgot_password);

        tapToChangeTextView = (TextView) findViewById(R.id.tap_to_change);
        TextView pleaseTextView = (TextView) findViewById(R.id.please_action);
        TextView domainLabelTextView = (TextView) findViewById(R.id.domain_label);
        TextView forgotPassInstructionsTextView = (TextView) findViewById(R.id.forgot_pass_instructions);


		ImageView logoImageView = (ImageView) findViewById(R.id.logo);
        String domainImageUrl = user.imageUrl();
		int primaryColor = user.primaryColor();

        if (screenId == 0) {
            usernamePanel.setVisibility(View.GONE);
            passwordPanel.setVisibility(View.GONE);
            domainPanel.setVisibility(View.VISIBLE);
            forgotPasswordButton.setVisibility(View.GONE);
            tapToChangeTextView.setVisibility(View.GONE);
            forgotPassInstructionsTextView.setVisibility(View.GONE);

            domainCustomEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_DONE);

			usernameCustomEditText.setHint(R.string.username_hint);
			pleaseTextView.setText(R.string.please_enter_associated_domain);
			domainLabelTextView.setText(R.string.optiquery_title);
			loginButton.setText(R.string.proceed);
			loginButton.setBackground(ColorUtil.getTintedDrawable(this, R.drawable.rounded_rect_orange, getResources().getColor(R.color.orange)));

            domainLabelTextView.setTextSize(30);

            logoImageView.setImageResource(R.drawable.optiquery_logo);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				getWindow().setNavigationBarColor(getResources().getColor(R.color.dark_orange));
				getWindow().setStatusBarColor(getResources().getColor(R.color.dark_orange));
			}
		} else if (screenId == 1) {
            usernamePanel.setVisibility(View.VISIBLE);
            passwordPanel.setVisibility(View.VISIBLE);
            domainPanel.setVisibility(View.GONE);
            forgotPasswordButton.setVisibility(View.VISIBLE);
            tapToChangeTextView.setVisibility(View.VISIBLE);
            forgotPassInstructionsTextView.setVisibility(View.GONE);

            usernameCustomEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_NEXT);
            passwordCustomEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_DONE);

            usernameCustomEditText.setHint(R.string.username_hint);
            pleaseTextView.setText(R.string.please_enter_username);
            domainLabelTextView.setText(user.domainLabel());
            loginButton.setText(R.string.login);
			loginButton.setBackground(ColorUtil.getTintedDrawable(this, R.drawable.rounded_rect_orange, primaryColor));
			forgotPasswordButton.setText(R.string.forgot_password);
			domainLabelTextView.setTextSize(15);

			Glide
					.with(this)
					.load(user.imageUrl())
					.error(R.drawable.optiquery_logo)
					.signature(new StringSignature(user.imageUrl()))
					.into(logoImageView);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				int color = ColorUtil.getShiftedColor(User.sharedUser(this).primaryColor(), 0.5f);
				getWindow().setNavigationBarColor(color);
				getWindow().setStatusBarColor(color);
			}
		} else if (screenId == 2) {
			usernamePanel.setVisibility(View.VISIBLE);
			passwordPanel.setVisibility(View.GONE);
			domainPanel.setVisibility(View.GONE);
			forgotPasswordButton.setVisibility(View.VISIBLE);
			tapToChangeTextView.setVisibility(View.VISIBLE);
			forgotPassInstructionsTextView.setVisibility(View.VISIBLE);
			pleaseTextView.setText(R.string.please_enter_email);

            usernameCustomEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_DONE);

            usernameCustomEditText.setHint(R.string.enter_email);
            domainLabelTextView.setText(user.domainLabel());
            loginButton.setText(R.string.submit_reset_request);
            forgotPasswordButton.setText(R.string.cancel_request);

            domainLabelTextView.setTextSize(15);

			usernameCustomEditText.setHint(R.string.enter_email);
			domainLabelTextView.setText(user.domainLabel());
			loginButton.setText(R.string.submit_reset_request);
			loginButton.setBackground(ColorUtil.getTintedDrawable(this, R.drawable.rounded_rect_orange, primaryColor));
			forgotPasswordButton.setText(R.string.cancel_request);
			domainLabelTextView.setTextSize(15);

			Glide
					.with(this)
					.load(user.imageUrl())
					.error(R.drawable.optiquery_logo)
					.signature(new StringSignature(user.imageUrl()))
					.into(logoImageView);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				int color = ColorUtil.getShiftedColor(User.sharedUser(this).primaryColor(), 0.5f);
				getWindow().setNavigationBarColor(color);
				getWindow().setStatusBarColor(color);
			}
		}
	}

    /**
     * NotificationsHandler Methods
     */

    @Override
    public void notificationsSuccess(ArrayList<Notification> notifications, Notification immidiateNotification) {
//		ActivityOptions options = ActivityOptions.makeCustomAnimation(this, 0, R.anim.slide_down_out);
        Intent menuIntent = new Intent(this, MenuActivity.class);

        if (immidiateNotification != null) {
            menuIntent.putExtra(DBTalker.EXTRA_NOTIFICATION, immidiateNotification);
        }

        startActivity(menuIntent);
        finish();
    }

    @Override
    public void notificationsFailure(String error) {
//		ActivityOptions options = ActivityOptions.makeCustomAnimation(this, 0, R.anim.slide_down_out);
        Intent menuIntent = new Intent(this, MenuActivity.class);
        startActivity(menuIntent);
        finish();
    }

	/**
	 *  Listener to make all the CustomEditTexts in activity not focusable.
	 *  Indicates back button press when one of CustomEditTexts of activity has focus ( and keyboard is open)
	 */
	CustomEditText.OnHideKeyboardListener hideKeyboardListener = new CustomEditText.OnHideKeyboardListener() {
		@Override
		public void onHideKeyboard() {
			/**
			 *  Android system automatically want that nessesarily one of the EditTexts in the screen have cursor.
			 *  And we want that no one of its have cursor. So we set all the EditTexts not focusable.
			 */
			domainCustomEditText.setFocusable(false);
			domainCustomEditText.setFocusableInTouchMode(false);

			usernameCustomEditText.setFocusable(false);
			usernameCustomEditText.setFocusableInTouchMode(false);

			passwordCustomEditText.setFocusable(false);
			passwordCustomEditText.setFocusableInTouchMode(false);
		}
	};

    @Override
    public void onDomainCheckSuccess(String domain, String domainName, String imageUrl, String primaryColor) {
        user.saveDomain(domain, domainName, imageUrl, primaryColor);
        screenId = 1;
        switchScreen();

		loginWelcomeDialog.dismiss();
		requestInProcess = false;

		Locker.unlock(this);
	}

    @Override
    public void onDomainCheckFailure(String error) {
        NotificationHelper.showNotification(error, this, true);


		loginWelcomeDialog.dismiss();
		requestInProcess = false;

        Locker.unlock(this);

    }

    public void onLogoClicked(View view) {
        if (screenId > 0) {
            screenId = 0;
            switchScreen();

            OptimizeHIT.sendEvent(
                    GAnalitycsEventNames.CHANGE_DOMAIN_BUTTON_PRESSED.CATEGORY,
                    GAnalitycsEventNames.CHANGE_DOMAIN_BUTTON_PRESSED.ACTION,
                    GAnalitycsEventNames.CHANGE_DOMAIN_BUTTON_PRESSED.LABEL);

        }
    }

	private void processWelcomeAndDismissDialog() {

		loginWelcomeDialog.processWelcome(user.firstName(), user.lastName());

		final Handler handler = new Handler();
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				proceed();
			}
		};
		handler.removeCallbacks(runnable);
		handler.postDelayed(runnable, 2000);
	}


//    private void hideSystemUI() {
//        ImageView poweredByImageView = (ImageView) findViewById(R.id.powered_by_image_view);
//        poweredByImageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LOW_PROFILE
//                | View.SYSTEM_UI_FLAG_IMMERSIVE);
//    }
//
//    private void showSystemUI() {
//        ImageView poweredByImageView = (ImageView) findViewById(R.id.powered_by_image_view);
//        poweredByImageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//    }
}
