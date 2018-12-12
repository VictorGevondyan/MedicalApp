package com.implementhit.OptimizeHIT.util;

import android.content.Context;
import android.util.Log;

import com.implementhit.OptimizeHIT.database.DBTalker;
import com.implementhit.OptimizeHIT.models.Notification;
import com.implementhit.OptimizeHIT.models.Solution;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by acerkinght on 8/6/16.
 */
public class ExternalSignalParser {

    public enum SignalSource {
        Notification,
        DeepLink
    }

    private static final String LOGOUT_URL = "ohitapp://logout/";
    private static final String SOLUTION_URL = "ohitapp://solution/open?solution_id";
    private static final String CATEGORY_URL = "ohitapp://category/open?cat_id";
    private static final String SUB_CATEGORY_V1_PREFIX_URL = "ohitapp://subcategory/open?subcat_id";
    private static final String SUB_CATEGORY_V2_PREFIX_URL = "ohitapp://subcategory/open?cat_id";
    private static final String SUGGESTIONS_URL = "ohitapp://suggested/";
    private static final String OHITAPP = "ohitapp://";

    public static final String DASHBOARD = "ohitapp://dashboard/";
    public static final String OPTI_QUERY = "ohitapp://optiquery/";
    public static final String LIBRARY = "ohitapp://library/";
    public static final String FIND_THE_CODE = "ohitapp://findthecode/";

    private static final int PAGE_DASHBOARD = 1;
    private static final int PAGE_OPTI_QUERY = 2;
    private static final int PAGE_LIBRARY = 3;
    private static final int PAGE_SUGGESTED = 4;
    private static final int PAGE_FIND_THE_CODE = 5;

    public static void processNotification(Context context, Notification notification, ExternalSignalFeedbackReceiver feedbackReceiver) {

        String solutionIdString = notification.getCorrespondSolutionId();

        int solutionId;
        if( ( solutionIdString != null ) &&  !solutionIdString.equals("")  ) {
            solutionId = Integer.parseInt(solutionIdString);
        } else {
            solutionId = -1;
        }

        Solution category = DBTalker.sharedDB(context).getCategory(notification.getCorrespondCategoryId());
        Solution subcategory = DBTalker.sharedDB(context).getCategory(notification.getCorrespondSubCategoryId());
        int pageToOpen = notification.getPageToOpen();


        if (notification.getOpensSuggestion()) {
            feedbackReceiver.onSuggestionsSignal(SignalSource.Notification);
        } else if (solutionId >= 0) {
            feedbackReceiver.onSolutionSignal(solutionId, SignalSource.Notification);
        } else if (category != null) {
            if (subcategory != null) {
                feedbackReceiver.onSubcategorySignal(category, subcategory, SignalSource.Notification);
            } else {
                feedbackReceiver.onCategorySignal(category, SignalSource.Notification);
            }
        } else if( pageToOpen != -1 ){

            if( pageToOpen == PAGE_DASHBOARD ){
                feedbackReceiver.onDashboardSignal(SignalSource.Notification);
            } else if( pageToOpen == PAGE_OPTI_QUERY ){
                feedbackReceiver.onOptiQuerySignal(SignalSource.Notification);
            } else if( pageToOpen == PAGE_LIBRARY ){
                feedbackReceiver.onLibrarySignal(SignalSource.Notification);
            } else if( pageToOpen == PAGE_SUGGESTED ){
                feedbackReceiver.onSuggestionsSignal(SignalSource.Notification);
            } else if( pageToOpen == PAGE_FIND_THE_CODE ){
                feedbackReceiver.onFindTheCodeSignal(SignalSource.Notification);
            }

        } else {
            feedbackReceiver.onNoSignal(SignalSource.Notification);
        }

    }

