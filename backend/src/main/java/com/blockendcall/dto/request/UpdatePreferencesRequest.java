package com.blockendcall.dto.request;

import lombok.Data;

@Data
public class UpdatePreferencesRequest {

    private Boolean blockOnlyConfirmed;
    private Boolean notifyOnConfirm;
    private Integer sensitivity;
    private Boolean paranoiaMode;
    private Boolean blockTelemarketing;
    private Boolean blockScam;
    private Boolean blockRobocall;
    private Boolean blockSilent;
    private Boolean voicemailMode;
}
