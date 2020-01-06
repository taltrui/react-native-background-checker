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

public class BackgroundCheckerModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private final IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    private Intent batteryStatus;
    private final static String BATTERY_STATE = "batteryState";

    public BackgroundCheckerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;

        this.batteryStatus = reactContext.registerReceiver(null, ifilter);
    }

    @Override
    public String getName() {
        return "BatteryChecker";
    }

    private String getStatus() {
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL;

        return isCharging ? "CHARGING" : "NOT_CHARGING";
    }

    private String getPlugMode() {
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        String mode = "UNKNOWN";

        if (chargePlug == BatteryManager.BATTERY_PLUGGED_USB) mode = "USB";
        if (chargePlug == BatteryManager.BATTERY_PLUGGED_AC) mode = "AC";

        return mode;
    }

    private Float getCurrentLevel() {
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        return level * 100 / (float) scale;
    }

    @ReactMethod
    public void status(Promise promise) {
        try {
            String status = getStatus();

            promise.resolve(status);
        } catch (Exception e) {
            promise.reject(e);
        }

    }

    @ReactMethod
    public void plugMode(Promise promise) {
        try {
            String mode = getPlugMode();

            promise.resolve(mode);
        } catch (Exception e) {
            promise.reject(e);
        }

    }

    @ReactMethod
    public void level(Promise promise) {
        try {
            float level = getCurrentLevel();

            promise.resolve(level);
        } catch (Exception e) {
            promise.reject(e);
        }

    }

    private void emitBatteryState(Intent state) {
        String mode = getPlugMode();
        float level = getCurrentLevel();
        String status = getStatus();
        WritableMap params = Arguments.createMap();

        params.putString("mode", mode);
        params.putDouble("level", (double) level);
        params.putString("status", status);

        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(BATTERY_STATE, params);
    }
}
