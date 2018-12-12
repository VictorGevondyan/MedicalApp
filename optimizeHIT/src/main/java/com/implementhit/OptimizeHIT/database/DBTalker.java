package com.implementhit.OptimizeHIT.database;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.text.format.DateUtils;
import android.util.Log;

import com.implementhit.OptimizeHIT.api.APITalker;
import com.implementhit.OptimizeHIT.api.DownloadDataRequestListener;
import com.implementhit.OptimizeHIT.api.NotificationsRequestListener;
import com.implementhit.OptimizeHIT.api.SuggestedLearningRequestListener;
import com.implementhit.OptimizeHIT.fragments.SolutionFragment;
import com.implementhit.OptimizeHIT.models.Notification;
import com.implementhit.OptimizeHIT.models.Settings;
import com.implementhit.OptimizeHIT.models.Solution;
import com.implementhit.OptimizeHIT.models.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class DBTalker extends SQLiteOpenHelper {
    public static final String EXTRA_NOTIFICATION_ID = "notificationId";
    public static final String EXTRA_NOTIFICATION = "notification";

    private static final String DB_NAME = "OptimizeHit";
    private static final int DB_VERSION = 13;

    // Database table names
    private final String CATEGORIES = "categories";
    private final String SUB_CATEGORIES = "sub_categories";
    private final String SOLUTIONS = "solutions";
    private final String FAVORITES = "favorites";
    private final String PEER_FAVORITES = "peer_favorites";
    private final String LIKES = "likes";
    private final String HISTORY = "history";
    private final String QUESTIONS = "questions";
    private final String SUGGESTIONS = "suggestions";
    private final String SETTINGS = "settings";
    private final String NOTIFICATIONS = "notifications";
    private final String BANNER_NOTIFICATIONS = "badgeNotifications";

    // Database column names
    private final String ID = "id";
    private final String NAME = "name";
    private final String TEXT = "text";
    private final String CORRESPOND_SOLUTION_ID = "solution_id";
    private final String CORRESPOND_CATEGORY_ID = "category_id";
    private final String CORRESPOND_SUB_CATEGORY_ID = "subcategory_id";
    private final String PAGE_TO_OPEN = "pageToOpen";
    private final String CORRESPOND_SUGGESTION = "sug";
    private final String WEIGHT = "weight";
    private final String CATEGORY = "category";
    private final String SUB_CATEGORY = "sub_category";
    private final String FULL_TITLE = "full_title";
    private final String SAVE_DATE = "saveDate";
    private final String TIMESTEMP = "ts";
    private final String IS_VOICE = "isVoice";
    private final String DOMAIN = "domain";
    private final String DATE = "date";
    private final String TYPE = "type";
    private final String VIEWED = "viewed";
    private final String DEFAULT_SCREEN = "defaultScreen";
    private final String PLAY_AUTO = "playAuto";
    private final String PLAY_SPEED = "playSpeed";
    private final String IS_GROUPING = "isGrouping";
    private final String IS_ENABLE_GROUPING_MESSAGE = "isEnableGroupingMessage";
    private final String CME = "cme";
    private final String RATING = "rating";
    private final String SKIP_SUBCATEGORY = "skip_sc";
    // This string is peer favorites data
    private final String FAVS = "favs";

    private final int SELECT_BLOCK_LIMIT = 500;

    private static DBTalker dbTalker;
    private User user;
    private Context context;

    private DBTalker(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

        this.context = context.getApplicationContext();
        this.user = User.sharedUser(context);

    }

    public static void removeLink() {
        dbTalker = null;
    }

    public static DBTalker sharedDB(Context context) {
        if (dbTalker == null) {
            dbTalker = new DBTalker(context);
        }

        return dbTalker;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String categoriesTable = " CREATE TABLE " + CATEGORIES + " ( "
                + ID + " INTEGER PRIMARY KEY NOT NULL, "
                + NAME + " TEXT NOT NULL, "
                + SKIP_SUBCATEGORY + " INTEGER NOT NULL, "
                + WEIGHT + " INTEGER NOT NULL ); ";

        String subCategoriesTable = " CREATE TABLE " + SUB_CATEGORIES + " ( "
                + ID + " INTEGER PRIMARY KEY NOT NULL, "
                + NAME + " TEXT NOT NULL, "
                + WEIGHT + " INTEGER NOT NULL ); ";

        String solutionsTable = " CREATE TABLE " + SOLUTIONS + " ( "
                + ID + " INTEGER PRIMARY KEY NOT NULL, "
                + NAME + " TEXT NOT NULL, "
                + WEIGHT + " INTEGER NOT NULL, "
                + CATEGORY + " INTEGER NOT NULL, "
                + SUB_CATEGORY + " INTEGER NOT NULL, "
                + CME + " INTEGER NOT NULL, "
                + RATING + " REAL NOT NULL, "
                + FULL_TITLE + " TEXT NOT NULL ); ";

        String suggestionsTable = "CREATE TABLE " + SUGGESTIONS + " ( "
                + ID + " INTEGER NOT NULL, "
                + NAME + " TEXT NOT NULL, "
                + WEIGHT + " INTEGER NOT NULL, "
                + DATE + " BIGINTEGER NOT NULL, "
                + VIEWED + " INTEGER NOT NULL, "
                + TYPE + " INTEGER NOT NULL, "
                + " PRIMARY KEY ( " + TYPE + " , " + ID + " ) ); ";

        String favoritesTable = " CREATE TABLE " + FAVORITES + " ( "
                + ID + " INTEGER PRIMARY KEY NOT NULL, "
                + RATING + " REAL NOT NULL, "
                + TIMESTEMP + " BIGINTEGER NOT NULL ); ";

        String peerFavoritesTable = " CREATE TABLE " + PEER_FAVORITES + " ( "
                + ID + " INTEGER PRIMARY KEY NOT NULL, "
                + FULL_TITLE + " TEXT NOT NULL , "
                + FAVS + " INTEGER NOT NULL, "
                + LIKES + " INTEGER NOT NULL ); ";

        String historyTable = " CREATE TABLE " + HISTORY + " ( "
                + ID + " INTEGER PRIMARY KEY NOT NULL, "
                + NAME + " TEXT NOT NULL, "
                + DOMAIN + " TEXT NOT NULL, "
                + SAVE_DATE + " BIGINTEGER NOT NULL, "
                + IS_VOICE + " INTEGER NOT NULL ); ";

        String questionsTable = "CREATE TABLE " + QUESTIONS + " ( "
                + NAME + " TEXT PRIMARY KEY NOT NULL ); ";

        String settingsTable = "CREATE TABLE " + SETTINGS + " ( "
                + DOMAIN + " TEXT PRIMARY KEY NOT NULL, "
                + PLAY_AUTO + " INTEGER NOT NULL, "
                + PLAY_SPEED + " REAL NOT NULL, "
                + IS_GROUPING + " INTEGER NOT NULL, "
                + IS_ENABLE_GROUPING_MESSAGE + " INTEGER NOT NULL, "
                + DEFAULT_SCREEN + " INTEGER NOT NULL ); ";

        String notificationsTable = "CREATE TABLE " + NOTIFICATIONS + " ( "
                + ID + " INTEGER PRIMARY KEY NOT NULL, "
                + TEXT + " TEXT NOT NULL, "
                + CORRESPOND_SOLUTION_ID + " TEXT NOT NULL, "
                + CORRESPOND_CATEGORY_ID + " INTEGER NOT NULL, "
                + CORRESPOND_SUB_CATEGORY_ID + " INTEGER NOT NULL, "
                + PAGE_TO_OPEN + " INTEGER NOT NULL, "
                + CORRESPOND_SUGGESTION + " INTEGER NOT NULL DEFAULT 0, "
                + DATE + " BIGINTEGER NOT NULL, "
                + VIEWED + " INTEGER NOT NULL ); ";

        String badgeNotificationsTable = "CREATE TABLE " + BANNER_NOTIFICATIONS + " ( "
                + ID + " INTEGER PRIMARY KEY NOT NULL,"
                + DATE + " BIGINTEGER NOT NULL );";

        db.execSQL(categoriesTable);
        db.execSQL(subCategoriesTable);
        db.execSQL(solutionsTable);
        db.execSQL(favoritesTable);
        db.execSQL(peerFavoritesTable);
        db.execSQL(historyTable);
        db.execSQL(questionsTable);
        db.execSQL(suggestionsTable);
        db.execSQL(settingsTable);
        db.execSQL(notificationsTable);
        db.execSQL(badgeNotificationsTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2 && newVersion > 1) {
            db.execSQL("ALTER TABLE "
                    + HISTORY + " ADD COLUMN "
                    + DOMAIN + " TEXT NOT NULL "
                    + " DEFAULT " + DatabaseUtils.sqlEscapeString(user.domain()));
        }
        if (oldVersion < 3 && newVersion > 2) {
            db.execSQL("CREATE TABLE " + QUESTIONS + " ( "
                    + NAME + " TEXT PRIMARY KEY NOT NULL ); ");

            db.execSQL("CREATE TABLE " + SUGGESTIONS + " ( "
                    + ID + " INTEGER NOT NULL, "
                    + NAME + " TEXT NOT NULL, "
                    + WEIGHT + " INTEGER NOT NULL, "
                    + DATE + " BIGINTEGER NOT NULL, "
                    + VIEWED + " INTEGER NOT NULL, "
                    + TYPE + " INTEGER NOT NULL, "
                    + " PRIMARY KEY ( " + TYPE + " , " + ID + " ) ); ");

            db.execSQL("CREATE TABLE " + SETTINGS + " ( "
                    + DOMAIN + " TEXT PRIMARY KEY NOT NULL, "
                    + PLAY_AUTO + " INTEGER NOT NULL, "
                    + PLAY_SPEED + " REAL NOT NULL, "
                    + DEFAULT_SCREEN + " INTEGER NOT NULL ); ");
        }
        if (oldVersion < 4 && newVersion > 3) {
            db.execSQL("ALTER TABLE " + SETTINGS + " ADD COLUMN "
                    + IS_GROUPING + " INTEGER NOT NULL "
                    + " DEFAULT 0;");
            db.execSQL("ALTER TABLE " + SETTINGS + " ADD COLUMN "
                    + IS_ENABLE_GROUPING_MESSAGE + " INTEGER NOT NULL "
                    + " DEFAULT 1;");
        }
        if (oldVersion < 5 && newVersion > 4) {
            db.execSQL("CREATE TABLE " + NOTIFICATIONS + " ( "
                    + ID + " INTEGER PRIMARY KEY NOT NULL, "
                    + TEXT + " TEXT NOT NULL, "
                    + CORRESPOND_SOLUTION_ID + " TEXT NOT NULL, "
                    + DATE + " INTEGER NOT NULL, "
                    + VIEWED + " INTEGER NOT NULL ); ");
            db.execSQL("UPDATE " + SETTINGS
                    + " SET " + DEFAULT_SCREEN + " = " + DEFAULT_SCREEN + " + 1 ");
        }
        if (oldVersion < 6 && newVersion > 5) {
            db.execSQL("UPDATE " + HISTORY
                    + " SET " + DOMAIN + " = "
                    + DatabaseUtils.sqlEscapeString(user.username())
                    + " WHERE " + DOMAIN + " = "
                    + DatabaseUtils.sqlEscapeString(user.domain()) + ";");
            db.execSQL("UPDATE " + SETTINGS
                    + " SET " + DOMAIN + " = "
                    + DatabaseUtils.sqlEscapeString(user.username())
                    + " WHERE " + DOMAIN + " = "
                    + DatabaseUtils.sqlEscapeString(user.domain()) + ";");
        }
        if (oldVersion < 7 && newVersion > 6) {
            db.execSQL("CREATE TABLE " + BANNER_NOTIFICATIONS + " ( "
                    + ID + " INTEGER PRIMARY KEY NOT NULL,"
                    + DATE + " BIGINTEGER NOT NULL );");
        }
        if (oldVersion < 8 && newVersion > 7) {
            db.execSQL("ALTER TABLE " + NOTIFICATIONS + " ADD COLUMN "
                    + CORRESPOND_CATEGORY_ID + " INTEGER NOT NULL "
                    + " DEFAULT -1;");
            db.execSQL("ALTER TABLE " + NOTIFICATIONS + " ADD COLUMN "
                    + CORRESPOND_SUB_CATEGORY_ID + " INTEGER NOT NULL "
                    + " DEFAULT -1;");
        }
        if (oldVersion < 9 && newVersion > 8) {
            db.execSQL("ALTER TABLE " + NOTIFICATIONS + " ADD COLUMN "
                    + CORRESPOND_SUGGESTION + " INTEGER NOT NULL "
                    + " DEFAULT 0;");
        }
        if (oldVersion < 10 && newVersion > 9) {
            db.execSQL("ALTER TABLE " + CATEGORIES + " ADD COLUMN "
                    + SKIP_SUBCATEGORY + " INTEGER NOT NULL "
                    + " DEFAULT 0;");
            db.execSQL("ALTER TABLE " + SOLUTIONS + " ADD COLUMN "
                    + CME + " INTEGER NOT NULL "
                    + " DEFAULT 0;");
        }
        if (oldVersion < 11 && newVersion > 10) {
            db.execSQL("DROP TABLE " + LIKES);

            try {
                db.execSQL("ALTER TABLE " + SOLUTIONS + " ADD COLUMN "
                        + RATING + " REAL NOT NULL "
                        + " DEFAULT 0;");
            } catch (Exception e) {

            }

            try {
                db.execSQL("ALTER TABLE " + FAVORITES + " ADD COLUMN "
                        + RATING + " REAL NOT NULL "
                        + " DEFAULT 0;");
            } catch (Exception e) {

            }

            try {
                db.execSQL(" CREATE TABLE " + PEER_FAVORITES + " ( "
                        + ID + " INTEGER PRIMARY KEY NOT NULL, "
                        + FULL_TITLE + " TEXT NOT NULL , "
                        + FAVS + " INTEGER NOT NULL, "
                        + LIKES + " INTEGER NOT NULL ); ");
            } catch (Exception e) {

            }
        }
        if (oldVersion < 12 && newVersion > 11) {
            try {
                db.execSQL("ALTER TABLE " + NOTIFICATIONS + " ADD COLUMN "
                        + PAGE_TO_OPEN + " INTEGER NOT NULL "
                        + " DEFAULT 0;");
            } catch (Exception e) {

            }
        }
        if (oldVersion < 13 && newVersion > 12) {
            try {
                db.execSQL("ALTER TABLE " + FAVORITES + " ADD COLUMN "
                        + TIMESTEMP + " BIGINTEGER NOT NULL "
                        + " DEFAULT 1;");
            } catch (Exception e) {

            }
        }
    }

    public void insertQuestions(String[] questions) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        writableDatabase.delete(QUESTIONS, null, null);

        StringBuilder questionsQuery = new StringBuilder("REPLACE INTO "
                + QUESTIONS + " ( " + NAME + " )  VALUES ");

        for (int index = 0; index < questions.length; index++) {
            questionsQuery = questionsQuery.append("( ")
                    .append(DatabaseUtils.sqlEscapeString(questions[index]))
                    .append(")");

            if (index != questions.length - 1) {
                questionsQuery = questionsQuery.append(",");
            }
        }
        writableDatabase.execSQL(questionsQuery.toString());

        Intent intent = new Intent();
        intent.setAction(APITalker.ACTION_SHOW_QUESTIONS);
        context.sendBroadcast(intent);
    }

    public void insertSuggestions(final ArrayList<Solution> reactivations,
                                  final ArrayList<Solution> suggestions,
                                  final ArrayList<Solution> locationBased,
                                  final int viewedCount,
                                  final SuggestedLearningRequestListener listener) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                SQLiteDatabase writableDatabase = getWritableDatabase();
                writableDatabase.delete(SUGGESTIONS, null, null);

                if (reactivations.size() > 0) {
                    String[] reactivationsQueries = insertSuggestionsQuery(reactivations, 1);
                    for (int block = reactivationsQueries.length - 1; block >= 0; block--) {
                        writableDatabase.execSQL(reactivationsQueries[block]);
                    }
                }
                if (suggestions.size() > 0) {
                    String[] suggestionsQueries = insertSuggestionsQuery(suggestions, 2);
                    for (int block = suggestionsQueries.length - 1; block >= 0; block--) {
                        writableDatabase.execSQL(suggestionsQueries[block]);
                    }
                }
                if (locationBased.size() > 0) {
                    String[] locationBasedQueries = insertSuggestionsQuery(
                            locationBased, 3);
                    for (int block = locationBasedQueries.length - 1; block >= 0; block--) {
                        writableDatabase.execSQL(locationBasedQueries[block]);
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                Intent intent = new Intent();
                intent.setAction(APITalker.ACTION_SUGGESTED_LEARNING_UPDATED);
                context.sendBroadcast(intent);
                
                if (listener != null) {
                    listener.onSuggestedLearningSuccess(reactivations, suggestions, locationBased, viewedCount);
                }
            }

        }.execute((Void) null);
    }

    public void insertBadgeNotification(int noficiationId) {
        String query = "REPLACE INTO " + BANNER_NOTIFICATIONS + " ( "
                + ID + " , " + DATE + " ) VALUES ( " + noficiationId + " , " + System.currentTimeMillis() + " ); ";

        SQLiteDatabase writableDatabase = getWritableDatabase();
        writableDatabase.execSQL(query);
    }

    public void insertNotification(Notification notification) {
        if (notification != null) {
            String notificationQuery = insertNotificationQuery(notification);

            SQLiteDatabase writableDatabase = getWritableDatabase();
            writableDatabase.execSQL(notificationQuery);
        }
    }

    public void insertNotifications(final ArrayList<Notification> notifications, final NotificationsRequestListener handler) {
        new AsyncTask<Void, Notification, Notification>() {

            @Override
            protected Notification doInBackground(Void... params) {

//				long lastNotificationDate = 0;
//
//				ArrayList<Notification> currentNotifications = getNotifications();
//
//				if (currentNotifications.size() > 0) {
//					lastNotificationDate = currentNotifications.get(0).getNotificationDate();
//				}

                SQLiteDatabase writableDatabase = getWritableDatabase();

                writableDatabase.delete(NOTIFICATIONS, null, null);

                if (notifications.size() > 0) {
                    String[] notificationsQueries = insertNotificationsQuery(notifications);
                    for (int block = notificationsQueries.length - 1; block >= 0; block--) {
                        writableDatabase.execSQL(notificationsQueries[block]);
                    }
                }

//				ArrayList<Notification> newNotifications = getNotifications();

//				if (newNotifications.size() > 0) {
//					ArrayList<Notification> notificationsToShow = getNotifications(1, lastNotificationDate, true);
//
//					if (notificationsToShow.size() > 0) {
//						return notificationsToShow.get(0);
//					}
//				}

                return null;
            }

            protected void onPostExecute(Notification result) {
                if (handler == null) {
                    Intent intent = new Intent();
                    intent.setAction(APITalker.ACTION_UPDATE_NOTIFICATIONS);
                    context.sendBroadcast(intent);
                } else {
                    handler.notificationsSuccess(notifications, result);
                }
            }
        }.execute((Void) null);
    }

    public void insertUserHistory(final JSONObject userHistory) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                JSONArray favorites = userHistory.optJSONArray(FAVORITES);
                JSONArray history = userHistory.optJSONArray(HISTORY);

                SQLiteDatabase writableDatabase = getWritableDatabase();
                writableDatabase.delete(FAVORITES, null, null);
