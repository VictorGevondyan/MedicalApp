package com.implementhit.OptimizeHIT.badge;

import android.content.Context;

import me.leolin.shortcutbadger.ShortcutBadger;

public class BadgeCounter {
    synchronized public static void updateNotificationBadge(Context context, int badgeCount) {
        // TODO: Migrate to another lib, as this seems not trustworthy
//        ShortcutBadger.applyCount(context.getApplicationContext(), badgeCount);
        ShortcutBadger.with(context).count(badgeCount);
    }
}