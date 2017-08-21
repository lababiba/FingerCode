package com.example.sf.fingercode;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;

/**
 * Created by azret.magometov on 19-Dec-16.
 */

public final class FingerprintUtils {
    private FingerprintUtils() {
    }

    public enum mSensorState {
        NOT_SUPPORTED,
        NOT_BLOCKED,
        NO_FINGERPRINTS,
        READY
    }

    public static boolean checkFingerprintCompatibility(Context context) {
        try {
        return context.getSystemService(FingerprintManager.class).isHardwareDetected();}
        catch (SecurityException e){

            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static mSensorState checkSensorState(Context context) {
        if (checkFingerprintCompatibility(context)) {

            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            if (!keyguardManager.isKeyguardSecure()) {
                return mSensorState.NOT_BLOCKED;
            }

            if (context.checkSelfPermission(Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                if (context.getSystemService(FingerprintManager.class).hasEnrolledFingerprints()) {
                    return mSensorState.NO_FINGERPRINTS;
                }
            }

            return mSensorState.READY;

        } else {
            return mSensorState.NOT_SUPPORTED;
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean isSensorStateAt(mSensorState state, Context context) {
        return checkSensorState(context) == state;
    }
}
