package com.implementhit.OptimizeHIT.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.implementhit.OptimizeHIT.OptimizeHIT;
import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.analytics.GAnalitycsEventNames;
import com.implementhit.OptimizeHIT.analytics.GAnalyticsScreenNames;
import com.implementhit.OptimizeHIT.api.APITalker;
import com.implementhit.OptimizeHIT.database.DBTalker;
import com.implementhit.OptimizeHIT.dialogs.ChooseDefaultDialog;
import com.implementhit.OptimizeHIT.dialogs.LoadingDialog;
import com.implementhit.OptimizeHIT.dialogs.YesNoDialog;
import com.implementhit.OptimizeHIT.fragments.SolutionFragment;
import com.implementhit.OptimizeHIT.models.User;
import com.implementhit.OptimizeHIT.util.CheckConnectionHelper;
import com.implementhit.OptimizeHIT.util.ColorUtil;
import com.implementhit.OptimizeHIT.util.FontsHelper;
import com.implementhit.OptimizeHIT.util.Locker;
import com.implementhit.OptimizeHIT.util.NotificationHelper;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

public class UserProfileActivity extends SuperActivity implements
        SeekBar.OnSeekBarChangeListener, View.OnClickListener, ChooseDefaultDialog.ChooseDefaultsHandler,
        CompoundButton.OnCheckedChangeListener, TextToSpeech.OnInitListener, MediaPlayer.OnCompletionListener, YesNoDialog.YesNoDialogListener{

    private static final String UTTERANCE_ID = "wpta";
    private static final String FILENAME = "optimizehitDemoSpeech";

    private static String IS_DEFAULT_MENU_SHOWN = "isDefaultMenuShown";

    private float speechSpeed;
    private String[] screenNames;

    private TextToSpeech textToSpeech;
    private com.implementhit.OptimizeHIT.models.Settings settings;
    private static MediaPlayer mediaPlayer = new MediaPlayer();
    private boolean isSpeechReady = false;
    private boolean isPlaying = false;
    private boolean isDefaultMenuShown = false;

    private LoadingDialog speechDialog;
    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_user_profile);

        OptimizeHIT.sendScreen(GAnalyticsScreenNames.USER_PROFILE_SCREEN, null, null, null);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        settings = DBTalker.sharedDB(this).initSettings();
        screenNames = getResources().getStringArray(R.array.default_screens);

        FontsHelper fontsHelper = FontsHelper.sharedHelper(this);

        TextView arrowIcon = (TextView) findViewById(R.id.icon_arrow);
        arrowIcon.setTypeface(fontsHelper.fontello());

        SwitchCompat autoSpeechSwitch = (SwitchCompat) findViewById(R.id.auto_play);
        autoSpeechSwitch.setChecked(settings.isAutoStartSpeech());
        autoSpeechSwitch.setOnCheckedChangeListener(this);
        ColorUtil.changeSwitchColor(autoSpeechSwitch, User.sharedUser(this).primaryColor());

        SwitchCompat enableGroupingSwitch = (SwitchCompat) findViewById(R.id.is_grouping);
        enableGroupingSwitch.setChecked(settings.isGroupingEnabled());
        enableGroupingSwitch.setOnCheckedChangeListener(this);
        enableGroupingSwitch.setOnClickListener(this);
        ColorUtil.changeSwitchColor(enableGroupingSwitch, User.sharedUser(this).primaryColor());

        SwitchCompat hideNoGroupingSwitch = (SwitchCompat) findViewById(R.id.hide_grouping_message);
        hideNoGroupingSwitch.setChecked(!settings.isDisplayingEnableGrouping());
        hideNoGroupingSwitch.setOnCheckedChangeListener(this);
        hideNoGroupingSwitch.setOnClickListener(this);
        ColorUtil.changeSwitchColor(hideNoGroupingSwitch, User.sharedUser(this).primaryColor());

        if (settings.isGroupingEnabled()) {
            hideNoGroupingSwitch.setEnabled(false);
            hideNoGroupingSwitch.setAlpha(0.5f);
        }

        User user = User.sharedUser(this);

        if( user.getFindACodePermission() ){
            View superbillSection = findViewById(R.id.superbill_section);
            superbillSection.setVisibility(View.VISIBLE);
        }

        SeekBar speechSpeedProgress = (SeekBar) findViewById(R.id.speech_speed);
        speechSpeedProgress.setMax(User.MAX_SPEECH_SPEED - User.MIN_SPEECH_SPEED);
        speechSpeedProgress.setProgress((int) (settings.speechSpeed() * 100 - User.MIN_SPEECH_SPEED));
        speechSpeedProgress.setOnSeekBarChangeListener(this);
        speechSpeedProgress.setOnClickListener(this);
        ColorUtil.changeSeekBarColor(speechSpeedProgress, User.sharedUser(this).primaryColor());

        Button playSample = (Button) findViewById(R.id.play_sample);
        playSample.setBackground(ColorUtil.getTintedDrawable(this, R.drawable.little_rounded_rect_orange, User.sharedUser(this).primaryColor()));
        playSample.setOnClickListener(this);

        RelativeLayout defautlsLayout = (RelativeLayout) findViewById(R.id.default_bar);
        defautlsLayout.setOnClickListener(this);

        TextView selectedScreen = (TextView) findViewById(R.id.default_screen);
        selectedScreen.setText(screenNames[settings.defaultScreen()]);

        speechDialog = new LoadingDialog(this);

        if (isDefaultMenuShown) {
            ChooseDefaultDialog chooseDefaultDialog = new ChooseDefaultDialog(this, this);
            isDefaultMenuShown = true;
            chooseDefaultDialog.show();
        }

        TextView settingsLabelTextView = (TextView) findViewById(R.id.settings_label);
        settingsLabelTextView.setBackgroundColor(User.sharedUser(this).primaryColor());

        // Setting up the user profile info
        ImageView clientLogoImageView = (ImageView)findViewById(R.id.client_logo);
        TextView domainLabelTextView = (TextView)findViewById(R.id.domain_label);
        TextView firstLastNameTextView = (TextView)findViewById(R.id.firstname_lastname);
        TextView userNameTextView = (TextView)findViewById(R.id.username);

        String domainLabel = user.domainLabel();
        String firstName = user.firstName();
        String lastName = user.lastName();
        String userName = user.username();

        domainLabelTextView.setText(domainLabel);
        firstLastNameTextView.setText(firstName + " " + lastName);
        userNameTextView.setText(userName);


        // Set up the action bar buttons
        TextView titleTextView = (TextView) findViewById(R.id.content_title);
        titleTextView.setText(R.string.profile_settings);

        LinearLayout leftButtonLayout = (LinearLayout) findViewById(R.id.left_button_layout);
        leftButtonLayout.setVisibility(View.VISIBLE);

        Button diagnosticsButton = (Button) findViewById(R.id.left_button);
        diagnosticsButton.setTypeface(FontsHelper.sharedHelper(this).fontello());
        diagnosticsButton.setText(R.string.icon_diagnostics);
        diagnosticsButton.setOnClickListener(this);

        TextView buttonSubtitleTextView = (TextView) findViewById(R.id.button_subtitle);
        buttonSubtitleTextView.setVisibility(View.VISIBLE);

        Button cancelButton = (Button) findViewById(R.id.right_button);
        cancelButton.setTypeface(FontsHelper.sharedHelper(this).fontello());
        cancelButton.setVisibility(View.VISIBLE);
        cancelButton.setText(R.string.icon_cancel_circled_dark);
        cancelButton.setOnClickListener(this);

        Button logoutButton = (Button) findViewById(R.id.logout);
        logoutButton.setOnClickListener(this);

        // We load client logo image into the corresponding ImageView using Glide library
        Glide
            .with(this)
            .load(user.imageUrl())
            .signature(new StringSignature(user.imageUrl()))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(clientLogoImageView);
    }

    @Override
    public void onResume() {

        if(isPlaying){
            playMediaPlayer();
        }

        textToSpeech = new TextToSpeech(getApplicationContext(), this);

        super.onResume();
    }

    @Override
    protected void refreshStateAfterLogin(boolean isInitialLogin) {

    }

    @Override
    public void onPause() {
        if (mediaPlayer.isPlaying()) {
            pauseMediaPlayer();
            audioManager.abandonAudioFocus(null);
        }

        textToSpeech.shutdown();

        super.onPause();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition( R.anim.hold,  R.anim.slide_down_out );
    }

    /**
     * OnSeekBarChangeListener Methods
     */

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (System.currentTimeMillis() - SuperActivity.savedLastClickTime < 200) {
            return;
        }

        SuperActivity.savedLastClickTime = System.currentTimeMillis();

        speechSpeed = (float) (User.MIN_SPEECH_SPEED + progress);
        speechSpeed = speechSpeed / 100;

        if (textToSpeech != null) {
            textToSpeech.setSpeechRate(speechSpeed);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isSpeechReady = false;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        settings.setSpeechSpeed(speechSpeed, this);

        if (isPlaying) {
            mediaPlayer.reset();

            Locker.lock(this);

            speechDialog.show();

            setTTSListener();
            loadTTS();
        }
    }

    /**
     * OnClickListener Methods
     */

    @Override
    @SuppressLint("NewApi")
    public void onClick(View view) {

        if (System.currentTimeMillis() - SuperActivity.savedLastClickTime < 200) {
            return;
        }

        SuperActivity.savedLastClickTime = System.currentTimeMillis();

        if (view.getId() == R.id.play_sample) {

            if (mediaPlayer.isPlaying()) {
                stopMediaPlayer();
                return;
            }

            if (isSpeechReady) {
                initializeMediaPlayer();
                playMediaPlayer();
            } else {
                mediaPlayer.reset();

                Locker.lock(this);

                speechDialog.show();

                setTTSListener();
                loadTTS();
            }

        } else if (view.getId() == R.id.default_bar) {

            ChooseDefaultDialog chooseDefaultDialog = new ChooseDefaultDialog(this, this);
            isDefaultMenuShown = true;
            chooseDefaultDialog.show();

        } else if (view.getId() == R.id.left_button) {
            Intent settingsActivityIntent = new Intent( UserProfileActivity.this, DiagnosticActivity.class );
            startActivity(settingsActivityIntent);
            overridePendingTransition(R.anim.slide_up_in, R.anim.hold);
        } else if (view.getId() == R.id.right_button) {
            onBackPressed();
        } else if (view.getId() == R.id.logout) {

            showConfirmationDialog();

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(IS_DEFAULT_MENU_SHOWN , isDefaultMenuShown);
    }

    @Override
    public void onRestoreInstanceState (Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        isDefaultMenuShown = savedInstanceState.getBoolean(IS_DEFAULT_MENU_SHOWN, false);

        if (isDefaultMenuShown) {
            findViewById(R.id.default_bar).performClick();
        }
    }

    private void showConfirmationDialog(){

        YesNoDialog yesNoDialog = new YesNoDialog();
        yesNoDialog.setupDialog(
                R.string.logout,
                R.string.message_confirm_logout,
                R.string.cancel);
        yesNoDialog.setHandler(UserProfileActivity.this);
        yesNoDialog.show(getSupportFragmentManager(), "yesNoDialog");

    }

    @Override
    public void onDialogAction(String actionCode) {
        if (actionCode.equals(YesNoDialog.ACTION_YES)) {
            logoutUser();
        }
    }

    private void logoutUser(){

        if( !CheckConnectionHelper.isNetworkAvailable(this)){

            NotificationHelper.showNotification( getString(R.string.title_no_internet),
                    getString(R.string.message_no_internet), true, this );
            return;

        }

        OptimizeHIT.sendEvent(
                GAnalitycsEventNames.LOGOUT_BUTTON_PRESSED.CATEGORY,
                GAnalitycsEventNames.LOGOUT_BUTTON_PRESSED.ACTION,
                GAnalitycsEventNames.LOGOUT_BUTTON_PRESSED.LABEL);

        NotificationHelper.showLoginPage(this, false, true);

        SuperActivity.FROM_LOGGED_OUT = true;

        OptimizeHIT.sendEvent(GAnalitycsEventNames.LOGOUT_BUTTON_PRESSED.CATEGORY,
                GAnalitycsEventNames.LOGOUT_BUTTON_PRESSED.ACTION, GAnalitycsEventNames.LOGOUT_BUTTON_PRESSED.LABEL);

        User user = User.sharedUser(this);
        APITalker.sharedTalker().logout(user.username(), user.domain());
        user.logoutUser();

        user.setLastAccessedPage(-1);

        super.setUserLoggedOut(true);
        super.setUserManualLoggedOut(true);
        super.finish();

    }

    /**
     * ChooseDefaultsHandler Methods
     */

    @Override
    public void selectedDefault(int defaultScreen) {
        settings.setDefaultScreen(defaultScreen, this);

        TextView selectedScreen = (TextView) findViewById(R.id.default_screen);
        selectedScreen.setText(screenNames[settings.defaultScreen()]);
    }

    @Override
    public void dismiss() {
        isDefaultMenuShown = false;
    }

    /**
     * OnCheckedChangeListener Methods
     */

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (System.currentTimeMillis() - SuperActivity.savedLastClickTime < 200) {
            return;
        }

        SuperActivity.savedLastClickTime = System.currentTimeMillis();

        if (buttonView.getId() == R.id.auto_play) {
            settings.setIsAutoStartSpeech(isChecked, this);
        } else if (buttonView.getId() == R.id.is_grouping) {
            settings.setIsGroupingEnabled(isChecked, this);

            SwitchCompat hideMessage = (SwitchCompat) findViewById(R.id.hide_grouping_message);

            if (isChecked) {
                hideMessage.setEnabled(false);
                hideMessage.setAlpha(0.5f);
            } else {
                hideMessage.setEnabled(true);
                hideMessage.setAlpha(1.0f);
            }
        } else if (buttonView.getId() == R.id.hide_grouping_message) {
            settings.setIsDisplayingEnableGrouping(!isChecked, this);
        }
    }

    /**
     * TTS Methods
     */

    @SuppressLint("NewApi")
    private void loadTTS() {
        HashMap<String, String> hashRender = new HashMap<>();
        hashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID);

        String fileName = this.getExternalCacheDir().getAbsolutePath() + FILENAME;
        File file = new File(fileName);

        textToSpeech.setLanguage(Locale.US);
        textToSpeech.setSpeechRate(settings.speechSpeed());

        if (Build.VERSION.SDK_INT >= 21) {
            textToSpeech.synthesizeToFile(getResources().getString(R.string.demo), null, file, UTTERANCE_ID);
        } else {
            textToSpeech.synthesizeToFile(getResources().getString(R.string.demo), hashRender, fileName);
        }
    }

    private void setTTSListener() {
        if (Build.VERSION.SDK_INT >= 15) {
            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {

                @Override
                public void onDone(String utteranceId) {
                    isSpeechReady = true;
                    initializeMediaPlayer();
                    playMediaPlayer();

                    speechDialog.dismiss();

                    Locker.unlock(UserProfileActivity.this);
                }

                @Override
                public void onError(String utteranceId) {
                    speechDialog.dismiss();

                    NotificationHelper.showNotification(SolutionFragment.SPEECH_ERROR, (SuperActivity) UserProfileActivity.this);

                    Locker.unlock(UserProfileActivity.this);
                }

                @Override
                public void onStart(String utteranceId) {
                }
            });
        } else {
            textToSpeech.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
                @Override
                public void onUtteranceCompleted(String utteranceId) {
                    isSpeechReady = true;
                    initializeMediaPlayer();
                    playMediaPlayer();

                    speechDialog.dismiss();

                    Locker.unlock(UserProfileActivity.this);
                }
            });
        }
    }

    private boolean requestAudioFocus() {
        int result = audioManager.requestAudioFocus(null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * OnInitListener Methods
     */

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.SUCCESS) {
            textToSpeech.shutdown();

            if (this != null) {
                textToSpeech = new TextToSpeech(this.getApplicationContext(), this);
            }
        }
    }

    /**
     * Media Player Helpers
     */

    private void initializeMediaPlayer() {
        String fileName = this.getExternalCacheDir().getAbsolutePath() + FILENAME;
        Uri uri = Uri.parse("file://" + fileName);

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mediaPlayer.setDataSource(this.getApplicationContext(), uri);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playMediaPlayer() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                Button playSample = (Button) findViewById(R.id.play_sample);
                playSample.setText(R.string.stop_sample);
            }
        });

        isPlaying = true;

        requestAudioFocus();

        mediaPlayer.start();
    }

    public void stopMediaPlayer() {
        if (this != null) {
            this.runOnUiThread(new Runnable() {
                public void run() {
                    Button playSample = (Button) findViewById(R.id.play_sample);
                    playSample.setText(R.string.play_sample);
                }
            });
        }

        isPlaying = false;

        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(null);
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }

    public void pauseMediaPlayer() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                Button playSample = (Button) findViewById(R.id.play_sample);
                playSample.setText(R.string.play_sample);
            }
        });

        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    /**
     * OnCompletionListener Methods
     */

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        stopMediaPlayer();
    }

    /**
     * Menu State Helpers
     */

    public boolean getIsPlaying() {
        return isPlaying;
    }

    public boolean getIsSpeechReady() {
        return isSpeechReady;
    }

    public boolean getIsDefaultMenuShown() {
        return isDefaultMenuShown;
    }

}