//                writableDatabase.delete(HISTORY, null, null);

                if (favorites.length() > 0) {
                    String[] favoritesQueries = insertFavoritesQuery(favorites);
                    for (int block = favoritesQueries.length - 1; block >= 0; block--) {
                        writableDatabase.execSQL(favoritesQueries[block]);
                    }
                }
                if (history.length() > 0) {
                    String[] historyQueries = insertHistoryQuery(history);
                    for (int block = historyQueries.length - 1; block >= 0; block--) {
                        writableDatabase.execSQL(historyQueries[block]);
                    }
                }

                return null;

            }

            @Override
            protected void onPostExecute(Void result) {
                Intent intent = new Intent();
                intent.setAction(APITalker.ACTION_UPDATE_HISTORY);
                context.sendBroadcast(intent);
            }

        }.execute((Void) null);

    }

    public void insertPeerFavorites(final JSONArray peerFavorites) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                SQLiteDatabase writableDatabase = getWritableDatabase();
                writableDatabase.delete(PEER_FAVORITES, null, null);

                if (peerFavorites.length() > 0) {
                    String[] favoritesQueries = insertPeerFavoritesQuery(peerFavorites);
                    for (int block = favoritesQueries.length - 1; block >= 0; block--) {
                        writableDatabase.execSQL(favoritesQueries[block]);
                    }
                }

                return null;

            }

            @Override
            protected void onPostExecute(Void result) {
                Intent intent = new Intent();
                intent.setAction(APITalker.ACTION_UPDATE_PEER_FAVORITES);
                context.sendBroadcast(intent);
            }

        }.execute((Void) null);

    }

    public void insertData(final JSONObject data, final boolean needsDrop, final DownloadDataRequestListener handler) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                SQLiteDatabase writableDatabase = getWritableDatabase();

                if (needsDrop) {
                    writableDatabase.delete(CATEGORIES, null, null);
                    writableDatabase.delete(SUB_CATEGORIES, null, null);
                    writableDatabase.delete(SOLUTIONS, null, null);
                }

                JSONArray solutions = data.optJSONArray(SOLUTIONS);
                JSONArray categories = data.optJSONArray(CATEGORIES);
                JSONArray subCategories = data.optJSONArray(SUB_CATEGORIES);

                if (categories.length() > 0) {
                    String[] categoryQueries = insertCategoriesQuery(categories);
                    for (int block = categoryQueries.length - 1; block >= 0; block--) {
                        writableDatabase.execSQL(categoryQueries[block]);
                    }
                }
                if (subCategories.length() > 0) {
                    String[] subCategoryQueries = insertSubCategoriesQuery(subCategories);
                    for (int block = subCategoryQueries.length - 1; block >= 0; block--) {
                        writableDatabase.execSQL(subCategoryQueries[block]);
                    }
                }
                if (solutions.length() > 0) {
                    String[] solutionsQueries = insertSolutionsQuery(solutions);
                    for (int block = solutionsQueries.length - 1; block >= 0; block--) {
                        writableDatabase.execSQL(solutionsQueries[block]);
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (handler != null) {
                    handler.onDownloadSuccess();
                } else {
                    Intent intent = new Intent();
                    intent.setAction(APITalker.ACTION_NEW_SOLUTION_DATA);
                    context.sendBroadcast(intent);
                }
            }

        }.execute((Void) null);

    }

    private String[] insertSolutionsQuery(JSONArray solutions) {
        int blockNumber = solutions.length() / SELECT_BLOCK_LIMIT + 1;
        int entitiesOffset = solutions.length();
        String[] queries = new String[blockNumber];

        for (int blockIndex = 0; blockIndex < blockNumber; blockIndex++) {
            StringBuilder solutionsQuery = new StringBuilder("REPLACE INTO "
                    + SOLUTIONS + " ( " + ID + "," + NAME + "," + FULL_TITLE
                    + "," + SUB_CATEGORY + "," + CATEGORY + "," + WEIGHT
                    + "," + CME + "," + RATING + " )  VALUES ");

            JSONObject solution;
            int limit = entitiesOffset - SELECT_BLOCK_LIMIT > 0 ? entitiesOffset
                    - SELECT_BLOCK_LIMIT
                    : 0;

            String ratingString;
            float rating;
            for (int index = entitiesOffset - 1; index >= limit; index--) {
                solution = solutions.optJSONObject(index);

                ratingString = solution.optString(RATING);

                //  if RATING == null , optString() returns "null", not null, so we do in this way.
                if (!ratingString.equals("null")) {
                    rating = Float.valueOf(ratingString);
                } else {
                    rating = -1;
                }

                solutionsQuery = solutionsQuery
                        .append("( ")
                        .append(solution.optInt(ID)).append(",")
                        .append(DatabaseUtils.sqlEscapeString(solution.optString(NAME))).append(",")
                        .append(DatabaseUtils.sqlEscapeString(solution.optString(FULL_TITLE))).append(",")
                        .append(solution.optInt(SUB_CATEGORY)).append(",")
                        .append(solution.optInt(CATEGORY)).append(",")
                        .append(solution.optInt(WEIGHT)).append(",")
                        .append(solution.optInt(CME)).append(",")
                        .append(rating).append(")");

                if (index != limit) {
                    solutionsQuery = solutionsQuery.append(",");
                }
            }

            entitiesOffset = entitiesOffset - SELECT_BLOCK_LIMIT;

            solutionsQuery = solutionsQuery.append(";");
            queries[blockIndex] = solutionsQuery.toString();

        }

        return queries;

    }

    private String[] insertCategoriesQuery(JSONArray categories) {
        int blockNumber = categories.length() / SELECT_BLOCK_LIMIT + 1;
        int entitiesOffset = categories.length();
        String[] queries = new String[blockNumber];

        for (int blockIndex = 0; blockIndex < blockNumber; blockIndex++) {
            StringBuilder categoriesQuery = new StringBuilder("REPLACE INTO "
                    + CATEGORIES + " ( " + ID + "," + NAME + "," + SKIP_SUBCATEGORY + "," + WEIGHT
                    + " )  VALUES ");

            JSONObject category;
            int limit = entitiesOffset - SELECT_BLOCK_LIMIT > 0 ? entitiesOffset
                    - SELECT_BLOCK_LIMIT
                    : 0;

            for (int index = entitiesOffset - 1; index >= limit; index--) {
                category = categories.optJSONObject(index);

                categoriesQuery = categoriesQuery
                        .append("( ")
                        .append(category.optInt(ID))
                        .append(",")
                        .append(DatabaseUtils.sqlEscapeString(category.optString(NAME))).append(",")
                        .append(category.optInt(SKIP_SUBCATEGORY)).append(",")
                        .append(category.optInt(WEIGHT)).append(" )");

                if (index != limit) {
                    categoriesQuery = categoriesQuery.append(",");
                }
            }

            entitiesOffset = entitiesOffset - SELECT_BLOCK_LIMIT;

            categoriesQuery = categoriesQuery.append(";");
            queries[blockIndex] = categoriesQuery.toString();
        }

        return queries;
    }

    private String[] insertSubCategoriesQuery(JSONArray subCategories) {
        int blockNumber = subCategories.length() / SELECT_BLOCK_LIMIT + 1;
        int entitiesOffset = subCategories.length();
        String[] queries = new String[blockNumber];

        for (int blockIndex = 0; blockIndex < blockNumber; blockIndex++) {
            StringBuilder subCategoriesQuery = new StringBuilder(
                    "REPLACE INTO " + SUB_CATEGORIES + " ( " + ID + "," + NAME
                            + "," + WEIGHT + " )  VALUES ");

            JSONObject subCategory;
            int limit = entitiesOffset - SELECT_BLOCK_LIMIT > 0 ? entitiesOffset
                    - SELECT_BLOCK_LIMIT
                    : 0;

            for (int index = entitiesOffset - 1; index >= limit; index--) {
                subCategory = subCategories.optJSONObject(index);

                subCategoriesQuery = subCategoriesQuery
                        .append("( ")
                        .append(subCategory.optInt(ID))
                        .append(",")
                        .append(DatabaseUtils.sqlEscapeString(subCategory
                                .optString(NAME))).append(",")
                        .append(subCategory.optInt(WEIGHT)).append(" )");

                if (index != limit) {
                    subCategoriesQuery = subCategoriesQuery.append(",");
                }
            }

            entitiesOffset = entitiesOffset - SELECT_BLOCK_LIMIT;

            subCategoriesQuery = subCategoriesQuery.append(";");
            queries[blockIndex] = subCategoriesQuery.toString();
        }

        return queries;
    }

    private String[] insertFavoritesQuery(JSONArray favorites) {
        int blockNumber = favorites.length() / SELECT_BLOCK_LIMIT + 1;
        int entitiesOffset = favorites.length();
        String[] queries = new String[blockNumber];

        for (int blockIndex = 0; blockIndex < blockNumber; blockIndex++) {
            StringBuilder favoritesQuery = new StringBuilder(
                    "INSERT INTO "
                            + FAVORITES
                            + " ( "
                            + ID + ","
                            + RATING + ","
                            + TIMESTEMP
                            + " )  VALUES ");

            int limit = entitiesOffset - SELECT_BLOCK_LIMIT > 0 ? entitiesOffset
                    - SELECT_BLOCK_LIMIT
                    : 0;

            JSONObject favoritesObject;
            int solutionId;
            double rating;
            long timestamp;
            for (int index = entitiesOffset - 1; index >= limit; index--) {
                favoritesObject = favorites.optJSONObject(index);

                if (favoritesObject.opt(RATING) instanceof String) {
                    rating = Double.valueOf(favoritesObject.optString(RATING));
                } else {
                    rating = favoritesObject.optDouble(RATING);
                }

                if (favoritesObject.opt(CORRESPOND_SOLUTION_ID) instanceof String) {
                    solutionId = Integer.valueOf(favoritesObject.optString(CORRESPOND_SOLUTION_ID));
                } else {
                    solutionId = favoritesObject.optInt(CORRESPOND_SOLUTION_ID);
                }

                if (favoritesObject.opt(TIMESTEMP) instanceof String) {
                    timestamp = Long.valueOf(favoritesObject.optString(TIMESTEMP));
                } else {
                    timestamp = favoritesObject.optLong(TIMESTEMP);
                }

                favoritesQuery = favoritesQuery
                        .append("( ")
                        .append(solutionId).append(",")
                        .append(rating).append(",")
                        .append(timestamp)
                        .append(")");

                if (index != limit) {
                    favoritesQuery = favoritesQuery.append(",");
                }
            }

            entitiesOffset = entitiesOffset - SELECT_BLOCK_LIMIT;

            favoritesQuery = favoritesQuery.append(";");
            queries[blockIndex] = favoritesQuery.toString();
        }

        return queries;
    }

    private String[] insertPeerFavoritesQuery(JSONArray peerFavorites) {
        int blockNumber = peerFavorites.length() / SELECT_BLOCK_LIMIT + 1;
        int entitiesOffset = peerFavorites.length();
        String[] queries = new String[blockNumber];

        for (int blockIndex = 0; blockIndex < blockNumber; blockIndex++) {
            StringBuilder peerFavoritesQuery = new StringBuilder(
                    "INSERT INTO "
                            + PEER_FAVORITES
                            + " ( "
                            + ID + ","
                            + FULL_TITLE + ","
                            + FAVS + ","
                            + LIKES
                            + " )  VALUES ");

            int limit = entitiesOffset - SELECT_BLOCK_LIMIT > 0 ? entitiesOffset
                    - SELECT_BLOCK_LIMIT
                    : 0;

            JSONObject peerFavoritesObject;
            int solutionId;
            String fullTitle;
            int favs;
            int likes;

            for (int index = entitiesOffset - 1; index >= limit; index--) {
                peerFavoritesObject = peerFavorites.optJSONObject(index);

                if (peerFavoritesObject.opt(ID) instanceof String) {
                    solutionId = Integer.valueOf(peerFavoritesObject.optString(ID));
                } else {
                    solutionId = peerFavoritesObject.optInt(ID);
                }

                fullTitle = peerFavoritesObject.optString(FULL_TITLE);

                if (peerFavoritesObject.opt(FAVS) instanceof String) {
                    favs = Integer.valueOf(peerFavoritesObject.optString(FAVS));
                } else {
                    favs = peerFavoritesObject.optInt(FAVS);
                }

                if (peerFavoritesObject.opt(LIKES) instanceof String) {
                    likes = Integer.valueOf(peerFavoritesObject.optString(LIKES));
                } else {
                    likes = peerFavoritesObject.optInt(LIKES);
                }

                peerFavoritesQuery = peerFavoritesQuery
                        .append("( ")
                        .append(solutionId).append(",")
                        .append(DatabaseUtils.sqlEscapeString(fullTitle)).append(",")
                        .append(favs).append(",")
                        .append(likes)
                        .append(")");

                if (index != limit) {
                    peerFavoritesQuery = peerFavoritesQuery.append(",");
                }
            }

            entitiesOffset = entitiesOffset - SELECT_BLOCK_LIMIT;

            peerFavoritesQuery = peerFavoritesQuery.append(";");
            queries[blockIndex] = peerFavoritesQuery.toString();
        }

        return queries;
    }

    private String[] insertSuggestionsQuery(ArrayList<Solution> suggestions, int type) {
        int blockNumber = suggestions.size() / SELECT_BLOCK_LIMIT + 1;
        int entitiesOffset = suggestions.size();
        String[] queries = new String[blockNumber];

        for (int blockIndex = 0; blockIndex < blockNumber; blockIndex++) {
            StringBuilder solutionsQuery = new StringBuilder("REPLACE INTO "
                    + SUGGESTIONS + " ( " + ID + " , " + NAME + " , " + WEIGHT
                    + " , " + DATE + " , " + VIEWED + " , " + TYPE
                    + " )  VALUES ");

            Solution solution;
            int limit = entitiesOffset - SELECT_BLOCK_LIMIT > 0 ? entitiesOffset
                    - SELECT_BLOCK_LIMIT
                    : 0;

            for (int index = entitiesOffset - 1; index >= limit; index--) {
                solution = suggestions.get(index);

                solutionsQuery = solutionsQuery
                        .append("( ")
                        .append(solution.solutionId())
                        .append(",")
                        .append(DatabaseUtils.sqlEscapeString(solution.title()))
                        .append(",").append(solution.weight()).append(",")
                        .append(solution.date()).append(",")
                        .append(solution.viewed() ? 1 : 0).append(",")
                        .append(type).append(")");

                if (index != limit) {
                    solutionsQuery = solutionsQuery.append(",");
                }
            }

            entitiesOffset = entitiesOffset - SELECT_BLOCK_LIMIT;

            solutionsQuery = solutionsQuery.append(";");
            queries[blockIndex] = solutionsQuery.toString();
        }

        return queries;
    }

    private String insertNotificationQuery(Notification notification) {

        StringBuilder notificationQuery = new StringBuilder("INSERT OR IGNORE INTO "
                + NOTIFICATIONS + " ( " + ID + " , " + TEXT + " , " + CORRESPOND_SOLUTION_ID + " , "
                + CORRESPOND_CATEGORY_ID + " , " + CORRESPOND_SUB_CATEGORY_ID + " , "
                + PAGE_TO_OPEN + " , " + DATE + " , " + VIEWED
                + " )  VALUES ");

        String correspondSolutionId;

        correspondSolutionId = notification.getCorrespondSolutionId();

        if (correspondSolutionId.equals("null")) {
            correspondSolutionId = "";
        }

        notificationQuery = notificationQuery
                .append("( ")
                .append(notification.getNotificationId()).append(",")
                .append(DatabaseUtils.sqlEscapeString(notification.getNotificationText())).append(",")
                .append(DatabaseUtils.sqlEscapeString(correspondSolutionId)).append(",")
                .append(notification.getCorrespondCategoryId()).append(",")
                .append(notification.getCorrespondSubCategoryId()).append(",")
                .append(notification.getPageToOpen()).append(",")
                .append(notification.getNotificationDate()).append(",")
                .append(notification.isRead() ? 1 : 0).append(")");

        notificationQuery = notificationQuery.append(";");

        return notificationQuery.toString();
    }

    private String[] insertNotificationsQuery(
            ArrayList<Notification> notifications) {
        int blockNumber = notifications.size() / SELECT_BLOCK_LIMIT + 1;
        int entitiesOffset = notifications.size();
        String[] queries = new String[blockNumber];

        for (int blockIndex = 0; blockIndex < blockNumber; blockIndex++) {
            StringBuilder notificationsQuery = new StringBuilder(
                    "REPLACE INTO " + NOTIFICATIONS + " ( "
                            + ID + " , " + TEXT + " , " + CORRESPOND_SOLUTION_ID + " , "
                            + CORRESPOND_CATEGORY_ID + " , " + CORRESPOND_SUB_CATEGORY_ID + " , "
                            + PAGE_TO_OPEN + " , " + DATE + " , " + VIEWED + " )  VALUES ");

            Notification notification;
            int limit = entitiesOffset - SELECT_BLOCK_LIMIT > 0 ? entitiesOffset
                    - SELECT_BLOCK_LIMIT
                    : 0;
            String correspondSolutionId;
            for (int index = entitiesOffset - 1; index >= limit; index--) {
                notification = notifications.get(index);

                correspondSolutionId = notification.getCorrespondSolutionId();
                if (correspondSolutionId.equals("null")) {
                    correspondSolutionId = "";
                }
                notificationsQuery = notificationsQuery
                        .append("( ")
                        .append(notification.getNotificationId()).append(",")
                        .append(DatabaseUtils.sqlEscapeString(notification.getNotificationText())).append(",")
                        .append(DatabaseUtils.sqlEscapeString(correspondSolutionId)).append(",")
                        .append(notification.getCorrespondCategoryId()).append(",")
                        .append(notification.getCorrespondSubCategoryId()).append(",")
                        .append(notification.getPageToOpen()).append(",")
                        .append(notification.getNotificationDate()).append(",")
                        .append(notification.isRead() ? 1 : 0).append(")");

                if (index != limit) {
                    notificationsQuery = notificationsQuery.append(",");
                }
            }

            entitiesOffset = entitiesOffset - SELECT_BLOCK_LIMIT;

            notificationsQuery = notificationsQuery.append(";");
            queries[blockIndex] = notificationsQuery.toString();
        }

        return queries;
    }

    private String[] insertHistoryQuery(JSONArray history) {
        int blockNumber = history.length() / SELECT_BLOCK_LIMIT + 1;
        int entitiesOffset = history.length();
        String[] queries = new String[blockNumber];

        for (int blockIndex = 0; blockIndex < blockNumber; blockIndex++) {
            StringBuilder historyQuery = new StringBuilder("REPLACE INTO "
                    + HISTORY + " ( " + ID + "," + SAVE_DATE + " )  VALUES ");

            JSONObject historyObject;
            int limit = entitiesOffset - SELECT_BLOCK_LIMIT > 0 ? entitiesOffset
                    - SELECT_BLOCK_LIMIT
                    : 0;

            for (int index = entitiesOffset - 1; index >= limit; index--) {
                historyObject = history.optJSONObject(index);

                historyQuery = historyQuery
                        .append("( ")
                        .append(historyObject.optInt(CORRESPOND_SOLUTION_ID))
                        .append(",")
                        .append(historyObject.optInt(SAVE_DATE)).append(" )");

                if (index != limit) {
                    historyQuery = historyQuery.append(",");
                }
            }

            entitiesOffset = entitiesOffset - SELECT_BLOCK_LIMIT;

            historyQuery = historyQuery.append(";");
            queries[blockIndex] = historyQuery.toString();
        }

        return queries;
    }

    public synchronized Settings initSettings() {
        String username = user.username();

        if (username.isEmpty()) {
            return null;
        }

        String query = "SELECT * FROM " + SETTINGS + " WHERE " + DOMAIN + " = "
                + DatabaseUtils.sqlEscapeString(username);

        SQLiteDatabase writableDatabase = getWritableDatabase();
        writableDatabase.beginTransactionNonExclusive();
        Cursor cursor = writableDatabase.rawQuery(query, null);
        writableDatabase.endTransaction();

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            int defaultScreenIndex = cursor.getColumnIndex(DEFAULT_SCREEN);
            int isAutoVoiceIndex = cursor.getColumnIndex(PLAY_AUTO);
            int speechSpeedIndex = cursor.getColumnIndex(PLAY_SPEED);
            int isEnableGroupingMessage = cursor
                    .getColumnIndex(IS_ENABLE_GROUPING_MESSAGE);
            int isGroupingIndex = cursor.getColumnIndex(IS_GROUPING);

            int defaultScreen = cursor.getInt(defaultScreenIndex);
            float speechSpeed = cursor.getFloat(speechSpeedIndex);
            boolean isAutoVoice = cursor.getInt(isAutoVoiceIndex) == 1;
            boolean isEnableGrouping = cursor.getInt(isEnableGroupingMessage) == 1;
            boolean isGrouping = cursor.getInt(isGroupingIndex) == 1;

            cursor.close();

            return new Settings(defaultScreen,
                    speechSpeed,
                    isAutoVoice,
                    isEnableGrouping,
                    isGrouping);
        }

        Settings settings = new Settings(2, 1.0f, false, true, false);
        saveSettings(settings);

        return settings;
    }

    public void saveSettings(Settings settings) {
        String username = user.username();

        String query = "REPLACE INTO " + SETTINGS + " ( " + DOMAIN + ","
                + PLAY_AUTO + "," + PLAY_SPEED + ","
                + IS_ENABLE_GROUPING_MESSAGE + "," + IS_GROUPING + ","
                + DEFAULT_SCREEN + " ) VALUES ( "
                + DatabaseUtils.sqlEscapeString(username) + ","
                + (settings.isAutoStartSpeech() ? 1 : 0) + ","
                + settings.speechSpeed() + ","
                + (settings.isDisplayingEnableGrouping() ? 1 : 0) + ","
                + (settings.isGroupingEnabled() ? 1 : 0) + ","
                + settings.defaultScreen() + " ); ";

        SQLiteDatabase writableDatabase = getWritableDatabase();
        writableDatabase.execSQL(query);
    }

    /**
     * Getter methods, which help to fetch data from database
     *
     */

    public ArrayList<Solution> getCategories() {

        String query = "SELECT DISTINCT * FROM " + CATEGORIES + " ORDER BY "
                + NAME + " ASC ";

        SQLiteDatabase readableDatabase = getReadableDatabase();

        readableDatabase.beginTransactionNonExclusive();
        Cursor cursor = readableDatabase.rawQuery(query, null);
        readableDatabase.endTransaction();

        ArrayList<Solution> categories = new ArrayList<>();

        cursor.moveToFirst();

        int idKey = cursor.getColumnIndex(ID);
        int titleKey = cursor.getColumnIndex(NAME);
        int skipSubcategoryKey = cursor.getColumnIndex(SKIP_SUBCATEGORY);

        while (!cursor.isAfterLast()) {
            Solution solution = new Solution(
                    cursor.getInt(idKey),
                    cursor.getString(titleKey));
            solution.setSkipsSubcategory(cursor.getInt(skipSubcategoryKey) == 1);
            solution.setFinalSolution(false);
            categories.add(solution);
            cursor.moveToNext();
        }

        cursor.close();

        return categories;

    }

    public String getCategoryNameById( int categoryId ){

        String query = "SELECT DISTINCT * FROM " + CATEGORIES
                + " WHERE " + ID + " = " + categoryId + ";";

        SQLiteDatabase readableDatabase = getReadableDatabase();

        readableDatabase.beginTransactionNonExclusive();
        Cursor cursor = readableDatabase.rawQuery(query, null);
        readableDatabase.endTransaction();

        cursor.moveToFirst();

        int titleKey = cursor.getColumnIndex(NAME);

        String categoryName = null;
        if ( cursor.moveToFirst() && cursor.getCount() > 0 ) {
            categoryName = cursor.getString(titleKey);
        }

        cursor.close();

        return categoryName;

    }

    public ArrayList<Solution> getSubCategories(int category) {
        String query = "SELECT DISTINCT " + SUB_CATEGORIES + "." + ID + ","
                + SUB_CATEGORIES + "." + NAME + " FROM " + SUB_CATEGORIES
                + " LEFT JOIN " + SOLUTIONS + " ON " + SOLUTIONS + "."
                + SUB_CATEGORY + " = " + SUB_CATEGORIES + "." + ID
                + " WHERE "+ SOLUTIONS + "." + CATEGORY + " = " + category + " ORDER BY "
                + SUB_CATEGORIES + "." + NAME + " ASC;";


        SQLiteDatabase readableDatabase = getReadableDatabase();

        readableDatabase.beginTransactionNonExclusive();
        Cursor cursor = readableDatabase.rawQuery(query, null);
        readableDatabase.endTransaction();

        ArrayList<Solution> subCategories = new ArrayList<>();

        cursor.moveToFirst();

        int idKey = cursor.getColumnIndex(ID);
        int titleKey = cursor.getColumnIndex(NAME);

        while (!cursor.isAfterLast()) {
            Solution solution = new Solution(
                    cursor.getInt(idKey),
                    cursor.getString(titleKey));
            subCategories.add(solution);
            solution.setFinalSolution(false);
            cursor.moveToNext();
        }

        cursor.close();

        return subCategories;
    }

    public ArrayList<Solution> getSolutions(int category, int subCategory) {

        String query = "SELECT DISTINCT "
                + SOLUTIONS + "." + ID + " AS " + SOLUTIONS + "_" + ID + ","
                + SOLUTIONS + "." + NAME + " AS " + SOLUTIONS + "_" + NAME + ","
                + SOLUTIONS + "." + WEIGHT + " AS " + SOLUTIONS + "_" + WEIGHT + ","
                + SOLUTIONS + "." + CATEGORY + " AS " + SOLUTIONS + "_" + CATEGORY + ","
                + SOLUTIONS + "." + SUB_CATEGORY + " AS " + SOLUTIONS + "_" + SUB_CATEGORY + ","
                + SOLUTIONS + "." + CME + " AS " + SOLUTIONS + "_" + CME + ","
                + SOLUTIONS + "." + RATING + " AS " + SOLUTIONS + "_" + RATING + ","
                + SOLUTIONS + "." + FULL_TITLE + " AS " + SOLUTIONS + "_" + FULL_TITLE + ","
                + FAVORITES + "." + ID + " AS " + FAVORITES + "_" + ID + ","
                + FAVORITES + "." + RATING + " AS " + FAVORITES + "_" + RATING + ","
                + FAVORITES + "." + TIMESTEMP + " AS " + FAVORITES + "_" + TIMESTEMP
                + " FROM " + SOLUTIONS
                + " LEFT JOIN " + FAVORITES
                + " ON " + FAVORITES + "." + ID + " = " + SOLUTIONS + "." + ID
                + " WHERE " + CATEGORY + " = " + category;

        if (subCategory >= 0) {
            query = query + " AND " + SUB_CATEGORY + " = " + subCategory
                    + " ORDER BY " + CME + " DESC, " + FULL_TITLE + " ASC;";
        } else {
            query = query
                    + " ORDER BY " + FULL_TITLE + " ASC;";
        }

        SQLiteDatabase readableDatabase = getReadableDatabase();
        readableDatabase.beginTransactionNonExclusive();
        Cursor cursor = readableDatabase.rawQuery(query, null);
        readableDatabase.endTransaction();

        ArrayList<Solution> solutions = new ArrayList<>();

        cursor.moveToFirst();

        int idKey = cursor.getColumnIndex(SOLUTIONS + "_" + ID);
        int titleKey = cursor.getColumnIndex(SOLUTIONS + "_" + FULL_TITLE);
        int cmeKey = cursor.getColumnIndex(SOLUTIONS + "_" + CME);
        int favoritesRatingKey = cursor.getColumnIndex(FAVORITES +  "_" + RATING);
        int solutionsRatingKey = cursor.getColumnIndex(SOLUTIONS + "_" + RATING);
        int categoryIdKey = cursor.getColumnIndex(SOLUTIONS + "_" + CATEGORY);

        float rating;
        while (!cursor.isAfterLast()) {
            Solution solution = new Solution(cursor.getInt(idKey), cursor.getString(titleKey));
            solution.setHasCME(cursor.getInt(cmeKey) == 1);

            int categoryId = cursor.getInt(categoryIdKey);

            rating = cursor.getFloat(favoritesRatingKey);

            if (rating == 0.0) {
                rating = cursor.getFloat(solutionsRatingKey);
            } else {
                solution.setRatedByUser(true);
            }

            solution.setRating(rating);

            solutions.add(solution);

            cursor.moveToNext();
        }

        cursor.close();

        return solutions;

    }

    public Solution getSolutionById(int solutionId) {

        String query = "SELECT DISTINCT "
                + SOLUTIONS + "." + ID + " AS " + SOLUTIONS + "_" + ID + ","
                + SOLUTIONS + "." + NAME + " AS " + SOLUTIONS + "_" + NAME + ","
                + SOLUTIONS + "." + WEIGHT + " AS " + SOLUTIONS + "_" + WEIGHT + ","
                + SOLUTIONS + "." + CATEGORY + " AS " + SOLUTIONS + "_" + CATEGORY + ","
                + SOLUTIONS + "." + SUB_CATEGORY + " AS " + SOLUTIONS + "_" + SUB_CATEGORY + ","
                + SOLUTIONS + "." + CME + " AS " + SOLUTIONS + "_" + CME + ","
                + SOLUTIONS + "." + RATING + " AS " + SOLUTIONS + "_" + RATING + ","
                + SOLUTIONS + "." + FULL_TITLE + " AS " + SOLUTIONS + "_" + FULL_TITLE + ","
                + FAVORITES + "." + ID + " AS " + FAVORITES + "_" + ID + ","
                + FAVORITES + "." + RATING + " AS " + FAVORITES + "_" + RATING + ","
                + FAVORITES + "." + TIMESTEMP + " AS " + FAVORITES + "_" + TIMESTEMP
                + " FROM " + SOLUTIONS
                + " LEFT JOIN " + FAVORITES
                + " ON " + FAVORITES + "." + ID + " = " + SOLUTIONS + "." + ID
                + " WHERE " + SOLUTIONS + "_" + ID + " = " + solutionId;

        SQLiteDatabase readableDatabase = getReadableDatabase();
        readableDatabase.beginTransactionNonExclusive();
        Cursor cursor = readableDatabase.rawQuery(query, null);
        readableDatabase.endTransaction();

        cursor.moveToFirst();

        int idKey = cursor.getColumnIndex(SOLUTIONS + "_" + ID);
        int titleKey = cursor.getColumnIndex(SOLUTIONS + "_" + FULL_TITLE);
        int cmeKey = cursor.getColumnIndex(SOLUTIONS + "_" + CME);
        int categoryIdKey = cursor.getColumnIndex(SOLUTIONS + "_" + CATEGORY);
        int favoritesRatingKey = cursor.getColumnIndex(FAVORITES +  "_" + RATING);
        int solutionsRatingKey = cursor.getColumnIndex(SOLUTIONS + "_" + RATING);

        Solution solution = null;
        if (cursor != null) {

            solution = new Solution(cursor.getInt(idKey), cursor.getString(titleKey));
            solution.setHasCME(cursor.getInt(cmeKey) == 1);

            int categoryId = cursor.getInt(categoryIdKey);
            String categoryNameString = getCategoryNameById( categoryId );
            solution.setCategoryName(categoryNameString);

            float rating = cursor.getFloat(favoritesRatingKey);

            if (rating == 0.0) {
                rating = cursor.getFloat(solutionsRatingKey);
            } else {
                solution.setRatedByUser(true);
            }

            solution.setRating(rating);

        }

        cursor.close();

        return solution;
    }

    public ArrayList<Solution> getSuggestedSolutions(int type) {
        String query = "SELECT DISTINCT "
                + SUGGESTIONS + "." + ID + " AS " + SUGGESTIONS + "_" + ID + ","
                + SUGGESTIONS + "." + NAME + " AS " + SUGGESTIONS + "_" + NAME + ","
                + SUGGESTIONS + "." + WEIGHT + " AS " + SUGGESTIONS + "_" + WEIGHT + ","
                + SUGGESTIONS + "." + DATE + " AS " + SUGGESTIONS + "_" + DATE + ","
                + SUGGESTIONS + "." + VIEWED + " AS " + SUGGESTIONS + "_" + VIEWED + ","
                + SUGGESTIONS + "." + TYPE + " AS " + SUGGESTIONS + "_" + TYPE + ","
                + SOLUTIONS + "." + ID + " AS " + SOLUTIONS + "_" + ID + ","
                + SOLUTIONS + "." + NAME + " AS " + SOLUTIONS + "_" + NAME + ","
                + SOLUTIONS + "." + WEIGHT + " AS " + SOLUTIONS + "_" + WEIGHT + ","
                + SOLUTIONS + "." + CATEGORY + " AS " + SOLUTIONS + "_" + CATEGORY + ","
                + SOLUTIONS + "." + SUB_CATEGORY + " AS " + SOLUTIONS + "_" + SUB_CATEGORY + ","
                + SOLUTIONS + "." + CME + " AS " + SOLUTIONS + "_" + CME + ","
                + SOLUTIONS + "." + RATING + " AS " + SOLUTIONS + "_" + RATING + ","
                + SOLUTIONS + "." + FULL_TITLE + " AS " + SOLUTIONS + "_" + FULL_TITLE + ","
                + FAVORITES + "." + ID + " AS " + FAVORITES + "_" + ID + ","
                + FAVORITES + "." + RATING + " AS " + FAVORITES + "_" + RATING + ","
                + FAVORITES + "." + TIMESTEMP + " AS " + FAVORITES + "_" + TIMESTEMP + ","
                + CATEGORIES + "." + ID + " AS " + CATEGORIES + "_" + ID + ","
                + CATEGORIES + "." + NAME + " AS " + CATEGORIES + "_" + NAME + ","
                + CATEGORIES + "." + SKIP_SUBCATEGORY + " AS " + CATEGORIES + "_" + SKIP_SUBCATEGORY + ","
                + CATEGORIES + "." + WEIGHT + " AS " + CATEGORIES + "_" + WEIGHT
                + " FROM " +  SUGGESTIONS
                + " LEFT JOIN " + SOLUTIONS
                + " ON " + SUGGESTIONS + "." + ID + " = " + SOLUTIONS + "." + ID
                + " LEFT JOIN " + FAVORITES
                + " ON " + FAVORITES + "." + ID + " = " + SOLUTIONS + "." + ID
                + " LEFT JOIN " + CATEGORIES
                + " ON " + SOLUTIONS + "." + CATEGORY + " = " + CATEGORIES + "." + ID
                + " WHERE " + SUGGESTIONS + "_" + TYPE + " = " + type
                + " ORDER BY " + SUGGESTIONS + "_" + VIEWED + " ASC, " + SUGGESTIONS + "_" + WEIGHT + " ASC; ";

        SQLiteDatabase readableDatabase = getReadableDatabase();
        Cursor cursor = readableDatabase.rawQuery(query, null);
        cursor.moveToFirst();

        int idIndex = cursor.getColumnIndex(SUGGESTIONS + "_" + ID);
        int nameIndex = cursor.getColumnIndex(SUGGESTIONS + "_" + NAME);
        int weightIndex = cursor.getColumnIndex(SUGGESTIONS + "_" + WEIGHT);
        int dateIndex = cursor.getColumnIndex(SUGGESTIONS + "_" + DATE);
        int viewedIndex = cursor.getColumnIndex(SUGGESTIONS + "_" + VIEWED);
        int typeIndex = cursor.getColumnIndex(SUGGESTIONS + "_" + TYPE);
        int cmeIndex = cursor.getColumnIndex(SOLUTIONS + "_" + CME);
        int favoriteRatingIndex = cursor.getColumnIndex(FAVORITES +  "_" + RATING);
        int solutionsRatingIndex = cursor.getColumnIndex( SOLUTIONS +  "_" + RATING );
        int categoryNameIndex = cursor.getColumnIndex( CATEGORIES + "_" + NAME );

        ArrayList<Solution> suggestions = new ArrayList<>();
        float rating;

        while (!cursor.isAfterLast()) {
            String elapsedTime = DateUtils.getRelativeTimeSpanString(context,
                    cursor.getLong(dateIndex) * 1000, true).toString();

            int solutionId = cursor.getInt(idIndex);
            Solution solution = new Solution(solutionId, cursor.getString(nameIndex));
            solution.setDate(cursor.getInt(dateIndex));
            solution.setWeight(cursor.getInt(weightIndex));
            solution.setViewed(cursor.getInt(viewedIndex) == 1);
            solution.setType(cursor.getInt(typeIndex));
            solution.setHasCME(cursor.getInt(cmeIndex) == 1);

            String categoryNameString = cursor.getString(categoryNameIndex);

            boolean mustSetSuggestions = ( categoryNameString != null || !categoryNameString.equals("") )
                    && ( type != APITalker.SUGGESTION_TYPES.REACTIVATION );
            if( mustSetSuggestions ){
                solution.setCategoryName(categoryNameString);
            }

            rating = cursor.getFloat(favoriteRatingIndex);
            if( rating == 0.0 ){
                rating = cursor.getFloat(solutionsRatingIndex);
            } else {
                solution.setRatedByUser(true);
            }

            solution.setRating(rating);

            if (type == 1) {
                solution.setTimestemp(elapsedTime);
            }

            suggestions.add(solution);

            cursor.moveToNext();
        }

        cursor.close();

        return suggestions;
    }

    public ArrayList<Solution> getFavorites() {
        SQLiteDatabase readableDatabase = getReadableDatabase();

        String query = "SELECT DISTINCT "
                + FAVORITES + "." + ID + " AS " + FAVORITES + "_" + ID + ","
                + FAVORITES + "." + RATING + " AS " + FAVORITES + "_" + RATING + ","
                + FAVORITES + "." + TIMESTEMP + " AS " + FAVORITES + "_" + TIMESTEMP + ","
                + SOLUTIONS + "." + ID + " AS " + SOLUTIONS + "_" + ID + ","
                + SOLUTIONS + "." + NAME + " AS " + SOLUTIONS + "_" + NAME + ","
                + SOLUTIONS + "." + WEIGHT + " AS " + SOLUTIONS + "_" + WEIGHT + ","
                + SOLUTIONS + "." + CATEGORY + " AS " + SOLUTIONS + "_" + CATEGORY + ","
                + SOLUTIONS + "." + SUB_CATEGORY + " AS " + SOLUTIONS + "_" + SUB_CATEGORY + ","
                + SOLUTIONS + "." + CME + " AS " + SOLUTIONS + "_" + CME + ","
                + SOLUTIONS + "." + RATING + " AS " + SOLUTIONS + "_" + RATING + ","
                + SOLUTIONS + "." + FULL_TITLE + " AS " + SOLUTIONS + "_" + FULL_TITLE + ","
                + CATEGORIES + "." + ID + " AS " + CATEGORIES + "_" + ID + ","
                + CATEGORIES + "." + NAME + " AS " + CATEGORIES + "_" + NAME + ","
                + CATEGORIES + "." + SKIP_SUBCATEGORY + " AS " + CATEGORIES + "_" + SKIP_SUBCATEGORY + ","
                + CATEGORIES + "." + WEIGHT + " AS " + CATEGORIES + "_" + WEIGHT
                + " FROM " + FAVORITES
                + " LEFT JOIN " + SOLUTIONS + " ON " + FAVORITES + "." + ID + " = " + SOLUTIONS + "." + ID
                + " LEFT JOIN " + CATEGORIES
                + " ON " + SOLUTIONS + "." + CATEGORY + " = " + CATEGORIES + "." + ID
                + " WHERE " + FAVORITES + "." + RATING + " >= " + SolutionFragment.MIN_SIGNIFICANT_RATING
                + " ORDER BY " + FAVORITES + "." + RATING + " DESC, " + FAVORITES + "." + TIMESTEMP + " DESC ";

        readableDatabase.beginTransactionNonExclusive();
        Cursor cursor = readableDatabase.rawQuery(query, null);
        readableDatabase.endTransaction();

        ArrayList<Solution> solutions = new ArrayList<>();

        cursor.moveToFirst();

        int idKey = cursor.getColumnIndex( FAVORITES + "_" + ID);
        int titleKey = cursor.getColumnIndex( SOLUTIONS+ "_" + NAME);
        int cmeKey = cursor.getColumnIndex( SOLUTIONS + "_" + CME);
        int ratingKey = cursor.getColumnIndex( FAVORITES +  "_" +RATING );
        int categoryKey = cursor.getColumnIndex( CATEGORIES + "_" + NAME );

        float rating;
        while (!cursor.isAfterLast()) {

            if (cursor.getString(titleKey) == null
                    || cursor.getString(titleKey).length() == 0) {
                cursor.moveToNext();
                continue;
            }

            Solution solution = new Solution(cursor.getInt(idKey), cursor.getString(titleKey));
            solution.setHasCME(cursor.getInt(cmeKey) == 1);

            rating = cursor.getFloat(ratingKey);
            if( rating == 0.0 ){
                ratingKey = cursor.getColumnIndex( SOLUTIONS +  "." + RATING );
                rating = cursor.getFloat(ratingKey);
            } else {
                solution.setRatedByUser(true);
            }

            solution.setRating(rating);

            String categoryNameString = cursor.getString(categoryKey);
            if( ( categoryNameString != null || !categoryNameString.equals("") ) ){
                solution.setCategoryName(categoryNameString);
            }


            solutions.add(solution);

            cursor.moveToNext();

        }

        cursor.close();

        return solutions;
    }

    public ArrayList<Solution> getPeerFavorites() {
        SQLiteDatabase readableDatabase = getReadableDatabase();

        String query = "SELECT DISTINCT "
                + PEER_FAVORITES + "." + ID + " AS " + PEER_FAVORITES + "_" + ID + ","
                + PEER_FAVORITES + "." + FULL_TITLE + " AS " + PEER_FAVORITES + "_" + FULL_TITLE + ","
                + PEER_FAVORITES + "." + FAVS + " AS " + PEER_FAVORITES + "_" + FAVS + ","
                + PEER_FAVORITES + "." + LIKES + " AS " + PEER_FAVORITES + "_" + LIKES + ","
                + SOLUTIONS + "." + ID + " AS " + SOLUTIONS + "_" + ID + ","
                + SOLUTIONS + "." + NAME + " AS " + SOLUTIONS + "_" + NAME + ","
                + SOLUTIONS + "." + WEIGHT + " AS " + SOLUTIONS + "_" + WEIGHT + ","
                + SOLUTIONS + "." + CATEGORY + " AS " + SOLUTIONS + "_" + CATEGORY + ","
                + SOLUTIONS + "." + SUB_CATEGORY + " AS " + SOLUTIONS + "_" + SUB_CATEGORY + ","
                + SOLUTIONS + "." + CME + " AS " + SOLUTIONS + "_" + CME + ","
                + SOLUTIONS + "." + RATING + " AS " + SOLUTIONS + "_" + RATING + ","
                + SOLUTIONS + "." + FULL_TITLE + " AS " + SOLUTIONS + "_" + FULL_TITLE + ","
                + FAVORITES + "." + ID + " AS " + FAVORITES + "_" + ID + ","
                + FAVORITES + "." + RATING + " AS " + FAVORITES + "_" + RATING + ","
                + FAVORITES + "." + TIMESTEMP + " AS " + FAVORITES + "_" + TIMESTEMP + ","
                + CATEGORIES + "." + ID + " AS " + CATEGORIES + "_" + ID + ","
                + CATEGORIES + "." + NAME + " AS " + CATEGORIES + "_" + NAME + ","
                + CATEGORIES + "." + SKIP_SUBCATEGORY + " AS " + CATEGORIES + "_" + SKIP_SUBCATEGORY + ","
                + CATEGORIES + "." + WEIGHT + " AS " + CATEGORIES + "_" + WEIGHT
                + " FROM " + PEER_FAVORITES
                + " LEFT JOIN " + SOLUTIONS + " ON " + PEER_FAVORITES + "." + ID + " = " + SOLUTIONS + "." + ID
                + " LEFT JOIN " + FAVORITES
                + " ON " + FAVORITES + "." + ID + " = " + SOLUTIONS + "." + ID
                + " LEFT JOIN " + CATEGORIES
                + " ON " + SOLUTIONS + "." + CATEGORY + " = " + CATEGORIES + "." + ID
//                + " WHERE " + FAVORITES + "_" + RATING + " >= " + SolutionFragment.MIN_SIGNIFICANT_RATING
                + " ORDER BY " + SOLUTIONS + "_" + NAME;

        readableDatabase.beginTransactionNonExclusive();
        Cursor cursor = readableDatabase.rawQuery(query, null);
        readableDatabase.endTransaction();

        ArrayList<Solution> solutions = new ArrayList<>();

        cursor.moveToFirst();

        int idKey = cursor.getColumnIndex( PEER_FAVORITES + "_" +ID);
        int titleKey = cursor.getColumnIndex( SOLUTIONS + "_" +NAME);
        int cmeKey = cursor.getColumnIndex( SOLUTIONS + "_" + CME );
        int favoriteRatingIndex = cursor.getColumnIndex(FAVORITES +  "_" + RATING);
        int solutionsRatingIndex = cursor.getColumnIndex( SOLUTIONS +  "_" + RATING );
        int categoryKey = cursor.getColumnIndex( CATEGORIES + "_" + NAME );

        float rating;
        while (!cursor.isAfterLast()) {

            if (cursor.getString(titleKey) == null
                    || cursor.getString(titleKey).length() == 0) {
                cursor.moveToNext();
                continue;
            }

            Solution solution = new Solution(cursor.getInt(idKey), cursor.getString(titleKey));
            solution.setHasCME(cursor.getInt(cmeKey) == 1);

            rating = cursor.getFloat(favoriteRatingIndex);
            if( rating == 0.0 ){
                rating = cursor.getFloat(solutionsRatingIndex);
            } else {
                solution.setRatedByUser(true);
            }

            solution.setRating(rating);

            String categoryNameString = cursor.getString(categoryKey);
            if( ( categoryNameString != null || !categoryNameString.equals("") ) ){
                solution.setCategoryName(categoryNameString);
            }

            solutions.add(solution);
            cursor.moveToNext();

        }

        cursor.close();

        return solutions;
    }

    public ArrayList<Solution> getHistory() {
        String username = user.username();

        String query = "SELECT DISTINCT "
                + SOLUTIONS + "." + ID + " AS " + SOLUTIONS + "_" + ID + " , "
                + SOLUTIONS + "." + NAME + " AS " + SOLUTIONS + "_" + NAME + " , "
                + SOLUTIONS + "." + CME + " AS " + SOLUTIONS + "_" + CME + " , "
                + SOLUTIONS + "." + RATING + " AS " + SOLUTIONS + "_" + RATING + " , "
                + FAVORITES + "." + RATING + " AS " + FAVORITES + "_" + RATING + " , "
                + HISTORY + "." + SAVE_DATE + " AS " + HISTORY + "_" + SAVE_DATE + " , "
                + HISTORY + "." + DOMAIN + " AS " + HISTORY + "_" + DOMAIN
                + " FROM " + HISTORY
                + " LEFT JOIN " + SOLUTIONS
                + " ON " + HISTORY + "." + ID + " = " + SOLUTIONS + "." + ID
                + " LEFT JOIN " + FAVORITES
                + " ON " + FAVORITES + "." + ID + " = " + SOLUTIONS + "." + ID
                + " WHERE " + HISTORY + "_" + DOMAIN + " = " + DatabaseUtils.sqlEscapeString(username)
                + " ORDER BY " + HISTORY + "_" + SAVE_DATE + " DESC";

        SQLiteDatabase readableDatabase = getReadableDatabase();

        readableDatabase.beginTransactionNonExclusive();
        Cursor cursor = readableDatabase.rawQuery(query, null);
        readableDatabase.endTransaction();

        ArrayList<Solution> solutions = new ArrayList<>();

        cursor.moveToFirst();

        int idKey = cursor.getColumnIndex(SOLUTIONS + "_" + ID);
        int titleKey = cursor.getColumnIndex(SOLUTIONS + "_" + NAME);
        int timeKey = cursor.getColumnIndex(HISTORY + "_" + SAVE_DATE);
        int cmeKey = cursor.getColumnIndex(SOLUTIONS + "_" + CME);
        int favoritesRatingKey = cursor.getColumnIndex(FAVORITES +  "_" + RATING);
        int solutionRatingKey = cursor.getColumnIndex(SOLUTIONS +  "_" + RATING);

        float rating;
        while (!cursor.isAfterLast()) {
            long time = cursor.getLong(timeKey);
            String elapsedTime = DateUtils.getRelativeDateTimeString(context,
                    time, DateUtils.SECOND_IN_MILLIS, DateUtils.YEAR_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE).toString();
            elapsedTime = elapsedTime.split(",")[0];

            Solution solution = new Solution(cursor.getInt(idKey), cursor.getString(titleKey));
            solution.setHasCME(cursor.getInt(cmeKey) == 1);
            solution.setTimestemp(elapsedTime);

            rating = cursor.getFloat(favoritesRatingKey);

            if (rating == 0.0) {
                rating = cursor.getFloat(solutionRatingKey);
            } else {
                solution.setRatedByUser(true);
            }

            solution.setRating(rating);


            solutions.add(solution);

            cursor.moveToNext();
        }

        cursor.close();

        return solutions;
    }

    public String[] getQuestions() {
        String query = "SELECT DISTINCT * FROM " + QUESTIONS + ";";


        SQLiteDatabase readableDatabase = getReadableDatabase();
        Cursor cursor = readableDatabase.rawQuery(query, null);
        String[] questions = new String[cursor.getCount()];
        int nameIndex = cursor.getColumnIndex(NAME);

        cursor.moveToFirst();
        int index = 0;

        while (!cursor.isAfterLast()) {
            questions[index] = cursor.getString(nameIndex);
            cursor.moveToNext();
            index++;
        }

        cursor.close();

        return questions;
    }

    public ArrayList<Notification> getNotifications() {
        return getNotifications(0, -1, false);
    }

    private ArrayList<Notification> getNotifications(int limit, long minDate, boolean mustNotBeRead) {
        String query = "SELECT DISTINCT * FROM " + NOTIFICATIONS;

        if (minDate >= 0) {
            query = query + " WHERE " + DATE + " > " + minDate;
        }
        if (mustNotBeRead) {
            if (minDate >= 0) {
                query = query + " AND " + VIEWED + " = " + 0;
            } else {
                query = query + " WHERE " + VIEWED + " = " + 0;
            }
        }

        query = query + " ORDER BY " + DATE + " DESC ";

        if (limit > 0) {
            query = query + " LIMIT " + limit;
        }

        SQLiteDatabase readableDatabase = getReadableDatabase();
        Cursor cursor = readableDatabase.rawQuery(query, null);
        cursor.moveToFirst();

        int idIndex = cursor.getColumnIndex(ID);
        int textIndex = cursor.getColumnIndex(TEXT);
        int correspondSolutionIdIndex = cursor.getColumnIndex(CORRESPOND_SOLUTION_ID);
        int correspondCategoryIdIndex = cursor.getColumnIndex(CORRESPOND_CATEGORY_ID);
        int correspondSubCategoryIdIndex = cursor.getColumnIndex(CORRESPOND_SUB_CATEGORY_ID);
        int pageToOpenIndex = cursor.getColumnIndex(PAGE_TO_OPEN);
        int correspondSuggestionIdIndex = cursor.getColumnIndex(CORRESPOND_SUGGESTION);
        int dateIndex = cursor.getColumnIndex(DATE);
        int viewedIndex = cursor.getColumnIndex(VIEWED);

        ArrayList<Notification> notifications = new ArrayList<>();

        int notificationId;
        String notificationText;
        String correspondSolutionId;
        int correspondCategoryId;
        int correspondSubCategoryId;
        int pageToOpen;
        int correspondSuggestion;
        long notificationReceiveDate;
        boolean read;

        while (!cursor.isAfterLast()) {
            notificationId = cursor.getInt(idIndex);
            notificationText = cursor.getString(textIndex);
            correspondSolutionId = cursor.getString(correspondSolutionIdIndex);
            correspondCategoryId = cursor.getInt(correspondCategoryIdIndex);
            correspondSubCategoryId = cursor.getInt(correspondSubCategoryIdIndex);
            pageToOpen = cursor.getInt(pageToOpenIndex);
            correspondSuggestion = cursor.getInt(correspondSuggestionIdIndex);
            notificationReceiveDate = cursor.getLong(dateIndex);
            read = cursor.getInt(viewedIndex) == 1;

            Notification notification = new Notification(notificationId,
                    notificationText, correspondSolutionId,
                    correspondCategoryId, correspondSubCategoryId, pageToOpen, correspondSuggestion == 1,
                    notificationReceiveDate, read);

            notifications.add(notification);

            cursor.moveToNext();
        }

        cursor.close();

        return notifications;
    }

    public int getUnreadNotificationsNumber() {
        return getNotifications(0, -1, true).size();
    }

    public ArrayList<Integer> getBannerNotifications() {
        ArrayList<Integer> notificationIds = new ArrayList<>();

        String query = "SELECT DISTINCT * FROM " + BANNER_NOTIFICATIONS
                + " ORDER BY " + DATE + " DESC ";


        SQLiteDatabase readableDatabase = getReadableDatabase();
        Cursor cursor = readableDatabase.rawQuery(query, null);
        cursor.moveToFirst();

        int idIndex = cursor.getColumnIndex(ID);

        int notificationId;

        while (!cursor.isAfterLast()) {
            notificationId = cursor.getInt(idIndex);
            notificationIds.add(notificationId);

            cursor.moveToNext();
        }

        cursor.close();

        return notificationIds;
    }

    public Solution getCategory(int categoryId) {
        String query = "SELECT DISTINCT * FROM " + CATEGORIES
                + " WHERE " + ID + " = " + categoryId + ";";

        SQLiteDatabase readableDatabase = getReadableDatabase();
        Cursor cursor = readableDatabase.rawQuery(query, null);
        cursor.moveToFirst();

        int titleIndex = cursor.getColumnIndex(NAME);

        Solution solution = null;
        if (cursor.moveToFirst() && cursor.getCount() > 0) {
            solution = new Solution(categoryId, cursor.getString(titleIndex));
        }

        cursor.close();

        return solution;
    }

    public Solution getSubCategory(int subCategoryId) {

        String query = "SELECT DISTINCT * FROM " + CATEGORIES + " WHERE " + ID
                + " = " + subCategoryId + ";";

        SQLiteDatabase readableDatabase = getReadableDatabase();
        Cursor cursor = readableDatabase.rawQuery(query, null);
        cursor.moveToFirst();

        int titleIndex = cursor.getColumnIndex(NAME);

        Solution solution = null;
        if (cursor.moveToFirst() && cursor.getCount() > 0) {
            solution = new Solution(subCategoryId, cursor.getString(titleIndex));
        }

        cursor.close();

        return solution;

    }

    public Notification getNotification(int notificationId) {
        String query = "SELECT * FROM " + NOTIFICATIONS + " WHERE " + ID + " = " + notificationId + ";";

        SQLiteDatabase readableDatabase = getReadableDatabase();
        Cursor cursor = readableDatabase.rawQuery(query, null);
        cursor.moveToFirst();

        if (cursor.isAfterLast()) {
            return null;
        }

        int textIndex = cursor.getColumnIndex(TEXT);
        int correspondSolutionIdIndex = cursor.getColumnIndex(CORRESPOND_SOLUTION_ID);
        int correspondCategoryIdIndex = cursor.getColumnIndex(CORRESPOND_CATEGORY_ID);
        int correspondSubCategoryIdIndex = cursor.getColumnIndex(CORRESPOND_SUB_CATEGORY_ID);
        int correspondSuggestionIdIndex = cursor.getColumnIndex(CORRESPOND_SUGGESTION);
        int pageToOpenIndex = cursor.getColumnIndex(PAGE_TO_OPEN);
        int dateIndex = cursor.getColumnIndex(DATE);
        int viewedIndex = cursor.getColumnIndex(VIEWED);

        String notificationText = cursor.getString(textIndex);
        String correspondSolutionId = cursor.getString(correspondSolutionIdIndex);
        int correspondCategoryId = cursor.getInt(correspondCategoryIdIndex);
        int correspondSubCategoryId = cursor.getInt(correspondSubCategoryIdIndex);
        int pageToOpen = cursor.getInt(pageToOpenIndex);
        int correspondSuggestion = cursor.getInt(correspondSuggestionIdIndex);
        long notificationReceiveDate = cursor.getLong(dateIndex);
        boolean read = cursor.getInt(viewedIndex) == 1;

        cursor.close();

        return new Notification(notificationId,
                notificationText, correspondSolutionId,
                correspondCategoryId, correspondSubCategoryId, pageToOpen, correspondSuggestion == 1,
                notificationReceiveDate, read);
    }

    public ArrayList<Solution> searchSolutions(String token) {
        if (token.length() == 0) {
            return new ArrayList<>();
        }

        token = DatabaseUtils.sqlEscapeString("%" + token + "%");

        String query = "SELECT DISTINCT "
                + SOLUTIONS + "." + ID + " AS " + SOLUTIONS + "_" + ID + ","
                + SOLUTIONS + "." + NAME + " AS " + SOLUTIONS + "_" + NAME + ","
                + SOLUTIONS + "." + WEIGHT + " AS " + SOLUTIONS + "_" + WEIGHT + ","
                + SOLUTIONS + "." + CATEGORY + " AS " + SOLUTIONS + "_" + CATEGORY + ","
                + SOLUTIONS + "." + SUB_CATEGORY + " AS " + SOLUTIONS + "_" + SUB_CATEGORY + ","
                + SOLUTIONS + "." + CME + " AS " + SOLUTIONS + "_" + CME + ","
                + SOLUTIONS + "." + RATING + " AS " + SOLUTIONS + "_" + RATING + ","
                + SOLUTIONS + "." + FULL_TITLE + " AS " + SOLUTIONS + "_" + FULL_TITLE + ","
                + FAVORITES + "." + ID + " AS " + FAVORITES + "_" + ID + ","
                + FAVORITES + "." + RATING + " AS " + FAVORITES + "_" + RATING + ","
                + FAVORITES + "." + TIMESTEMP + " AS " + FAVORITES + "_" + TIMESTEMP + ","
                + CATEGORIES + "." + ID + " AS " + CATEGORIES + "_" + ID + ","
                + CATEGORIES + "." + NAME + " AS " + CATEGORIES + "_" + NAME + ","
                + CATEGORIES + "." + SKIP_SUBCATEGORY + " AS " + CATEGORIES + "_" + SKIP_SUBCATEGORY + ","
                + CATEGORIES + "." + WEIGHT + " AS " + CATEGORIES + "_" + WEIGHT
                + " FROM " + SOLUTIONS
                + " LEFT JOIN " + FAVORITES
                + " ON " + FAVORITES + "." + ID + " = " + SOLUTIONS + "." + ID
                + " LEFT JOIN " + CATEGORIES
                + " ON " + SOLUTIONS + "." + CATEGORY + " = " + CATEGORIES + "." + ID
                + " WHERE "
                + FULL_TITLE + " LIKE " + token + " ORDER BY " + FULL_TITLE
                + " ASC ;";

        SQLiteDatabase readableDatabase = getReadableDatabase();
        readableDatabase.beginTransactionNonExclusive();
        Cursor cursor = readableDatabase.rawQuery(query, null);
        readableDatabase.endTransaction();

        ArrayList<Solution> solutions = new ArrayList<>();

        cursor.moveToFirst();

        int idKey = cursor.getColumnIndex(SOLUTIONS + "_" + ID);
        int titleKey = cursor.getColumnIndex(SOLUTIONS + "_" + FULL_TITLE);
        int cmeKey = cursor.getColumnIndex(SOLUTIONS + "_" + CME);
        int favoritesRatingKey = cursor.getColumnIndex(FAVORITES +  "_" + RATING);
        int solutionsRatingKey = cursor.getColumnIndex(SOLUTIONS + "_" + RATING);
        int categoryNameIndex = cursor.getColumnIndex( CATEGORIES + "_" + NAME );

        float rating;
        String categoryName;
        while (!cursor.isAfterLast()) {
            Solution solution = new Solution(cursor.getInt(idKey), cursor.getString(titleKey));
            solution.setHasCME(cursor.getInt(cmeKey) == 1);

            rating = cursor.getFloat(favoritesRatingKey);

            if (rating == 0.0) {
                rating = cursor.getFloat(solutionsRatingKey);
            } else {
                solution.setRatedByUser(true);
            }

            solution.setRating(rating);

            // get correspond category name from categories table
            // and set it to the solution
            categoryName = cursor.getString( categoryNameIndex );
            solution.setCategoryName( categoryName );

            solutions.add(solution);
            cursor.moveToNext();
        }

        cursor.close();

        return solutions;
    }

    public void saveHistory(int solutionId, String name, boolean isVoice) {
        String username = user.username();

        String query = "REPLACE INTO " + HISTORY + "( " + ID + "," + NAME + ","
                + SAVE_DATE + "," + DOMAIN + "," + IS_VOICE + " ) VALUES ( "
                + solutionId + "," + DatabaseUtils.sqlEscapeString(name) + ","
                + System.currentTimeMillis() + ","
                + DatabaseUtils.sqlEscapeString(username) + ","
                + (isVoice ? 1 : 0) + " )";

        SQLiteDatabase writableDatabase = getWritableDatabase();
        writableDatabase.execSQL(query);
    }

    public boolean isFavorite(int solutionId) {
        String query = "SELECT * FROM " + FAVORITES + " WHERE " + ID + " = "
                + solutionId;

        SQLiteDatabase readableDatabase = getReadableDatabase();

        Cursor cursor = readableDatabase.rawQuery(query, null);
        boolean isFavorite = cursor.getCount() > 0;
        cursor.close();

        return isFavorite;
    }

    public void addFavorite(int solutionId, double rating, long timestemp) {
        String query = "REPLACE INTO " + FAVORITES + "( " + ID + "," + RATING + "," + TIMESTEMP + ") VALUES ( "
                + solutionId + "," + rating + "," + timestemp + " )";

        SQLiteDatabase writableDatabase = getWritableDatabase();
        writableDatabase.execSQL(query);
    }

    public void removeFavorite(int solutionId) {
        String query = "DELETE FROM " + FAVORITES + " WHERE " + ID + " = "
                + solutionId;

        SQLiteDatabase writableDatabase = getWritableDatabase();
        writableDatabase.execSQL(query);
    }

    public void markNotificationsReadInDb(ArrayList<String> notificationsIdArray) {

        for (int index = 0; index < notificationsIdArray.size(); index++) {
            String query = "UPDATE " + NOTIFICATIONS + " SET " + VIEWED + " = "
                    + 1 + " WHERE " + ID + " = "
                    + notificationsIdArray.get(index) + ";";

            SQLiteDatabase readableDatabase = getReadableDatabase();
            readableDatabase.execSQL(query);
        }
    }

    public void markReactivationViewed(int solutionId) {

        String query = "UPDATE " + SUGGESTIONS
                + " SET " + VIEWED + " = " + 1
                + " WHERE " + ID + " = " + solutionId + ";";

        SQLiteDatabase readableDatabase = getReadableDatabase();
        readableDatabase.execSQL(query);
    }

    public void removeBannerNotification(int notificationId) {
        String query = "DELETE FROM " + BANNER_NOTIFICATIONS + " WHERE " + ID + " = "
                + notificationId;
        SQLiteDatabase writableDatabase = getWritableDatabase();
        writableDatabase.execSQL(query);
    }

    public void flushBannerNotificationsTable() {
        getWritableDatabase().delete(BANNER_NOTIFICATIONS, null, null);
    }

    /**
     * Delete Methods
     */

    public void flushDatabase() {
        SQLiteDatabase writableDatabase = getWritableDatabase();

        writableDatabase.delete(CATEGORIES, null, null);
        writableDatabase.delete(SUB_CATEGORIES, null, null);
        writableDatabase.delete(SOLUTIONS, null, null);
        writableDatabase.delete(FAVORITES, null, null);
        writableDatabase.delete(NOTIFICATIONS, null, null);
        writableDatabase.delete(QUESTIONS, null, null);
        writableDatabase.delete(SUGGESTIONS, null, null);
        writableDatabase.delete(BANNER_NOTIFICATIONS, null, null);
    }

    /**
     * Database Getters and Setters
     */

    private final Object WRITABLE_LOCK = new Object();
    private final Object READABLE_LOCK = new Object();

    @Override
    public SQLiteDatabase getWritableDatabase() {
        synchronized (WRITABLE_LOCK) {
            return super.getWritableDatabase();
        }
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        synchronized (READABLE_LOCK) {
            return super.getReadableDatabase();
        }
    }
}