    public static boolean processDeepLink(Context context, String deepLink, ExternalSignalFeedbackReceiver feedbackReceiver) {
        if (!deepLink.startsWith(OHITAPP)) {
            return false;
        }

        if (deepLink.startsWith(SOLUTION_URL)) {
            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher matcher = pattern.matcher(deepLink);

            String solutionId = "";

            if (matcher.find()) {
                solutionId = matcher.group();
            }

            try {
                feedbackReceiver.onSolutionSignal( Integer.parseInt(solutionId), SignalSource.DeepLink );
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else if (deepLink.startsWith(CATEGORY_URL)) {
            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher matcher = pattern.matcher(deepLink);

            String categoryId = "";

            if (matcher.find()) {
                categoryId = matcher.group();
            }

            try {
                Solution category = DBTalker.sharedDB(context).getCategory(Integer.parseInt(categoryId));

                if (category != null) {
                    feedbackReceiver.onCategorySignal(category, SignalSource.DeepLink);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else if (deepLink.startsWith(SUB_CATEGORY_V1_PREFIX_URL)) {
            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher matcher = pattern.matcher(deepLink);

            String categoryId = "";
            String subcategoryId = "";

            if (matcher.find()) {
                subcategoryId = matcher.group();
            }

            if (matcher.find()) {
                categoryId = matcher.group();
            }

            try {
                Solution category = DBTalker.sharedDB(context).getCategory(Integer.parseInt(categoryId));
                Solution subcategory = DBTalker.sharedDB(context).getCategory(Integer.parseInt(subcategoryId));

                if (category != null && subcategory != null) {
                    feedbackReceiver.onSubcategorySignal(category, subcategory, SignalSource.DeepLink);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else if (deepLink.startsWith(SUB_CATEGORY_V2_PREFIX_URL)) {
            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher matcher = pattern.matcher(deepLink);

            String categoryId = "";
            String subcategoryId = "";

            if (matcher.find()) {
                categoryId = matcher.group();
            }

            if (matcher.find()) {
                subcategoryId = matcher.group();
            }

            try {
                Solution category = DBTalker.sharedDB(context).getCategory(Integer.parseInt(categoryId));
                Solution subcategory = DBTalker.sharedDB(context).getCategory(Integer.parseInt(subcategoryId));

                if (category != null && subcategory != null) {
                    feedbackReceiver.onSubcategorySignal(category, subcategory, SignalSource.DeepLink);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else if (deepLink.startsWith(SUGGESTIONS_URL)) {
            feedbackReceiver.onSuggestionsSignal(SignalSource.DeepLink);
        } else if (deepLink.startsWith(LOGOUT_URL)) {
            feedbackReceiver.onLogout(SignalSource.DeepLink);
        } else if (deepLink.startsWith(DASHBOARD)) {
            feedbackReceiver.onDashboardSignal(SignalSource.DeepLink);
        } else if (deepLink.startsWith(OPTI_QUERY)) {
            feedbackReceiver.onOptiQuerySignal(SignalSource.DeepLink);
        } else if (deepLink.startsWith(LIBRARY)) {
            feedbackReceiver.onLibrarySignal(SignalSource.DeepLink);
        } else if (deepLink.startsWith(FIND_THE_CODE)) {
            feedbackReceiver.onFindTheCodeSignal(SignalSource.DeepLink);
        }

        return true;
    }

    public interface ExternalSignalFeedbackReceiver {
        void onSuggestionsSignal(SignalSource signalSource);
        void onSolutionSignal(int solutionId, SignalSource signalSource);
        void onCategorySignal(Solution category, SignalSource signalSource);
        void onSubcategorySignal(Solution category, Solution subcategory, SignalSource signalSource);
        void onNoSignal(SignalSource signalSource);
        void onLogout(SignalSource signalSource);
        void onDashboardSignal( SignalSource signalSource );
        void onOptiQuerySignal( SignalSource signalSource );
        void onLibrarySignal( SignalSource signalSource );
        void onFindTheCodeSignal( SignalSource signalSource );
    }

}
