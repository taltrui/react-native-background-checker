package com.reactlibrary;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import android.content.BroadcastReceiver;
import android.content.Context;

public class BackgroundCheckerModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private final IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    private Intent batteryStatus;
    private final static String BATTERY_STATE = "batteryState";

    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            emitStatus(intent);
        }
    };

    public BackgroundCheckerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;

        this.batteryStatus = reactContext.registerReceiver(batteryReceiver, ifilter);
    }

    @Override
    public String getName() {
        return "BackgroundChecker";
    }

    private String getStatus(Intent intent) {
        try {

            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status == BatteryManager.BATTERY_STATUS_FULL;

            return isCharging ? "CHARGING" : "NOT_CHARGING";
        } catch (Exception e) {
            return e.toString();
        }
    }

    private String getPlugMode(Intent intent) {
        try {

            int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            String mode = "NONE";

            if (chargePlug == BatteryManager.BATTERY_PLUGGED_USB)
                mode = "USB";
            if (chargePlug == BatteryManager.BATTERY_PLUGGED_AC)
                mode = "AC";

            return mode;

        } catch (Exception e) {
            return e.toString();
        }
    }

    private float getCurrentLevel(Intent intent) {
        try {

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            return level * 100 / (float) scale;
        } catch (Exception e) {
            return -1;
        }
    }

    private void emitStatus(Intent intent) {
        String mode = getPlugMode(intent);
        float level = getCurrentLevel(intent);
        String status = getStatus(intent);
        WritableMap params = Arguments.createMap();

        params.putString("mode", mode);
        params.putDouble("level", (double) level);
        params.putString("status", status);

        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(BATTERY_STATE, params);
    }

    @ReactMethod
    public void status(Promise promise) {
        try {
            String status = getStatus(batteryStatus);

            promise.resolve(status);
        } catch (Exception e) {
            promise.reject(e);
        }

    }

    @ReactMethod
    public void plugMode(Promise promise) {
        try {
            String mode = getPlugMode(batteryStatus);

            promise.resolve(mode);
        } catch (Exception e) {
            promise.reject(e);
        }

    }

    @ReactMethod
    public void level(Promise promise) {
        try {
            float level = getCurrentLevel(batteryStatus);

            promise.resolve(level);
        } catch (Exception e) {
            promise.reject(e);
        }

    }
}
