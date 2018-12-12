package com.implementhit.OptimizeHIT.api;

/**
 * Created by acerkinght on 8/15/16.
 */
public interface OnGetDashboardBadgeCountListener {
    void onDashboardBadgeCountSuccess(int dashboardBadgeCount);
    void onDashboardBadgeCountFailure(String error);
}
