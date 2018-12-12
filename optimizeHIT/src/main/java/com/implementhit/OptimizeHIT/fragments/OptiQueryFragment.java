package com.implementhit.OptimizeHIT.fragments;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.implementhit.OptimizeHIT.OptimizeHIT;
import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.activity.MenuActivity;
import com.implementhit.OptimizeHIT.activity.ResultsActivity;
import com.implementhit.OptimizeHIT.activity.SolutionActivity;
import com.implementhit.OptimizeHIT.activity.SuperActivity;
import com.implementhit.OptimizeHIT.adapter.QuestionsListAdapter;
import com.implementhit.OptimizeHIT.analytics.GAnalitycsEventNames;
import com.implementhit.OptimizeHIT.analytics.GAnalyticsScreenNames;
import com.implementhit.OptimizeHIT.api.APITalker;
import com.implementhit.OptimizeHIT.api.OptiQuerySearchRequestListener;
import com.implementhit.OptimizeHIT.api.PopularQuestionsRequestListener;
import com.implementhit.OptimizeHIT.api.TalkersConstants;
import com.implementhit.OptimizeHIT.database.DBTalker;
import com.implementhit.OptimizeHIT.dialogs.LoadingDialog;
import com.implementhit.OptimizeHIT.dialogs.NoSolutionDialog;
import com.implementhit.OptimizeHIT.dialogs.NoSolutionDialog.NoSolutionDialogHandler;
import com.implementhit.OptimizeHIT.models.Solution;
import com.implementhit.OptimizeHIT.models.User;
import com.implementhit.OptimizeHIT.util.CheckConnectionHelper;
import com.implementhit.OptimizeHIT.util.ColorUtil;
import com.implementhit.OptimizeHIT.util.CustomEditText;
import com.implementhit.OptimizeHIT.util.FontsHelper;
import com.implementhit.OptimizeHIT.util.Locker;
import com.implementhit.OptimizeHIT.util.NotificationHelper;
import com.implementhit.OptimizeHIT.util.PermissionHelper;
import com.implementhit.OptimizeHIT.views.VoiceSpinnerView;
import com.nuance.speechkit.DetectionType;
import com.nuance.speechkit.Language;
import com.nuance.speechkit.Recognition;
import com.nuance.speechkit.RecognitionException;
import com.nuance.speechkit.RecognitionType;
import com.nuance.speechkit.Transaction;
import com.nuance.speechkit.TransactionException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class OptiQueryFragment extends Fragment implements OnClickListener, OnEditorActionListener,
		NoSolutionDialogHandler, PopularQuestionsRequestListener, OptiQuerySearchRequestListener, OnBackPressedListener, QuestionsListAdapter.OnQuestionClickedListener {
	private static final String SAVE_FAILED_VOICE_QUERY = "saveFailedVoiceQuery";
	private static final String SAVE_OPTIQUERY_KEYBOARD_STATE = "saveOptiQueryKeyboardState";
	private static final String SAVE_OPTIQUERY_QUESTION = "saveOptiQueryQuestion";
	private static final String SAVE_OPTIQUERY_QUESTION_BEFORE_PROCESSING = "saveOptiQueryQuestionBeforeProcessing";
	private static final String SAVE_CURSOR_POSITION = "saveCursorPosition";

	private ValueAnimator scaleDownAnimator;
	private ValueAnimator scaleUpAnimator;
	private ValueAnimator bounceValueAnimator;
	private ValueAnimator spinnerValueAnimator;

    private View optiQueryView;
	private View voiceBackgroundView;
	private VoiceSpinnerView voiceSpinnerView;
	private TextView microphone;
	private RelativeLayout microphoneButton;

	private String optiQueryQuestion;
	private int currentPosition = -1;

	private boolean isListening = false;
    private boolean noSolutionShown = false;
	private boolean isKeyboardVisible = false;

	private AtomicBoolean processingQuestion = new AtomicBoolean(false);

	private QuestionsListAdapter questionsAdapter;

	private Queue<Integer> optiQuerySearchIds = new LinkedList<>();

	private LoadingDialog processingDialog;

	private Transaction nuanceTransaction;

	private Context context;

	Handler handler = new Handler();

    private CustomEditText optiqueryEditText;

	public OptiQueryFragment() {
		nuanceTransaction = null;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		OptimizeHIT.sendScreen(GAnalyticsScreenNames.OPTIQUERY_SCREEN, null, null, null);

		optiQueryView = inflater.inflate(R.layout.fragment_opti_query, container, false);

		microphoneButton = (RelativeLayout) optiQueryView.findViewById(R.id.microphone_button);
		microphoneButton.setOnClickListener(onMicrophoneButtonClickListener);

		voiceBackgroundView = microphoneButton.findViewById(R.id.voice_background);
		voiceBackgroundView.setBackground(ColorUtil.getTintedDrawable(getActivity(), R.drawable.circle_orange, User.sharedUser(getActivity()).primaryColor()));
		voiceSpinnerView = (VoiceSpinnerView) microphoneButton.findViewById(R.id.voice_spinner);

		FontsHelper fontsHelper = FontsHelper.sharedHelper(getActivity());

		microphone = (TextView) optiQueryView.findViewById(R.id.microphone);
		microphone.setTypeface(fontsHelper.fontello());
		microphone.bringToFront();

		initAnimations();

		setMicrophoneActive(CheckConnectionHelper.isNetworkAvailable(getActivity()) && PermissionHelper.isMicrophoneAvailable(getActivity()));

		optiqueryEditText = (CustomEditText) optiQueryView.findViewById(R.id.question);
		optiqueryEditText.addTextChangedListener(queryTextWatcher);
		optiqueryEditText.setRawInputType(InputType.TYPE_CLASS_TEXT);
		optiqueryEditText.setOnEditorActionListener(this);

		if ( savedInstanceState != null ) {
			String failedQuery = savedInstanceState.getString(SAVE_FAILED_VOICE_QUERY);

			int cursorPosition = savedInstanceState.getInt(SAVE_CURSOR_POSITION);
			isKeyboardVisible = savedInstanceState.getBoolean(SAVE_OPTIQUERY_KEYBOARD_STATE, false);

			if (failedQuery != null && !failedQuery.isEmpty()) {
                shouldOpenNoSolutions(failedQuery);
			}

			setCurrentCursorPosition(cursorPosition);

            if( isKeyboardVisible ){
                showKeyboard();
            }

            optiQueryQuestion = savedInstanceState.getString(SAVE_OPTIQUERY_QUESTION);
		}

        optiqueryEditText.setText(optiQueryQuestion);

		if (noSolutionShown) {
			openNoSolutions();
		}

		LinearLayout voiceSearchContainer = (LinearLayout) optiQueryView.findViewById(R.id.voice_container);

		Button queryButton = (Button) optiQueryView.findViewById(R.id.button_query);
		queryButton.setBackground(ColorUtil.getTintedDrawable(getActivity(), R.drawable.rounded_rect_orange, User.sharedUser(getActivity()).primaryColor()));
		queryButton.setOnClickListener(this);

		if (currentPosition > -1 && currentPosition <= optiqueryEditText.getText().length()) {
			optiqueryEditText.setSelection(currentPosition, currentPosition);
		}

		LinearLayout questionsLinearLayout = (LinearLayout) optiQueryView.findViewById(R.id.questions_list);
		questionsAdapter = new QuestionsListAdapter(getActivity(), new String[0], questionsLinearLayout, this);

		loadQuestionsFromDatabase();

		processingDialog = new LoadingDialog(getActivity());

		voiceSearchContainer.setFocusable(true);
		voiceSearchContainer.setFocusableInTouchMode(true);
		voiceSearchContainer.requestFocus();

		TextView clearButton = (TextView) optiQueryView.findViewById(R.id.clear_icon);
		clearButton.setOnClickListener(this);
		clearButton.setTypeface(FontsHelper.sharedHelper(getActivity()).fontello());

		trackKeyboard();

        setRetainInstance(true);

		return optiQueryView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
        context = getActivity().getApplicationContext();
		if(isListening){
			startListeningAnim();
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

	}

    @Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(SAVE_FAILED_VOICE_QUERY, failedVoiceQuery());
        savedInstanceState.putString(SAVE_OPTIQUERY_QUESTION, optiQueryQuestion());
        savedInstanceState.putString(SAVE_OPTIQUERY_QUESTION_BEFORE_PROCESSING, optiQueryQuestion);
        savedInstanceState.putInt(SAVE_CURSOR_POSITION, currentCursorPosition());
		savedInstanceState.putBoolean(SAVE_OPTIQUERY_KEYBOARD_STATE, isKeyboardVisible);

		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		processingQuestion.set(false);

		setMicrophoneActive(CheckConnectionHelper.isNetworkAvailable(getActivity()) && PermissionHelper.isMicrophoneAvailable(getActivity()));
		loadQuestionsFromDatabase();

        if (SuperActivity.FROM_LOGGED_OUT) {
            optiqueryEditText.setText(null);
        } else {
            optiqueryEditText.setText(optiQueryQuestion);
        }
	}

	@Override
	public void onPause() {
		super.onPause();

        isKeyboardVisible = getIsKeyboardVisible();

		handler.postDelayed(cancelSearchRunnable, 2000);
//		closeKeyboardSoft();
	}

	Runnable cancelSearchRunnable = new Runnable() {
		@Override
		public void run() {
			cancelSearch();
		}
	};

	@Override
	public void onStop() {
		super.onStop();

		recoverAnimation();
	}

	@Override
	public void onDetach() {
		handler.removeCallbacks(cancelSearchRunnable);
		super.onDetach();
	}

    public String failedVoiceQuery() {
		if (noSolutionShown) {
			return optiQueryQuestion;
		}

		return "";
	}

	public String optiQueryQuestion() {
		CustomEditText optiqueryText = (CustomEditText) optiQueryView.findViewById(R.id.question);
		return optiqueryText.getText().toString();
	}

	public int currentCursorPosition() {
		CustomEditText optiqueryText = (CustomEditText) optiQueryView.findViewById(R.id.question);
		return optiqueryText.getSelectionStart();
	}

	public void setCurrentCursorPosition(int currentPosition) {
		this.currentPosition = currentPosition;
	}

	public void shouldOpenNoSolutions(String failedVoiceQuery) {
		noSolutionShown = true;
		optiQueryQuestion = failedVoiceQuery;
	}

	private void openNoSolutions() {
		NoSolutionDialog noSolutionDialog = new NoSolutionDialog(getActivity(), optiQueryQuestion, this);
		noSolutionDialog.show();
		noSolutionShown = true;
	}

	/**
	 * Animation Helpers
	 */

	private int spinnerStartValue;
	private int spinnerEndValue;

	private void initAnimations() {
		bounceValueAnimator = ValueAnimator.ofInt(0, 0);
		bounceValueAnimator.setDuration(400);
		bounceValueAnimator.setRepeatMode(ValueAnimator.REVERSE);
		bounceValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
		bounceValueAnimator.addUpdateListener(animatorUpdateListener);
		scaleDownAnimator = ValueAnimator.ofInt(0, 0);
		scaleDownAnimator.setDuration(100);
		scaleDownAnimator.addUpdateListener(animatorUpdateListener);
		scaleDownAnimator.addListener(animatorListener);
		scaleUpAnimator = ValueAnimator.ofInt(0, 0);
		scaleUpAnimator.setDuration(100);
		scaleUpAnimator.addUpdateListener(animatorUpdateListener);
		scaleUpAnimator.addListener(animatorListener);
		spinnerValueAnimator = ValueAnimator.ofInt(0, 0);
		spinnerValueAnimator.setDuration(4000);
		spinnerValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
		spinnerValueAnimator.addUpdateListener(animatorUpdateListener);
	}

	public void recoverAnimation() {
		scaleUpAnimator.cancel();
		scaleDownAnimator.cancel();
		bounceValueAnimator.cancel();
		spinnerValueAnimator.cancel();
	}

	public void startListeningAnim() {
		scaleDownAnimator.setIntValues(microphoneButton.getMeasuredHeight(), microphoneButton.getMeasuredHeight() / 3);
		scaleDownAnimator.start();
	}

	public void stopListeningAnim() {
		scaleDownAnimator.cancel();
		bounceValueAnimator.cancel();

		scaleUpAnimator.setIntValues(voiceBackgroundView.getMeasuredHeight(), microphoneButton.getMeasuredHeight());
		scaleUpAnimator.start();
	}

	public void stopDecodingAnim() {
		spinnerValueAnimator.cancel();
		voiceSpinnerView.setVisibility(View.INVISIBLE);
	}

	private ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
		@Override
		public void onAnimationUpdate(ValueAnimator valueAnimator) {
			int value = (int) valueAnimator.getAnimatedValue();

			if (valueAnimator.equals(scaleDownAnimator)
					|| valueAnimator.equals(scaleUpAnimator)
					|| valueAnimator.equals(bounceValueAnimator)) {
				RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) voiceBackgroundView.getLayoutParams();
				layoutParams.width = value;
				layoutParams.height = value;
				voiceBackgroundView.setLayoutParams(layoutParams);
				voiceBackgroundView.requestLayout();
			} else if (valueAnimator.equals(spinnerValueAnimator)) {
				if (value - 150 < spinnerStartValue) {
					voiceSpinnerView.setArcStart(spinnerStartValue);
					voiceSpinnerView.setArcSweep(value - spinnerStartValue);
				} else if (spinnerEndValue - value <= 150) {
					voiceSpinnerView.setArcStart(value - 150);
					voiceSpinnerView.setArcSweep(spinnerEndValue - value);
				} else {
					voiceSpinnerView.setArcStart(value - 150);
					voiceSpinnerView.setArcSweep(150);
				}

				voiceSpinnerView.invalidate();

				if (value >= spinnerEndValue - 1 && isListening) {
					spinnerStartValue = spinnerEndValue - 150;
					spinnerEndValue = spinnerStartValue + 3 * 360 + 60;
					spinnerValueAnimator.setIntValues(spinnerStartValue, spinnerEndValue);
					spinnerValueAnimator.start();
				}
			}
		}
	};

	private ValueAnimator.AnimatorListener animatorListener = new ValueAnimator.AnimatorListener() {

		@Override
		public void onAnimationStart(Animator animator) {

		}

		@Override
		public void onAnimationEnd(Animator animator) {
			if (animator.equals(scaleDownAnimator)) {
				bounceValueAnimator.setIntValues(microphoneButton.getMeasuredHeight() / 3, 2 * microphoneButton.getMeasuredHeight() / 3);
				bounceValueAnimator.start();
			} else if (animator.equals(scaleUpAnimator) && isListening) {
				spinnerStartValue = 0;
				spinnerEndValue = 3 * 360 + 210;
				voiceSpinnerView.setVisibility(View.VISIBLE);
				spinnerValueAnimator.setIntValues(spinnerStartValue, spinnerEndValue);
				spinnerValueAnimator.start();
			}
		}

		@Override
		public void onAnimationCancel(Animator animator) {

		}

		@Override
		public void onAnimationRepeat(Animator animator) {

		}
	};

	/**
	 * Search Helpers
	 */

	public void cancelSearch() {
		Locker.unlock(getActivity());

		if (!isListening) {
			return;
		}

		isListening = false;
		nuanceTransaction.cancel();
		voiceHandler.removeCallbacks(voiceRunnable);
		stopListeningAnim();
	}

	/**
	 * OnClickListener Methods
	 */

	@Override
	public void onClick(View view) {

		if (System.currentTimeMillis() - SuperActivity.savedLastClickTime < 200) {
			return;
		}

		SuperActivity.savedLastClickTime = System.currentTimeMillis();

		if (view.getId() == R.id.button_query) {
			if (isListening) {
				cancelSearch();
			}

			querySolution();
		} else if (view.getId() == R.id.clear_icon) {
			CustomEditText questionText = (CustomEditText) optiQueryView.findViewById(R.id.question);
			questionText.setText("");
			closeKeyboardSoft();
		}
	}

	OnClickListener onMicrophoneButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			OptimizeHIT.sendEvent(GAnalitycsEventNames.VOICE_SEARCH_BUTTON_PRESSED.CATEGORY,
					GAnalitycsEventNames.VOICE_SEARCH_BUTTON_PRESSED.ACTION,
					GAnalitycsEventNames.VOICE_SEARCH_BUTTON_PRESSED.LABEL);

			if (System.currentTimeMillis() - SuperActivity.savedLastClickTime < 200) {
				return;
			}

			SuperActivity.savedLastClickTime = System.currentTimeMillis();


			if (!PermissionHelper.checkIfHasPermission(getActivity(), Manifest.permission.RECORD_AUDIO)) {
				NotificationHelper.showNotification(getString(R.string.suggestion), getString(R.string.voice_recognition_disabled), true, (SuperActivity) getActivity());
				return;
			}
			if (!PermissionHelper.hasMicrophoneHardware(getActivity())) {
				NotificationHelper.showNotification(getString(R.string.suggestion), getString(R.string.microphone_unavailable), true, (SuperActivity) getActivity());
				return;
			}
			if (!CheckConnectionHelper.isNetworkAvailable(getActivity())) {
				NotificationHelper.showNotification(TalkersConstants.JUST_FAILURE, (SuperActivity) getActivity());
				return;
			}

			if (isListening) {
				// Voice search was in progress. Cancel voice search

				OptimizeHIT.sendEvent(GAnalitycsEventNames.VOICE_SEARCH_STOPPED_FINISHED.CATEGORY,
						GAnalitycsEventNames.VOICE_SEARCH_STOPPED_FINISHED.ACTION,
						GAnalitycsEventNames.VOICE_SEARCH_STOPPED_FINISHED.LABEL);

				cancelSearch();
			} else {
				// Start voice search

				OptimizeHIT.sendEvent(GAnalitycsEventNames.VOICE_SEARCH_START.CATEGORY,
						GAnalitycsEventNames.VOICE_SEARCH_START.ACTION, GAnalitycsEventNames.VOICE_SEARCH_START.LABEL);

				startVoiceListening();
			}
		}
	};

	public void startVoiceListening(){
		isListening = true;

		Transaction.Options options = new Transaction.Options();
		options.setRecognitionType(RecognitionType.DICTATION);
		options.setDetection(DetectionType.Short);
		options.setLanguage(new Language("eng-USA"));

		nuanceTransaction = MenuActivity.getNuanceSession().recognize(options, nuanceTransactionListener);
	}

	public void browseManually() {
//		CustomEditText optiqueryText = (CustomEditText) optiQueryView.findViewById(R.id.question);
//		optiqueryText.setText("");
//
//		ActivityOptions options = ActivityOptions.makeCustomAnimation(getActivity(), R.anim.slide_left_in, R.anim.hold);
//		Intent browseIntent = new Intent(getActivity(), BrowseActivity.class);
//		getActivity().startActivityForResult(browseIntent, BrowseActivity.REQUEST_CODE, options.toBundle());
	}

	public void querySolution() {

		OptimizeHIT.sendEvent(GAnalitycsEventNames.VOICE_SEARCH_RESULT_RECEIVED.CATEGORY,
				GAnalitycsEventNames.VOICE_SEARCH_RESULT_RECEIVED.ACTION, optiQueryQuestion);

		Locker.lock(getActivity());

		CustomEditText questionText = (CustomEditText) optiQueryView.findViewById(R.id.question);
		optiQueryQuestion = questionText.getText().toString();

		if (!isQueryAcceptable()) {
			return;
		}

		processingDialog.show();

		int requestId = APITalker.sharedTalker().optiQuerySearch( User.sharedUser( getActivity()).hash(), optiQueryQuestion,
                getActivity(), this );

		optiQuerySearchIds.add(requestId);

	}

	public boolean isQueryAcceptable() {
		if (optiQueryQuestion == null || optiQueryQuestion.isEmpty()) {
			Locker.unlock(getActivity());

			NotificationHelper.showNotification(getString(R.string.error), getString(R.string.type_or_use_voice), true, (SuperActivity) getActivity());

			return false;
		}

		return true;
	}

	/**
	 * OptiQuerySearchHandler Methods
	 */

	@Override
	public void onOptiQuerySuccess(ArrayList<Solution> solutions, String watsonHtml, boolean solutionValid,
			boolean watsonValid, int requestId) {

		if (!optiQuerySearchIds.contains(requestId)) {
			return;
		}

		optiQuerySearchIds.remove(requestId);

		if (processingDialog.isShowing()) {
			processingDialog.dismiss();
		}

		ActivityOptions options = ActivityOptions.makeCustomAnimation(context, R.anim.slide_left_in, R.anim.hold);
		ResultsActivity.setWatsonHtml(watsonHtml);
		Intent resultsIntent = new Intent(context, ResultsActivity.class);
		resultsIntent.putExtra(SolutionActivity.EXTRA_SOLUTIONS, solutions);
		resultsIntent.putExtra(ResultsActivity.OPTI_QUERY_EXTRA, optiQueryQuestion);
		resultsIntent.putExtra(SolutionActivity.EXTRA_ACCESS_METHOD, APITalker.ACCESS_METHODS.ACCESS_METHOD_OPTIQUERY);
		resultsIntent.putExtra(SolutionFragment.VOICE_QUERY_EXTRA, optiQueryQuestion);
		resultsIntent.putExtra(ResultsActivity.HAS_VOICE_ACCESS_EXTRA, User.sharedUser(context).voiceAccess() && solutionValid);
		resultsIntent.putExtra(ResultsActivity.HAS_WATSON_ACCESS_EXTRA, User.sharedUser(context).watsonAccess() && watsonValid);
		resultsIntent.putExtra(SolutionActivity.EXTRA_POSITION, -1);
		startActivityForResult(resultsIntent, ResultsActivity.REQUEST_CODE, options.toBundle());

		isListening = false;

	}

	@Override
	public void onOptiQuerySuccess(Solution solution, String html, String speech, String watsonHtml,
			boolean solutionValid, boolean watsonValid, int requestId) {
		if (!optiQuerySearchIds.contains(requestId)) {
			return;
		}

		DBTalker.sharedDB(getActivity()).saveHistory(solution.solutionId(), solution.title(), true);

		optiQuerySearchIds.remove(requestId);

		if (processingDialog.isShowing()) {
			processingDialog.dismiss();
		}

		ArrayList<Solution> solutions = new ArrayList<>();
		solutions.add(solution);

		ActivityOptions options = ActivityOptions.makeCustomAnimation(context, R.anim.slide_left_in, R.anim.hold);
		Intent resultsIntent = new Intent(context, ResultsActivity.class);
		ResultsActivity.setWatsonHtml(watsonHtml);
		resultsIntent.putExtra(SolutionActivity.EXTRA_HTML, html);
		resultsIntent.putExtra(SolutionActivity.EXTRA_SPEECH, speech);
		resultsIntent.putParcelableArrayListExtra(SolutionActivity.EXTRA_SOLUTIONS, solutions);
		resultsIntent.putExtra(SolutionActivity.EXTRA_POSITION, 0);
		resultsIntent.putExtra(SolutionActivity.EXTRA_ACCESS_METHOD, APITalker.ACCESS_METHODS.ACCESS_METHOD_OPTIQUERY);
		resultsIntent.putExtra(ResultsActivity.OPTI_QUERY_EXTRA, optiQueryQuestion);
		resultsIntent.putExtra(SolutionFragment.VOICE_QUERY_EXTRA, optiQueryQuestion);
		resultsIntent.putExtra(ResultsActivity.HAS_VOICE_ACCESS_EXTRA,
				User.sharedUser(context).voiceAccess() && solutionValid);
		resultsIntent.putExtra(ResultsActivity.HAS_WATSON_ACCESS_EXTRA,
				User.sharedUser(context).watsonAccess() && watsonValid);
		startActivityForResult(resultsIntent, SolutionActivity.REQUEST_CODE, options.toBundle());

		isListening = false;

		Locker.unlock(getActivity());
	}

	@Override
	public void onOptiQueryFail(String error, int requestId) {
		if (!optiQuerySearchIds.contains(requestId)) {
			return;
		}

		closeKeyboardSoft();

		optiQuerySearchIds.remove(requestId);

		if (processingDialog.isShowing()) {
			processingDialog.dismiss();
		}

		isListening = false;

		if (error.equals(TalkersConstants.SOLUTION_INVALID)) {
			openNoSolutions();
		}

		NotificationHelper.showNotification(error, (SuperActivity) getActivity());

		processingQuestion.set(false);

		Locker.unlock(getActivity());
	}


	/**
	 * NoSolutionDialogHandler Methods
	 */

	@Override
	public void tryAgain() {
		noSolutionShown = false;
		Locker.lock(getActivity());

		querySolution();
	}

	@Override
	public void browse() {
		noSolutionShown = false;
	}

	@Override
	public void onDismiss() {
		noSolutionShown = false;
		recoverAnimation();
	}

	@Override
	public void onQuestionClicked(String question) {

		if (System.currentTimeMillis() - SuperActivity.savedLastClickTime < 200) {
			return;
		}

		SuperActivity.savedLastClickTime = System.currentTimeMillis();

		cancelSearch();

		if (processingQuestion.get()) {
			return;
		}

		processingQuestion.set(true);

		CustomEditText questionText = (CustomEditText) optiQueryView.findViewById(R.id.question);

		if (question.equals(getResources().getStringArray(R.array.loading_questions)[0])
				|| question.equals(getResources().getStringArray(R.array.no_questions)[0])
				|| question.equals(getResources().getStringArray(R.array.unable_questions)[0])
				|| question.equals(getResources().getStringArray(R.array.refresh_to_load_again)[0])) {
			return;
		}

		optiQueryQuestion = question;

		questionText.setText(optiQueryQuestion);
		questionText.setSelection(questionText.getText().length());
		querySolution();
	}

	/**
	 * Handlers
	 */

	public interface VoiceSearchFragmentHandler {
		void browse();
	}

	/**
	 * Question Helpers
	 */

	public void loadQuestionsFromDatabase() {
		String[] questions = DBTalker.sharedDB(getActivity()).getQuestions();

		if (questions.length == 0) {
			questions = getResources().getStringArray(R.array.no_questions);
		}

		questionsAdapter.setItems(questions);
	}

	/**
	 * PopularQuestionsHandler Methods
	 */

	@Override
	public void onPopularQuestionsSuccess(String[] questions) {
		if (questions.length == 0) {
			questionsAdapter.setItems(getResources().getStringArray(R.array.no_questions));
		}

		questionsAdapter.setItems(questions);

		Locker.unlock(getActivity());
	}

	@Override
	public void onPopularQuestionsFail(String error) {
		if (error.equals(TalkersConstants.JUST_FAILURE)) {
			questionsAdapter.setItems(getResources().getStringArray(R.array.refresh_to_load_again));
			NotificationHelper.showNotification(error, (SuperActivity) getActivity());
		} else if (error.equals(TalkersConstants.HASH_INVALID)) {
			NotificationHelper.showNotification(error, (SuperActivity) getActivity());
		}

		Locker.unlock(getActivity());
	}

	/**
	 * Soft Keyboard Helpers
	 */

	public void trackKeyboard() {
		final View activityRootView = optiQueryView.findViewById(R.id.opti_scroll);
		activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				activityRootView.requestLayout();
			}
		});
	}

	public boolean getIsKeyboardVisible() {
		return isKeyboardVisible;
	}

	public void closeKeyboardSoft() {
		if (getActivity() != null && optiQueryView != null) {
			LinearLayout searchPanel = (LinearLayout) optiQueryView.findViewById(R.id.voice_container);
			searchPanel.requestFocus();
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
		}
	}

	public void showKeyboard() {
		if (getActivity() != null && optiQueryView != null) {

			CustomEditText searchField = (CustomEditText) optiQueryView.findViewById(R.id.question);
            searchField.setFocusable(true);
            searchField.setFocusableInTouchMode(true);
			searchField.requestFocus();

			InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.showSoftInput(searchField, 0);

            isKeyboardVisible = true;

		}
	}

	public boolean isQuestionFocused() {
		if (optiQueryView == null) {
			return false;
		}

		return optiQueryView.findFocus() != null && optiQueryView.findFocus().getId() == R.id.question;
	}

	/**
	 * TextWatcher for Query Field
	 */

	TextWatcher queryTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			optiQueryQuestion = s.toString();

			if (s.length() == 0) {
				optiQueryView.findViewById(R.id.clear_icon).setVisibility(View.INVISIBLE);
			} else {
				optiQueryView.findViewById(R.id.clear_icon).setVisibility(View.VISIBLE);
			}

            isKeyboardVisible = true;

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void afterTextChanged(Editable s) {

		}
	};

	/**
	 * OnEditorActionListener Methods
	 */

	@Override
	public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE) {
			querySolution();
			return true;
		}

		return false;
	}

	/**
	 * Nuance Speech Listener
	 */

	Transaction.Listener nuanceTransactionListener = new Transaction.Listener() {

		@Override
		public void onStartedRecording(Transaction transaction) {
			super.onStartedRecording(transaction);

			voiceHandler.postDelayed(voiceRunnable, 60000);

			final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
			tg.startTone(ToneGenerator.TONE_PROP_BEEP);

			startListeningAnim();
		}

		@Override
		public void onFinishedRecording(Transaction transaction) {
			super.onFinishedRecording(transaction);
			stopListeningAnim();
		}

		@Override
		public void onRecognition(Transaction transaction, Recognition recognition) {
			super.onRecognition(transaction, recognition);

			isListening = false;

			voiceHandler.removeCallbacks(voiceRunnable);
			optiQueryQuestion = recognition.getText().trim();
			processVoiceSearchQuery(optiQueryQuestion);
			stopDecodingAnim();
		}

		@Override
		public void onSuccess(Transaction transaction, String suggestion) {
			super.onSuccess(transaction, suggestion);
		}

		@Override
		public void onError(Transaction transaction, String suggestion, TransactionException exception) {
			super.onError(transaction, suggestion, exception);

			String errorMessage = exception.getMessage();

			if (exception instanceof RecognitionException
					&& errorMessage.contains("cancelled")) {
				return;
			}

			OptimizeHIT.sendEvent(GAnalitycsEventNames.VOICE_SEARCH_ERROR.CATEGORY,
					GAnalitycsEventNames.VOICE_SEARCH_ERROR.ACTION, errorMessage );

			isListening = false;

			voiceHandler.removeCallbacks(voiceRunnable);

			NotificationHelper.showNotification(getString(R.string.suggestion), suggestion, true, (SuperActivity) getActivity());
			nuanceTransaction.cancel();

			stopDecodingAnim();
		}

	};

	public void processVoiceSearchQuery( String voiceSearchQuery ){
		CustomEditText questionText = (CustomEditText) optiQueryView.findViewById(R.id.question);
		questionText.setText(voiceSearchQuery);
		questionText.setSelection(voiceSearchQuery.length());

        querySolution();

        isListening = false;
    }

	public void setMicrophoneActive(boolean isActive) {
		if (optiQueryView == null) {
			return;
		}

		if (isActive) {
			//voiceBackgroundView.setBackgroundResource(R.drawable.circle_orange);
			voiceBackgroundView.setBackground(ColorUtil.getTintedDrawable(getActivity(), R.drawable.circle_orange, User.sharedUser(getActivity()).primaryColor()));
			microphone.setText(R.string.icon_mic);
		} else {
			voiceBackgroundView.setBackgroundResource(R.drawable.circle_red);
		}
	}

	/**
	 * Voice Recognition timer
	 */

	private Handler voiceHandler = new Handler();
	private Runnable voiceRunnable = new Runnable() {

		@Override
		public void run() {
			stopListeningAnim();
			nuanceTransaction.cancel();
			NotificationHelper.showNotification(getString(R.string.suggestion), getString(R.string.speech_not_recognized), true, (SuperActivity) getActivity());

			Locker.unlock(getActivity());
		}
	};

	@Override
	public boolean onBackPressed() {
		if (nuanceTransaction != null) {
			cancelSearch();
			nuanceTransaction.cancel();
			return true;
		}

		return false;
	}

}
