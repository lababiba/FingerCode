package com.example.sf.fingercode;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationCallback;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import javax.crypto.Cipher;

public class LoginActivity extends Activity {

    private static final String PIN = "pin";
    private EditText mEditText;
    private SharedPreferences mPreferences;
    private FingerprintHelper mFingerprintHelper;
    String TAG = "mTag";

    AuthenticationCallback ath = new AuthenticationCallback() {
        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            Log.d(TAG, "onAuthenticationError: ");
            super.onAuthenticationError(errorCode, errString);
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            Log.d(TAG, "onAuthenticationHelp: ");
            super.onAuthenticationHelp(helpCode, helpString);
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            Log.d(TAG, "onAuthenticationSucceeded: ");
            super.onAuthenticationSucceeded(result);
        }

        @Override
        public void onAuthenticationFailed() {
            Log.d(TAG, "onAuthenticationFailed: ");
            super.onAuthenticationFailed();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditText = (EditText) findViewById(R.id.editText);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareLogin();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPreferences.contains(PIN)) {
            prepareSensor();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFingerprintHelper != null) {
            mFingerprintHelper.cancel();
        }
    }

    private void prepareLogin() {

        final String pin = mEditText.getText().toString();
        if (pin.length() > 0) {
            savePin(pin);
            startActivity(new Intent(this, MainActivity.class));
        } else {
            Toast.makeText(this, "pin is empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePin(String pin) {
        if (FingerprintUtils.isSensorStateAt(FingerprintUtils.mSensorState.READY, this)) {
            String encoded = CryptoUtils.encode(pin);
            mPreferences.edit().putString(PIN, encoded).apply();
        }
    }

    private void prepareSensor() {
        if (FingerprintUtils.isSensorStateAt(FingerprintUtils.mSensorState.READY, this)) {
            FingerprintManager.CryptoObject cryptoObject = CryptoUtils.getCryptoObject();
            if (cryptoObject != null) {
                Toast.makeText(this, "use fingerprint to login", Toast.LENGTH_LONG).show();
                mFingerprintHelper = new FingerprintHelper(this);
                mFingerprintHelper.startAuth(cryptoObject);
            } else {
                mPreferences.edit().remove(PIN).apply();
                Toast.makeText(this, "new fingerprint enrolled. enter pin again", Toast.LENGTH_SHORT).show();
            }

        }
    }


    public class FingerprintHelper extends AuthenticationCallback {
        private Context mContext;
        private CancellationSignal mCancellationSignal;

        FingerprintHelper(Context context) {
            mContext = context;
        }

        void startAuth(FingerprintManager.CryptoObject cryptoObject) {
            mCancellationSignal = new CancellationSignal();
            FingerprintManager manager = mContext.getSystemService(FingerprintManager.class);
//            manager.authenticate(cryptoObject, 0, mCancellationSignal, this, null)
                if (checkSelfPermission(Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED) {
                    manager.authenticate(cryptoObject ,mCancellationSignal, 0, this, null);
                    return;
                }

            }


        void cancel() {
            if (mCancellationSignal != null) {
                mCancellationSignal.cancel();
            }
        }

        @Override
        public void onAuthenticationError(int errMsgId, CharSequence errString) {
            Toast.makeText(mContext, errString, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
            Toast.makeText(mContext, helpString, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            Cipher cipher = result.getCryptoObject().getCipher();
            String encoded = mPreferences.getString(PIN, null);
            String decoded = CryptoUtils.decode(encoded, cipher);
            mEditText.setText(decoded);
            Toast.makeText(mContext, "success", Toast.LENGTH_SHORT).show();
        }



        @Override
        public void onAuthenticationFailed() {
            Toast.makeText(mContext, "try again", Toast.LENGTH_SHORT).show();
        }

    }


}