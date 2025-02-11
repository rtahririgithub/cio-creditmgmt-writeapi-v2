package com.telus.credit.model;

import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.telus.credit.common.DtoRefectionToStringStyle;
import com.telus.credit.model.helper.PatchField;
import com.telus.credit.validation.group.Create;
import com.telus.credit.validation.group.Patch;

@JsonInclude(Include.NON_NULL)
public class TelusAuditCharacteristic {

    @NotBlank(groups = {Create.class, Patch.class}, message = "1112")
    private String originatorApplicationId;
    private String channelOrganizationId;
    
    
    @NotBlank(groups = {Create.class, Patch.class}, message = "1113")
    private String userId;

    private Boolean tenpubsubsync;

    private boolean channelOrganizationIdDirty = false;

    public String getOriginatorApplicationId() {
        return originatorApplicationId;
    }
    public void setOriginatorApplicationId(String originatorApplicationId) {
        this.originatorApplicationId = originatorApplicationId;
    }
    public String getChannelOrganizationId() {
        return channelOrganizationId;
    }
    public void setChannelOrganizationId(String channelOrganizationId) {
        this.channelOrganizationId = channelOrganizationId;
        this.channelOrganizationIdDirty = true;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public PatchField<String> getChannelOrganizationIdPatch() {
        return PatchField.patchOrNull(channelOrganizationIdDirty, channelOrganizationId);
    }

    public boolean getTenpubsubsync() {
        if (tenpubsubsync == null) {
            return false;
        }
        return tenpubsubsync;
    }

    public void setTenpubsubsync(Boolean tenpubsubsync) {
        this.tenpubsubsync = tenpubsubsync;
    }

    @Override
	public String toString() {
        return ToStringBuilder.reflectionToString(this, new DtoRefectionToStringStyle());
	}
}
