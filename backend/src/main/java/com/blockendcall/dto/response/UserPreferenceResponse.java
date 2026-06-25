package com.blockendcall.dto.response;

import com.blockendcall.entity.UserPreference;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPreferenceResponse {

    private Long id;
    private boolean blockOnlyConfirmed;
    private boolean notifyOnConfirm;
    private int sensitivity;
    private boolean paranoiaMode;
    private boolean blockTelemarketing;
    private boolean blockScam;
    private boolean blockRobocall;
    private boolean blockSilent;
    private boolean voicemailMode;

    public static UserPreferenceResponse from(UserPreference entity) {
        return UserPreferenceResponse.builder()
                .id(entity.getId())
                .blockOnlyConfirmed(entity.isBlockOnlyConfirmed())
                .notifyOnConfirm(entity.isNotifyOnConfirm())
                .sensitivity(entity.getSensitivity())
                .paranoiaMode(entity.isParanoiaMode())
                .blockTelemarketing(entity.isBlockTelemarketing())
                .blockScam(entity.isBlockScam())
                .blockRobocall(entity.isBlockRobocall())
                .blockSilent(entity.isBlockSilent())
                .voicemailMode(entity.isVoicemailMode())
                .build();
    }
}
