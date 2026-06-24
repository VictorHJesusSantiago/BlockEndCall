package com.blockendcall.android.service;

import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.util.Log;

import com.blockendcall.android.api.ApiClient;
import com.blockendcall.android.api.BlockedNumberApi;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.NumberCheckResult;
import com.blockendcall.android.util.NotificationHelper;
import com.blockendcall.android.util.SessionManager;

import retrofit2.Response;

/**
 * Intercepts incoming calls and silently rejects numbers confirmed as spam by the community.
 * Must be set as the default Call Screening app via system settings (RoleManager.ROLE_CALL_SCREENING).
 */
public class BlockCallScreeningService extends CallScreeningService {

    private static final String TAG = "BlockCallScreening";

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.createChannels(this);
    }

    @Override
    public void onScreenCall(Call.Details callDetails) {
        String incomingNumber = callDetails.getHandle().getSchemeSpecificPart();

        new Thread(() -> {
            try {
                SessionManager session = new SessionManager(getApplicationContext());
                BlockedNumberApi api = ApiClient.getApi(session);

                Response<ApiResponse<NumberCheckResult>> response =
                        api.checkNumber(incomingNumber).execute();

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().isSuccess()) {

                    NumberCheckResult result = response.body().getData();

                    if (result != null && result.isBlocked() && result.isConfirmed()) {
                        Log.i(TAG, "Blocking confirmed spam: " + incomingNumber
                                + " [" + result.getCategory() + ", score=" + result.getSpamScore() + "]");

                        respondToCall(callDetails, buildRejectResponse());
                        NotificationHelper.notifyBlockedCall(
                                getApplicationContext(), incomingNumber, result.getCategory());
                    } else {
                        respondToCall(callDetails, buildAllowResponse());
                    }
                } else {
                    respondToCall(callDetails, buildAllowResponse());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking number, allowing call: " + e.getMessage());
                respondToCall(callDetails, buildAllowResponse());
            }
        }).start();
    }

    private CallResponse buildRejectResponse() {
        return new CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSilenceCall(true)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build();
    }

    private CallResponse buildAllowResponse() {
        return new CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)
                .build();
    }
}
