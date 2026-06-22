package com.blockendcall.android.service;

import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.util.Log;

import com.blockendcall.android.api.ApiClient;
import com.blockendcall.android.api.BlockedNumberApi;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.BlockedNumber;
import com.blockendcall.android.util.SessionManager;

import retrofit2.Response;

/**
 * Intercepts incoming calls and silently rejects numbers confirmed as spam by the community.
 * Must be set as the default Call Screening app via system settings (or the role request dialog).
 */
public class BlockCallScreeningService extends CallScreeningService {

    private static final String TAG = "BlockCallScreening";

    @Override
    public void onScreenCall(Call.Details callDetails) {
        String incomingNumber = callDetails.getHandle().getSchemeSpecificPart();

        // Run network check on a background thread — onScreenCall is not on the main thread.
        new Thread(() -> {
            try {
                SessionManager session = new SessionManager(getApplicationContext());
                BlockedNumberApi api = ApiClient.getApi(session);

                Response<ApiResponse<BlockedNumber>> response =
                        api.checkNumber(incomingNumber).execute();

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().isSuccess()
                        && response.body().getData() != null) {

                    BlockedNumber blocked = response.body().getData();
                    Log.i(TAG, "Blocking spam call from " + incomingNumber
                            + " [" + blocked.getCategory() + ", "
                            + blocked.getReportCount() + " reports]");

                    respondToCall(callDetails, buildRejectResponse());
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
