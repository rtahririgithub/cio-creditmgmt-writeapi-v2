package com.telus.credit.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TelusChannel extends Channel{
	private Boolean tenpubsubsync;
    public boolean getTenpubsubsync() {
        if (tenpubsubsync == null) {
            return false;
        }
        return tenpubsubsync;
    }

    public void setTenpubsubsync(Boolean tenpubsubsync) {
        this.tenpubsubsync = tenpubsubsync;
    }	
}
