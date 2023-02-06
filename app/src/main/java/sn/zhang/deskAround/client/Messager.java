package sn.zhang.deskAround.client;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class Messager {
    public final static String ACTION_CAST_DONE = "sn.zhang.deskAround.ACTION_CAST_DONE";
    public final static String ACTION_CAST_FAILED = "sn.zhang.deskAround.ACTION_CAST_FAILED";
    public final static String ACTION_TARGET_CONNECTED = "sn.zhang.deskAround.ACTION_TARGET_CONNECTED";
    public final static String EXTRA_DATA = "sn.zhang.deskAround.EXTRA_DATA";

    public static void announce(Context context, String action) {
        Intent intent = new Intent(action);
        context.sendBroadcast(intent);
    }

    public static void announce(Context context, String action, String message) {
        Intent intent = new Intent(action);
        intent.putExtra(EXTRA_DATA, message);
        context.sendBroadcast(intent);
    }

    public static IntentFilter getIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CAST_DONE);
        intentFilter.addAction(ACTION_CAST_FAILED);
        intentFilter.addAction(ACTION_TARGET_CONNECTED);
        return intentFilter;
    }
}
