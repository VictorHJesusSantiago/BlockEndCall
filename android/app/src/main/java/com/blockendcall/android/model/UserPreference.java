package com.blockendcall.android.model;

import com.google.gson.annotations.SerializedName;

public class UserPreference {
    @SerializedName("blockOnlyConfirmed") private boolean blockOnlyConfirmed;
    @SerializedName("notifyOnConfirm") private boolean notifyOnConfirm;
    @SerializedName("sensitivity") private int sensitivity;
    @SerializedName("paranoiaMode") private boolean paranoiaMode;
    @SerializedName("blockTelemarketing") private boolean blockTelemarketing;
    @SerializedName("blockScam") private boolean blockScam;
    @SerializedName("blockRobocall") private boolean blockRobocall;
    @SerializedName("blockSilent") private boolean blockSilent;
    @SerializedName("voicemailMode") private boolean voicemailMode;
    public boolean isBlockOnlyConfirmed() { return blockOnlyConfirmed; }
    public boolean isNotifyOnConfirm() { return notifyOnConfirm; }
    public int getSensitivity() { return sensitivity; }
    public boolean isParanoiaMode() { return paranoiaMode; }
    public boolean isBlockTelemarketing() { return blockTelemarketing; }
    public boolean isBlockScam() { return blockScam; }
    public boolean isBlockRobocall() { return blockRobocall; }
    public boolean isBlockSilent() { return blockSilent; }
    public boolean isVoicemailMode() { return voicemailMode; }
}
