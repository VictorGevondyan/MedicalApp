package com.implementhit.OptimizeHIT.api;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.format.DateUtils;

import com.implementhit.OptimizeHIT.database.DBTalker;
import com.implementhit.OptimizeHIT.database.ICDDatabase;
import com.implementhit.OptimizeHIT.factory.ICD9RelatedFactory;
import com.implementhit.OptimizeHIT.factory.ICDAdditionalInfoFactory;
import com.implementhit.OptimizeHIT.factory.IDCRecordFactory;
import com.implementhit.OptimizeHIT.models.ICD9Related;
import com.implementhit.OptimizeHIT.models.ICDAdditionalInfo;
import com.implementhit.OptimizeHIT.models.IDCRecord;
import com.implementhit.OptimizeHIT.models.Notification;
import com.implementhit.OptimizeHIT.models.Solution;
import com.implementhit.OptimizeHIT.models.User;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class APITalker {
    /*
     * Constants
	 */

    // Action names
    public static final String ACTION_NEW_SOLUTION_DATA = "actionNewSolutionData";
    public static final String ACTION_UPDATE_DATA_TERMINATED = "actionUpdateDataFail";
    public static final String ACTION_SHOW_QUESTIONS = "actionShowQuestions";
    public static final String ACTION_SUGGESTED_LEARNING_UPDATED = "actionSuggestedLearningUpdated";
    public static final String ACTION_UPDATE_NOTIFICATIONS = "actionUpdateNotifications";
    public static final String ACTION_SHOW_NOTIFICATION = "actionShowNotification";
    public static final String ACTION_SUPERBILL_UPDATED = "actionSuperbillUpdated";
    public static final String ACTION_UPDATE_HISTORY = "actionSaveHistory";
    public static final String ACTION_UPDATE_PEER_FAVORITES = "actionUpdatePeerFavorites";

    // Url constants
     public static final String BASE_URL = "http://api.optimizehitmobile.com";
//    public static final String BASE_URL = "http://ihit-mob-api.eyesoftinc.com";
    public static final String API_URL = BASE_URL + "/api_endpointv2";
    public static final String CHECK_SERVER_REACHABILITY_URL = BASE_URL + "/api_endpointv2/reachability";
    public static final String DASHBOARD_URL = BASE_URL + "/api_endpointv2/dashboard/";

    // Device specific constants
    private static final String PLATFORM = "platform";
    private static final String ANDROID = "Android";
    private static final String VERSION = "version";

    // API method specific constants
    private static final String API_METHOD = "api";
    private static final String API_LOGIN = "login";
    private static final String API_DASHBOARD_BADGE = "dashboard_badge";
    private static final String API_CHECK_DOMAIN = "check_domain";
    private static final String API_LOGOUT = "logout";
    private static final String API_HASHCHECK = "hashcheck";
    private static final String API_GET_DATA = "getdata";
    private static final String API_VOICE_SOLUTION = "voicesolution";
    private static final String API_GET_VOICE_SOLUTION = "getvoicesolution";
    private static final String API_GET_SOLUTION = "getsolution";
    private static final String API_GET_PEER_FAVORITES = "fetchfavs";
    private static final String API_ADD_LIKE = "addlike";
    private static final String API_REMOVE_LIKE = "removelike";
    private static final String API_FEEDBACK = "sendsolutionfeedback";
    private static final String API_POPULAR_QUIESTION = "watson_questions";
    private static final String API_OPTIQUERY = "optiquery";
    private static final String API_UPDATE_LOCATION = "updatelocation";
    private static final String API_SUGGESTED_SOLUTION = "getsuggestedsolutions";
    private static final String API_PUSH_NOTIFICATION = "updatepushtoken";
    private static final String API_FORGOT_PASSWORD = "forgotpassword";
    private static final String API_FIND_CODE = "icdsearchtool";
    private static final String API_DOWNLOAD_SUPERBILL = "downloadgroupedsuperbills";
    private static final String API_EXPLORE_SUPERBILL = "icd10explore";
    private static final String API_ADD_SUPERBILL = "addsuperbill";
    private static final String API_REMOVE_SUPERBILL = "removesuperbill";
    private static final String API_NOTIFICATIONS = "get_notifications";
    private static final String API_READ_NOTIFICATIONS = "read_notification";
    private static final String API_BEACON_IN_RANGE = "beacon_in_range";
    private static final String API_SUBMIT_SUPPORT_TICKER = "submitsupportticket";
    private static final String API_MARK_SUGGESTION_VIEWED = "mark_suggestion_viewed";
    private static final String API_RATE_SOLUTION = "rate_solution";
    private static final String API_GET_USER_HISTORY = "get_user_history";

    // Session specific constants
    private static final String LOGIN = "login";

    // User specific constants
    private static final String USERNAME = "username";
    private static final String FIRSTNAME = "fname";
    private static final String LASTNAME = "lname";
    private static final String DOMAIN = "domain";
    private static final String DOMAIN_LABEL = "domain_label";
    private static final String PASSWORD = "password";
    private static final String HASH = "hash";
    private static final String TRIGGER = "trigger";
    private static final String VOICE_ACCESS = "voice_access";
    private static final String WATSON_ACCESS = "watson_access";
    private static final String FIND_A_CODE = "findacode";

    // Notification specific constants
    private static final String NOTIFICATIONS = "notifications";
    private static final String NOTIFICATION_IDS = "notification_ids";
    private static final String PAGE_TO_OPEN = "link_page";

    // Voice search specific constants
    private static final String RESULT = "result";

    // Solution specific constants
    private static final String SOLUTION_ID = "id";
    private static final String SOLUTION = "solution";
    private static final String SOLUTIONS = "solutions";
    private static final String FULL_HTML = "full_html";
    private static final String FULL_HTML_YES = "yes";

    private static final String SOLUTION_VALID = "solution_valid";

    private static final String HTML = "desc_inline";
    private static final String DESCIPTION = "description";
    private static final String TITLE = "title";
    private static final String FULL_TITLE = "full_title";

    private static final String CALL_TYPE = "call_type";

    public static class CALL_TYPES {
        public static final String CALL_TYPE_BROWSE_SOLUTIONS = "manualsolution";
        public static final String CALL_TYPE_FAVORITES = "favoritesolution";
        public static final String CALL_TYPE_LIKES = "likesolution";
        public static final String CALL_TYPE_HISTORY = "historysolution";
        public static final String CALL_TYPE_DEEP_LINK = "deeplinksolution";
        public static final String CALL_TYPE_OPTIQUERY = "optiquery";
        public static final String CALL_TYPE_PUSH_NOTIFICATION = "pushsolution";
        public static final String CALL_TYPE_REACTIVATION = "reactivation";
        public static final String CALL_TYPE_SUGGESTION = "suggestion";
        public static final String CALL_TYPE_LOCATION = "location";
    }

    // Social specific constants

    private static final String LIKE_TYPE = "like_type";

    public static class LIKE_TYPES {
        public static final String LIKE_TYPE_FAVORITE = "favorite";
        public static final String LIKE_TYPE_LIKE = "like";
    }

    // Feedback specific constants

    private static final String VOICE_QUERY = "voice_query";
    private static final String FEEDBACK = "feedback";

    private static final String ACCESS_METHOD = "access_method";

    public static class ACCESS_METHODS {
        public static final String ACCESS_METHOD_VOICE = "voice";
        public static final String ACCESS_METHOD_MULTIVOICE = "multivoice";
        public static final String ACCESS_METHOD_BROWSE = "browse";
        public static final String ACCESS_METHOD_FAVORITES = "favorites";
        public static final String ACCESS_METHOD_PEER_FAVORITES = "peerfavorites";
        public static final String ACCESS_METHOD_LIKES = "likes";
        public static final String ACCESS_METHOD_HISTORY = "history";
        public static final String ACCESS_METHOD_OPTIQUERY = "optiquery";
        public static final String ACCESS_METHOD_DEEPLINKSOLUTION = "deeplinksolution";
        public static final String ACCESS_METHOD_PUSH_NOTIFICATION = "pushsolution";
        public static final String ACCESS_METHOD_REACTIVATION = "reactivation";
        public static final String ACCESS_METHOD_SUGGESTION = "suggestion";
        public static final String ACCESS_METHOD_LOCATION = "location";
    }

    // OptiQuery specific constants

    private static final String QUESTIONS = "questions";
    private static final String QUESTION = "question";
    private static final String LIBRARY = "library";
    private static final String WATSON = "watson";
    private static final String HAS_ANSWERS = "has_answers";
    private static final String WATSON_HTML = "html";

    // Location specific constants

    private static final String GEO_LAT = "geo_lat";
    private static final String GEO_LONG = "geo_long";
    private static final String GEO_ACCURACY = "geo_accuracy";

    // Suggestion specific constants
    private static final String SUGGESTIONS = "suggestions";
    private static final String INTERNAL_ERROR = "err";
    private static final String VIEWED = "viewed";
    private static final String SUGGESTION_TYPE = "type";

    // Change Password specific constants
    private static final String SUCCESS = "success";
    private static final String MESSAGE = "message";

    public static class SUGGESTION_TYPES {
        public static final int REACTIVATION = 1;
        public static final int SUGGESTION = 2;
        public static final int LOCATION_BASED = 3;
    }

    // Find Code specific constants
    private static final String QUERY = "query";
    private static final String BILLABLE = "billable";
    private static final String ICD_10 = "icd10";

    // Download superbills specific constants
    private static final String SUPERBILLS = "superbills";

    // Explore superbills specific constants
    private static final String CODE = "code";
    private static final String ADDITIONAL_DOCS = "additional_docs";
    private static final String RELATED = "relatedicd9";

    // Add superbills specific constants
    private static final String GROUP = "group";

    // Push Notification specific constants
    private static final String TOKEN = "token";
    private static final String READ = "read";

    // Beacons specific constants
    private static final String BEACON_MAJOR = "beacon_maj";
    private static final String BEACON_MINOR = "beacon_min";

    // Support Ticket specific constants
    private static final String ERROR = "error";

    // Rate Solution specific constant
    private static final String RATING = "rating";

	/*
     * Request Management Helpers
	 */

    int requestIdCounter = 0;

	/*
	 * Singletone
	 */

    private static APITalker talker = null;

    private AsyncHttpClient http;

    private APITalker() {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

        http = new AsyncHttpClient(schemeRegistry);
        http.setResponseTimeout(20000);
        http.setConnectTimeout(20000);
    }

    public static APITalker sharedTalker() {
        if (talker == null) {
            talker = new APITalker();
        }

        return talker;
    }

    /**
     * Session Methods
     */

    public void checkDomain(final String domain, final OnCheckDomainListener listener) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_CHECK_DOMAIN);
        params.put(DOMAIN, domain);

        http.post(API_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (listener != null) {
                    boolean hasError = response.optBoolean("error", false);

                    if (hasError) {
                        String error = response.optString("message");

                        if (error != null && error.length() > 0) {
                            listener.onDomainCheckFailure(error);
                        } else {
                            listener.onDomainCheckFailure(TalkersConstants.LOGIN_FAILED);
                        }
                        return;
                    }

                    if (listener != null) {
                        try {
                            JSONObject domainJSON = response.getJSONObject("domain");
                            String domainName = domainJSON.getString("name");
                            String imageUrl = domainJSON.getString("logo_url");
                            String primaryColor = domainJSON.optString("primary_color", "");

                            listener.onDomainCheckSuccess(domain, domainName, imageUrl, primaryColor);
                        } catch (JSONException e) {
                            listener.onDomainCheckFailure(TalkersConstants.JUST_FAILURE);
                            e.printStackTrace();
                            return;
                        }
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable throwable, org.json.JSONObject errorResponse) {
                if (listener != null) {
                    listener.onDomainCheckFailure(TalkersConstants.JUST_FAILURE);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (listener != null) {
                    listener.onDomainCheckFailure(TalkersConstants.JUST_FAILURE);
                }
            }
        });
    }

    public void login(final String username, final String domain,
                      final String password, final String androidVersion,
                      final SessionEstablishRequestListener listener) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_LOGIN);
        params.put(USERNAME, username);
        params.put(DOMAIN, domain);
        params.put(PASSWORD, password);
        params.put(PLATFORM, ANDROID);
        params.put(VERSION, androidVersion);

        http.post(API_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (listener != null) {
                    boolean isLoginSuccessfull = response.optBoolean(LOGIN,
                            false);

                    if (!isLoginSuccessfull) {
                        String error = response.optString("error");

                        if (error != null && error.length() > 0) {
                            listener.onLoginFailure(error);
                        } else {
                            listener.onLoginFailure(TalkersConstants.LOGIN_FAILED);
                        }
                        return;
                    }

                    long trigger = response.optLong(TRIGGER);
                    String domainLabel = response.optString(DOMAIN_LABEL);
                    String firstName = response.optString(FIRSTNAME);
                    String lastName = response.optString(LASTNAME);
                    String hash = response.optString(HASH);
                    boolean watsonAccess = response.optInt(WATSON_ACCESS) == 1;
                    boolean voiceAccess = response.optInt(VOICE_ACCESS) == 1;
                    boolean findACode = response.optInt(FIND_A_CODE) == 1;

                    listener.onLoginSuccess(username, firstName, lastName, domain, domainLabel,
                            hash, trigger, voiceAccess, watsonAccess,
                            findACode);
                }
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable throwable, org.json.JSONObject errorResponse) {
                if (listener != null) {
                    listener.onLoginFailure(TalkersConstants.JUST_FAILURE);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (listener != null) {
                    listener.onLoginFailure(TalkersConstants.JUST_FAILURE);
                }
            }
        });
    }

    public void logout(final String username, final String domain) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... nopasara) {
                HttpParams params = new BasicHttpParams();
                params.setParameter(API_METHOD, API_LOGOUT);
                params.setParameter(USERNAME, username);
                params.setParameter(DOMAIN, domain);

                int TIMEOUT_MILLISEC = 3000;
                HttpConnectionParams.setConnectionTimeout(params,
                        TIMEOUT_MILLISEC);
                HttpConnectionParams.setSoTimeout(params, TIMEOUT_MILLISEC);
                HttpClient httpclient = new DefaultHttpClient(params);
                HttpPost httppost = new HttpPost(API_URL);
                httppost.addHeader("Content-Type", "application/json");
                try {
                    httpclient.execute(httppost);
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.execute((Void) null);

    }

    /**
     * Data Methods
     */

    public void downloadData( final Context context, final String hash, final boolean needsDrop, final DBTalker db, final DownloadDataRequestListener listener) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_GET_DATA);
        params.put(HASH, hash);

        final Intent intent = new Intent();
        intent.setAction(ACTION_UPDATE_DATA_TERMINATED);

        http.post(API_URL, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                boolean hashValid = response.optBoolean(HASH, false);

                if (!hashValid) {
                    listener.onDownloadFail(TalkersConstants.HASH_INVALID);
                    return;
                }

                if (db != null) {
                    db.insertData(response, needsDrop, listener);
                }
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable throwable, org.json.JSONObject errorResponse) {
                if (listener != null) {
                    listener.onDownloadFail(TalkersConstants.JUST_FAILURE);
                }

                context.sendBroadcast(intent);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (listener != null) {
                    listener.onDownloadFail(TalkersConstants.JUST_FAILURE);
                }

                context.sendBroadcast(intent);

            }
        });
    }

    public void checkDataUpdate(final String hash, final long trigger, final CheckDataUpdateRequestListener listener) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_GET_DATA);
        params.put(HASH, hash);
        params.put(TRIGGER, trigger);

        http.post(API_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (listener != null) {
                    if (response.has(TRIGGER)) {
                        long trigger = response.optLong(TRIGGER);

                        listener.onCheckDataUpdateSuccess(trigger);

                        return;
                    }

                    listener.onCheckDataUpdateFail(TalkersConstants.JUST_FAILURE);
                }
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable throwable, org.json.JSONObject errorResponse) {
                if (listener != null) {
                    listener.onCheckDataUpdateFail(TalkersConstants.JUST_FAILURE);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (listener != null) {
                    listener.onCheckDataUpdateFail(TalkersConstants.JUST_FAILURE);
                }
            }
        });
    }

    public void getDashboardBadgeCount(final String hash, final OnGetDashboardBadgeCountListener listener) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_DASHBOARD_BADGE);
        params.put(HASH, hash);

        http.post(API_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (listener != null) {
                    boolean hashValid = response.optBoolean(HASH, false);

                    if (!hashValid) {
                        listener.onDashboardBadgeCountFailure(TalkersConstants.HASH_INVALID);
                        return;
                    }

                    listener.onDashboardBadgeCountSuccess(response.optInt("badge", 0));
                }
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable throwable, org.json.JSONObject errorResponse) {
                if (listener != null) {
                    listener.onDashboardBadgeCountFailure(TalkersConstants.JUST_FAILURE);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (listener != null) {
                    listener.onDashboardBadgeCountFailure(TalkersConstants.JUST_FAILURE);
                }
            }
        });
    }

    /**
     * Voice Methods
     */

    public int voiceSearch(final String hash, final String result, final VoiceSearchHandler listener) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_VOICE_SOLUTION);
        params.put(HASH, hash);
        params.put(RESULT, result);

        final Integer requestId = requestIdCounter++;

        http.post(API_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (listener != null) {
                    boolean hashValid = response.optBoolean(HASH, false);
                    boolean solutionValid = response.optBoolean(SOLUTION_VALID, false);

                    if (!hashValid) {
                        listener.voiceSearchFailure(
                                TalkersConstants.HASH_INVALID, requestId);
                        return;
                    }
                    if (!solutionValid) {
                        listener.voiceSearchFailure(
                                TalkersConstants.SOLUTION_INVALID, requestId);
                        return;
                    }

                    JSONArray solutionsJSON = response.optJSONArray(SOLUTIONS);

                    if (solutionsJSON.length() > 1) {
                        ArrayList<Solution> solutions = new ArrayList<>();

                        for (int index = 0; index < solutionsJSON.length(); index++) {
                            solutions.add(new Solution(solutionsJSON
                                    .optJSONObject(index)));
                        }

                        listener.voiceSearchSuccess(solutions, requestId);
                    } else {
                        Solution solution = new Solution(solutionsJSON
                                .optJSONObject(0));
                        String html = solutionsJSON.optJSONObject(0).optString(
                                HTML);
                        String speech = solutionsJSON.optJSONObject(0)
                                .optString(DESCIPTION);

                        listener.voiceSearchSuccess(solution, html, speech,
                                requestId);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable throwable, org.json.JSONObject errorResponse) {
                if (listener != null) {
                    listener.voiceSearchFailure(TalkersConstants.JUST_FAILURE, requestId);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (listener != null) {
                    listener.voiceSearchFailure(TalkersConstants.JUST_FAILURE, requestId);
                }
            }
        });

        return requestId;
    }

    /**
     * Solutions Methods
     */

    public void getSolution(final String hash, final int solutionId,
                            final String callType,
                            final SolutionRequestListener listener) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_GET_SOLUTION);
        params.put(HASH, hash);
        params.put(FULL_HTML, FULL_HTML_YES);
        params.put(SOLUTION, solutionId + "");
        params.put(CALL_TYPE, callType);

        http.post(API_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (listener != null) {
                    boolean hashValid = response.optBoolean(HASH, false);

                    if (!hashValid) {
                        listener.solutionFailure(TalkersConstants.HASH_INVALID);
                        return;
                    }

                    boolean validSolution = response.optBoolean(SOLUTION_VALID,
                            false);

                    if (!validSolution) {
                        listener.solutionFailure(TalkersConstants.SOLUTION_INVALID);
                        return;
                    }

                    String title = response.optString(FULL_TITLE);
                    String html = response.optString(HTML);
                    String speech = response.optString(DESCIPTION);

                    listener.solutionSuccess(solutionId, title,
                            html, speech);
                }
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable throwable, org.json.JSONObject errorResponse) {
                if (listener != null) {
                    listener.solutionFailure(TalkersConstants.JUST_FAILURE);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (listener != null) {
                    listener.solutionFailure(TalkersConstants.JUST_FAILURE);
                }
            }
        });

    }

    /**
     * Peers Methods
     */

    public void getPeerFavorites(final String hash, final DBTalker db) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_GET_PEER_FAVORITES);
        params.put(HASH, hash);

        http.post(API_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  JSONObject response) {

                boolean hashValid = response.optBoolean(HASH, false);

                if (!hashValid) {
                    return;
                }

                JSONArray peerSolutions = response.optJSONArray(SOLUTIONS);

                db.insertPeerFavorites(peerSolutions);
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable throwable, org.json.JSONObject errorResponse) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }
        });
    }

    /**
     * Peers Methods
     */

    public void getUserHistory(final String hash, final DBTalker db) {

        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_GET_USER_HISTORY);
        params.put(HASH, hash);

        http.post(API_URL, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  JSONObject response) {


                boolean hashValid = response.optBoolean(HASH, false);

                if (!hashValid) {
                    return;
                }

                db.insertUserHistory(response);

            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable throwable, org.json.JSONObject errorResponse) {
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            }
        });
    }

    /**
     * Likes Methods
     */

    public void addLlike(final String hash, final String likeType,
                         final int solutionId, final LikesRequestListener listener) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_ADD_LIKE);
        params.put(LIKE_TYPE, likeType);
        params.put(HASH, hash);
        params.put(SOLUTION, solutionId + "");

        http.post(API_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  JSONObject response) {
                if (listener != null) {
                    boolean hashValid = response.optBoolean(HASH, false);

                    if (!hashValid) {
                        listener.addLikeFailure(likeType,
                                TalkersConstants.HASH_INVALID);
                        return;
                    }

                    listener.addLikeSuccess(likeType);
                }
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable throwable, org.json.JSONObject errorResponse) {
                if (listener != null) {
                    listener.addLikeFailure(likeType, TalkersConstants.JUST_FAILURE);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (listener != null) {
                    listener.addLikeFailure(likeType, TalkersConstants.JUST_FAILURE);
                }
            }
        });
    }

    public void removeLike(final String hash, final String likeType,
                           final int solutionId, final LikesRequestListener listener) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_REMOVE_LIKE);
        params.put(LIKE_TYPE, likeType);
        params.put(HASH, hash);
        params.put(SOLUTION, solutionId + "");

        http.post(API_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  JSONObject response) {
                if (listener != null) {
                    boolean hashValid = response.optBoolean(HASH, false);

                    if (!hashValid) {
                        listener.removeLikeFailure(likeType,
                                TalkersConstants.HASH_INVALID);
                        return;
                    }

                    listener.removeLikeSuccess(likeType, solutionId);
                }
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable throwable, org.json.JSONObject errorResponse) {
                if (listener != null) {
                    listener.removeLikeFailure(likeType, TalkersConstants.JUST_FAILURE);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (listener != null) {
                    listener.removeLikeFailure(likeType, TalkersConstants.JUST_FAILURE);
                }
            }
        });
    }

    /**
     * Feedback
     */

    public void leaveFeedback(String hash, String accessMethod, int solutionId,
                              String voiceQuery, String feedback, final FeedbackRequestListener listener) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_FEEDBACK);
        params.put(HASH, hash);
        params.put(SOLUTION, solutionId + "");
        params.put(ACCESS_METHOD, accessMethod);
        params.put(VOICE_QUERY, voiceQuery);
        params.put(FEEDBACK, feedback);

        http.post(API_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (listener == null) {
                    return;
                }

                boolean hashValid = response.optBoolean(HASH, false);

                if (!hashValid) {
                    listener.leaveFeedbackFailure(TalkersConstants.HASH_INVALID);
                    return;
                }

                listener.leaveFeedackSuccess();
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable throwable, org.json.JSONObject errorResponse) {
                if (listener != null) {
                    listener.leaveFeedbackFailure(TalkersConstants.JUST_FAILURE);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (listener != null) {
                    listener.leaveFeedbackFailure(TalkersConstants.JUST_FAILURE);
                }
            }
        });
    }

    /**
     * Popular Questions
     */

    public void getPopularQuestions(String hash, final Context context,
                                    final PopularQuestionsRequestListener listener) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_POPULAR_QUIESTION);
        params.put(HASH, hash);

        http.post(API_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                boolean hashValid = response.optBoolean(HASH, false);

                if (!hashValid) {
                    if (listener != null) {
                        listener.onPopularQuestionsFail(TalkersConstants.HASH_INVALID);
                    }
                    return;
                }

                JSONArray questionsJson = response.optJSONArray(QUESTIONS);
                String[] questions = new String[questionsJson.length()];

                for (int index = 0; index < questionsJson.length(); index++) {
                    questions[index] = questionsJson.optString(index);
                }

                DBTalker.sharedDB(context).insertQuestions(questions);

                if (listener != null) {
                    listener.onPopularQuestionsSuccess(questions);
                }
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable throwable, org.json.JSONObject errorResponse) {
                if (listener != null) {
                    listener.onPopularQuestionsFail(TalkersConstants.JUST_FAILURE);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (listener != null) {
                    listener.onPopularQuestionsFail(TalkersConstants.JUST_FAILURE);
                }
            }
        });
    }

    /**
     * OptiQuery Search
     */

    public int optiQuerySearch(final String hash, final String question, final Context context,
                               final OptiQuerySearchRequestListener listener) {

        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_OPTIQUERY);
        params.put(HASH, hash);
        params.put(QUESTION, question);

        final Integer requestId = requestIdCounter++;

        http.post(API_URL, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                if (listener != null) {

                    boolean hashValid = response.optBoolean(HASH, false);

                    if (!hashValid) {
                        listener.onOptiQueryFail(TalkersConstants.HASH_INVALID, requestId);
                        return;
                    }

                    JSONObject library = response.optJSONObject(LIBRARY);
                    JSONObject watson = response.optJSONObject(WATSON);

                    boolean solutionValid = library != null && library.optBoolean(SOLUTION_VALID, false);
                    boolean watsonValid = watson != null && watson.optBoolean(HAS_ANSWERS, false);

                    if (!solutionValid && !watsonValid) {
                        listener.onOptiQuerySuccess(new ArrayList<Solution>(), null, false, false, requestId);
                        return;
                    }

                    JSONArray solutionsJSON = library.optJSONArray(SOLUTIONS);
                    String watsonHtml = watson.optString(WATSON_HTML);

                    if (solutionValid) {

                        if (solutionsJSON.length() > 1) {

                            ArrayList<Solution> solutions = getSolutionsWithCategories( solutionsJSON, context );
                            listener.onOptiQuerySuccess(solutions, watsonHtml,
                                    solutionValid, watsonValid, requestId);

                        } else {

                            Solution solution = new Solution(solutionsJSON.optJSONObject(0));
                            String html = solutionsJSON.optJSONObject(0).optString(HTML);
                            String speech = solutionsJSON.optJSONObject(0).optString(DESCIPTION);

                            listener.onOptiQuerySuccess(solution, html, speech,
                                    watsonHtml, solutionValid, watsonValid,
                                    requestId);

                        }

                    } else {

                        listener.onOptiQuerySuccess(new ArrayList<Solution>(),
                                watsonHtml, solutionValid, watsonValid,
                                requestId);

                    }

                }

            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable throwable, org.json.JSONObject errorResponse) {
                if (listener != null) {
                    listener.onOptiQueryFail(TalkersConstants.JUST_FAILURE, requestId);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (listener != null) {
                    listener.onOptiQueryFail(TalkersConstants.JUST_FAILURE, requestId);
                }
            }
        });

        return requestId;
    }

    public ArrayList<Solution> getSolutionsWithCategories( JSONArray solutionsJsonArray, Context context ){

        ArrayList<Solution> solutions = new ArrayList<>();

        DBTalker dbTalker = DBTalker.sharedDB(context);

        JSONObject solutionJsonObject;
        Solution solution;
        int solutionId;
        for (int index = 0; index < solutionsJsonArray.length(); index++) {

            solutionJsonObject = solutionsJsonArray.optJSONObject(index);
            solutionId = solutionJsonObject.optInt(SOLUTION_ID);
            solution = dbTalker.getSolutionById(solutionId);

            solutions.add(solution);
        }

        return solutions;

    }

    /**
     * UpdateLocation Methods
     */

    public void updateLocation(final String hash, double latitude,
                               double longitude, float accuracy,
                               final UpdateLocationHandler listener) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_UPDATE_LOCATION);
        params.put(HASH, hash);
        params.put(GEO_LAT, latitude);
        params.put(GEO_LONG, longitude);
        params.put(GEO_ACCURACY, accuracy);
        params.put(PLATFORM, ANDROID);

        http.post(API_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  JSONObject response) {
                if (listener != null) {
                    boolean hashValid = response.optBoolean(HASH, false);

                    if (!hashValid) {
                        listener.onLocationUpdateFail(TalkersConstants.HASH_INVALID);
                        return;
                    }

                    listener.onLocationUpdateSuccess();
                }
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable throwable, org.json.JSONObject errorResponse) {
                if (listener != null) {
                    listener.onLocationUpdateFail(TalkersConstants.JUST_FAILURE);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (listener != null) {
                    listener.onLocationUpdateFail(TalkersConstants.JUST_FAILURE);
                }
            }
        });
    }

    public void getSuggestedLearning(String hash, final Context context, final SuggestedLearningRequestListener listener) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_SUGGESTED_SOLUTION);
        params.put(HASH, hash);

        http.post(API_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                boolean hashValid = response.optBoolean(HASH, false);

                if (!hashValid) {
                    if (listener != null) {
                        listener.onSuggestedLearningFail(TalkersConstants.HASH_INVALID);
                    }
                    return;
                }

                boolean error = response.optBoolean(INTERNAL_ERROR, false);

                if (error) {
                    if (listener != null) {
                        listener.onSuggestedLearningFail(TalkersConstants.INTERNAL_ERROR);
                    }
                }

                JSONArray solutions = response.optJSONArray(SUGGESTIONS);

                if (solutions != null) {
                    ArrayList<Solution> reactivations = new ArrayList<Solution>();
                    ArrayList<Solution> suggestions = new ArrayList<Solution>();
                    ArrayList<Solution> locationBased = new ArrayList<Solution>();
                    int viewedCount = 0;

                    for (int index = 0; index < solutions.length(); index++) {
                        JSONObject solutionObject = solutions.optJSONObject(index);
                        int suggestionType = solutionObject.optInt(SUGGESTION_TYPE);
                        boolean isViewed = solutionObject.optString(VIEWED, "0").equals("1");
                        Solution solution = new Solution(solutions.optJSONObject(index));

                        if (suggestionType == SUGGESTION_TYPES.SUGGESTION) {
                            suggestions.add(solution);
                        } else if (suggestionType == SUGGESTION_TYPES.REACTIVATION) {
                            String elapsedTime = DateUtils
                                    .getRelativeTimeSpanString(context, solution.date() * 1000, true)
                                    .toString();
                            solution.setTimestemp(elapsedTime);
                            reactivations.add(solution);
                        } else if (suggestionType == SUGGESTION_TYPES.LOCATION_BASED) {
                            locationBased.add(solution);
                        }

                        if (!isViewed && suggestionType == SUGGESTION_TYPES.REACTIVATION) {
                            viewedCount++;
                        }
                    }

                    Collections.sort(suggestions);
                    Collections.sort(locationBased);
                    Collections.sort(reactivations);

                    DBTalker.sharedDB(context).insertSuggestions(reactivations, suggestions, locationBased, viewedCount, listener);
                    User.sharedUser(context).setReactivations(viewedCount);

//                    if (listener != null) {
//                        listener.onSuggestedLearningSuccess(reactivations,
//                                suggestions, locationBased, viewedCount);
//                    }
                }
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable throwable, org.json.JSONObject errorResponse) {
                if (listener != null) {
                    listener.onSuggestedLearningFail(TalkersConstants.JUST_FAILURE);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (listener != null) {
                    listener.onSuggestedLearningFail(TalkersConstants.JUST_FAILURE);
                }
            }
        });
    }

    public void markViewed(String hash, int solutionId, int solutionType,
                           final MarkViewedRequestListener listener) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_MARK_SUGGESTION_VIEWED);
        params.put(HASH, hash);
        params.put(SOLUTION, solutionId);
        params.put(SUGGESTION_TYPE, solutionType);

        http.post(API_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (listener != null) {
                    boolean hashValid = response.optBoolean(HASH, false);

                    if (!hashValid) {
                        listener.onMarkViewFail(TalkersConstants.HASH_INVALID);
                        return;
                    }

                    listener.onMarkViewSuccess();
                }
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable throwable, org.json.JSONObject errorResponse) {
                if (listener != null) {
                    listener.onMarkViewFail(TalkersConstants.JUST_FAILURE);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (listener != null) {
                    listener.onMarkViewFail(TalkersConstants.JUST_FAILURE);
                }
            }
        });
    }

    public void updatePushNotificationToken(String hash, final Context context,
                                            final String token, final PushNotificationAPIRequestListener listener) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_PUSH_NOTIFICATION);
        params.put(HASH, hash);
        params.put(PLATFORM, ANDROID);
        params.put(TOKEN, token);

        http.post(API_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  JSONObject response) {
                if (listener != null) {
                    boolean hashValid = response.optBoolean(HASH, false);

                    if (!hashValid) {
                        listener.pushTokenUpdateFail(context);
                        return;
                    }

                    listener.pushTokenUpdateSuccess(context, token);
                }
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable throwable, org.json.JSONObject errorResponse) {
                if (listener != null) {
                    listener.pushTokenUpdateFail(context);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (listener != null) {
                    listener.pushTokenUpdateFail(context);
                }
            }
        });
    }

    public void changePassword(final String username, final String domain,
                               final String androidVersion, final ChangePasswordRequestListener listener) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_FORGOT_PASSWORD);
        params.put(USERNAME, username);
        params.put(DOMAIN, domain);
        params.put(PLATFORM, ANDROID);
        params.put(VERSION, androidVersion);

        http.post(API_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  JSONObject response) {
                if (listener != null) {
                    boolean isChangePasswordSuccessful = response.optBoolean(
                            SUCCESS, false);
                    String message = response.optString(MESSAGE);

                    if (!isChangePasswordSuccessful) {
                        listener.changePasswordFail(
                                TalkersConstants.CHANGE_PASSWORD_FAILURE,
                                message);
                    } else {
                        listener.changePasswordSuccess(
                                TalkersConstants.CHANGE_PASSWORD_SUCCESS,
                                message);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable throwable, org.json.JSONObject errorResponse) {
                if (listener != null) {
                    listener.changePasswordFail(TalkersConstants.JUST_FAILURE, null);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (listener != null) {
                    listener.changePasswordFail(TalkersConstants.JUST_FAILURE, null);
                }
            }
        });
    }

    /**
     * Superbill Methods
     */

    public void findCode(String hash, String query, final boolean billable,
                         final FindCodeRequestListener listener) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_FIND_CODE);
        params.put(HASH, hash);
        params.put(QUERY, query);
        params.put(BILLABLE, billable ? "1" : "0");

        http.post(API_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  JSONObject response) {
                if (listener != null) {
                    boolean hashValid = response.optBoolean(HASH, false);

                    if (!hashValid) {
                        listener.findCodeFailure(TalkersConstants.HASH_INVALID);
                        return;
                    }

                    JSONObject icdRecordsJson = response.optJSONObject(ICD_10);

                    if (icdRecordsJson == null || icdRecordsJson.length() == 0) {
                        listener.findCodeSuccess(new IDCRecord[0]);
                        return;
                    }

                    IDCRecord[] records = IDCRecordFactory.getIDCRecords(icdRecordsJson, billable);

                    Arrays.sort(records);

                    listener.findCodeSuccess(records);
                }
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable throwable, org.json.JSONObject errorResponse) {
                if (listener != null) {
                    listener.findCodeFailure(TalkersConstants.JUST_FAILURE);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (listener != null) {
                    listener.findCodeFailure(TalkersConstants.JUST_FAILURE);
                }
            }
        });
    }

    public void downloadSuperbill(String hash, final ICDDatabase database) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_DOWNLOAD_SUPERBILL);
        params.put(HASH, hash);

        http.post(API_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        JSONArray superbillsJson = response
                                .optJSONArray(SUPERBILLS);

                        if (superbillsJson != null) {
                            database.storeSuperbills(superbillsJson);
                        } else {
                            database.storeSuperbills(new JSONArray());
                        }

                        return null;
                    }

                    protected void onPostExecute(Void result) {
                        Intent intent = new Intent(APITalker.ACTION_SUPERBILL_UPDATED);
                        database.getContext().sendBroadcast(intent);
                    }

                    ;
                }.execute((Void) null);
            }
        });
    }

    public void exploreSuperbill(String hash, final String code,
                                 final String description, final ExploreSuperbillRequestListener listener) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_EXPLORE_SUPERBILL);
        params.put(HASH, hash);
        params.put(CODE, code);

        http.post(API_URL, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  JSONObject response) {

                if (listener != null) {
                    boolean hashValid = response.optBoolean(HASH, false);

                    if (!hashValid) {
                        listener.exploreSuperbillFailure(TalkersConstants.HASH_INVALID);
                        return;
                    }

                    boolean billable = response.optString(BILLABLE, "1")
                            .equals("1");
                    JSONObject additionalDocs = response
                            .optJSONObject(ADDITIONAL_DOCS);
                    JSONArray relatedJson = response.optJSONArray(RELATED);

                    ICD9Related[] idc9Related = new ICD9Related[0];
                    ICDAdditionalInfo[] icd10Info = new ICDAdditionalInfo[0];

                    if (relatedJson != null) {
                        idc9Related = ICD9RelatedFactory
                                .getICD9RelatedRecords(relatedJson);
                    }
                    if (additionalDocs != null) {
                        icd10Info = ICDAdditionalInfoFactory
                                .getAdditionalInfo(additionalDocs);
                    }

                    listener.exploreSuperbillSuccess(idc9Related, icd10Info,
                            // error is on previous line!!!!
                            billable, code, description);
                }
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable throwable, org.json.JSONObject errorResponse) {
                if (listener != null) {
                    listener.exploreSuperbillFailure(TalkersConstants.JUST_FAILURE);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (listener != null) {
                    listener.exploreSuperbillFailure(TalkersConstants.JUST_FAILURE);
                }
            }

        } );

    }

    public void addSuperbill(String hash, String code, String description, boolean billable, String group,
                             final AddSuperbillHandler listener) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_ADD_SUPERBILL);
        params.put(HASH, hash);
        params.put(CODE, code);
        params.put(DESCIPTION, description);
        params.put(BILLABLE, billable ? "1" : "0");
        params.put(GROUP, group);

        http.post(API_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  JSONObject response) {
                if (listener != null) {
                    boolean hashValid = response.optBoolean(HASH, false);

                    if (!hashValid) {
                        listener.addSuperbillFailure(TalkersConstants.HASH_INVALID);
                        return;
                    }

                    listener.addSuperbillSuccess();
                }
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable throwable, org.json.JSONObject errorResponse) {
                if (listener == null) {
                    return;
                }

                listener.addSuperbillFailure(TalkersConstants.JUST_FAILURE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (listener == null) {
                    return;
                }

                listener.addSuperbillFailure(TalkersConstants.JUST_FAILURE);
            }
        });
    }

    public void removeSuperbill(String hash, String code) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_REMOVE_SUPERBILL);
        params.put(HASH, hash);
        params.put(CODE, code);

        http.post(API_URL, params, new JsonHttpResponseHandler() {});
    }

    public void checkServersReachability(final CheckServerReachabilityRequestListener listener) {

        http.get(CHECK_SERVER_REACHABILITY_URL, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (listener == null) {
                    return;
                }

                listener.onCheckServerReachabilitySuccess();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseResponse, Throwable throwable) {
                if (listener == null) {
                    return;
                }

                listener.onCheckServerReachabilityFail();
            }
        });

    }

    public void getNotifications(final Context context, final String hash, final NotificationsRequestListener listener) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_NOTIFICATIONS);
        params.put(HASH, hash);

        http.post(API_URL, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                boolean hashValid = response.optBoolean(HASH, false);

                if (!hashValid) {
                    if (listener == null) {
                        return;
                    }

                    listener.notificationsFailure(TalkersConstants.HASH_INVALID);

                    return;
                }

                JSONArray notificationsJSONArray = response.optJSONArray(NOTIFICATIONS);
                ArrayList<Notification> notifications = new ArrayList<Notification>();

                JSONObject notification;

                for (int index = notificationsJSONArray.length() - 1; index >= 0; index--) {
                    notification = notificationsJSONArray.optJSONObject(index);
                    notifications.add(new Notification(notification));
                }

                DBTalker.sharedDB(context).insertNotifications(notifications, listener);
                int unreadNotificationsNumber = DBTalker.sharedDB(context).getUnreadNotificationsNumber();
                User.sharedUser(context).setUnreadNotifications(unreadNotificationsNumber);
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable throwable, org.json.JSONObject errorResponse) {
                if (listener == null) {
                    return;
                }

                listener.notificationsFailure(TalkersConstants.JUST_FAILURE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (listener == null) {
                    return;
                }

                listener.notificationsFailure(TalkersConstants.JUST_FAILURE);
            }

        });
    }

    public void markNotificationRead(final String hash, final String notificationIds, final MarkNotificationsReadRequestListener listener) {

        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_READ_NOTIFICATIONS);
        params.put(HASH, hash);
        params.put(NOTIFICATION_IDS, notificationIds);

        http.post(API_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                boolean hashValid = response.optBoolean(HASH, false);

                if (!hashValid) {
                    if (listener == null) {
                        return;
                    }

                    listener.onMarkNotificationsReadFail(TalkersConstants.HASH_INVALID);
                    return;
                }

                JSONArray notificationsIdJson = null;
                try {
                    notificationsIdJson = new JSONArray(notificationIds);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                ArrayList<String> notificationsIdArray = jsonArrayToStringArray(notificationsIdJson);

                if (listener == null) {
                    return;
                }

                listener.onMarkNotificationsReadSuccess(notificationsIdArray);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (listener == null) {
                    return;
                }

                listener.onMarkNotificationsReadFail(TalkersConstants.JUST_FAILURE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (listener == null) {
                    return;
                }

                listener.onMarkNotificationsReadFail(TalkersConstants.JUST_FAILURE);
            }
        });
    }

    private ArrayList<String> jsonArrayToStringArray(JSONArray jsonArray) {
        ArrayList<String> stringArray = new ArrayList<String>();
        for (int i = 0, count = jsonArray.length(); i < count; i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                stringArray.add(jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return stringArray;
    }

    /**
     * Beacon Methods
     */

    public void sendBeaconInRange(final Context context, final String hash, final int beaconMajor, final int beaconMinor) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_BEACON_IN_RANGE);
        params.put(HASH, hash);
        params.put(BEACON_MAJOR, Integer.valueOf(beaconMajor));
        params.put(BEACON_MINOR, Integer.valueOf(beaconMinor));

        http.post(API_URL, params, new JsonHttpResponseHandler() {
        });
    }

    public void sendSupportTicket(String subject, String description, String diagnostics, String hash, final SupportTicketRequestListener listener) {
        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_SUBMIT_SUPPORT_TICKER);
        params.put(HASH, hash);
        params.put("subject", subject);
        params.put("comments", description + "\n\nDiagnostics Data\n\n" + diagnostics);

        http.post(API_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (listener == null) {
                    return;
                }

                boolean hashValid = response.optBoolean(HASH, false);
                boolean hasError = response.optBoolean(ERROR, false);

                if (!hashValid) {
                    listener.onSupportTicketRequestFail(TalkersConstants.HASH_INVALID, null);

                    return;
                }

                if (hasError) {
                    listener.onSupportTicketRequestFail("Error", response.optString(MESSAGE, "An error has occurred"));

                    return;
                }

                listener.onSupportTicketRequestSuccess("Ticket Submitted", response.optString(MESSAGE, "Ticket submitted successfully"));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (listener == null) {
                    return;
                }

                listener.onSupportTicketRequestFail(TalkersConstants.JUST_FAILURE, null);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    public void rateSolution(final String hash, final Context context, final int solutionId,
                             final float rating,
                             final RateSolutionListener rateSolutionListener) {

        RequestParams params = new RequestParams();
        params.put(API_METHOD, API_RATE_SOLUTION);
        params.put(HASH, hash);
        params.put(SOLUTION, solutionId + "");
        params.put(RATING, rating);

        http.post(API_URL, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  JSONObject response) {
                if (rateSolutionListener != null) {

                    boolean hashValid = response.optBoolean(HASH, false);

                    if (!hashValid) {
                        rateSolutionListener.rateSolutionFail(TalkersConstants.HASH_INVALID);
                        return;
                    }

                    boolean solutionIsValid = response.optBoolean(SOLUTION_VALID,
                            false);

                    if (!solutionIsValid) {
                        rateSolutionListener.rateSolutionFail(TalkersConstants.SOLUTION_INVALID);
                        return;
                    }

                    rateSolutionListener.rateSolutionSuccess();

                }

            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable throwable, org.json.JSONObject errorResponse) {
                if (rateSolutionListener != null) {
                    rateSolutionListener.rateSolutionFail(TalkersConstants.JUST_FAILURE);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (rateSolutionListener != null) {
                    rateSolutionListener.rateSolutionFail(TalkersConstants.JUST_FAILURE);
                }
            }
        });
    }

}

