package com.telus.credit.model;

import javax.validation.Valid;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.telus.credit.common.DtoRefectionToStringStyle;
import com.telus.credit.validation.ValidIdentification;
import com.telus.credit.validation.group.Create;
import com.telus.credit.validation.group.Patch;

@JsonInclude(Include.NON_NULL)
public class RelatedParty {

    @JsonProperty("id")
    private String id;
    private String role;
    private String href;
    private String name;
    @JsonProperty("@type")
    private String type;
    @JsonProperty("@schemaLocation")
    private String schemaLocation;
    @JsonProperty("@referredType")
    private String atReferredType;
    @JsonProperty("@baseType")
    private String atBaseType;

    
    //@NotNull(groups = Create.class, message = "1108")
    @ValidIdentification(groups = {Create.class, Patch.class})
    @Valid    
    private RelatedPartyToPatch engagedParty;
    
    public RelatedPartyToPatch getEngagedParty() {
 	      return engagedParty;
 	   }
    public void setEngagedParty(RelatedPartyToPatch engagedParty) {
       this.engagedParty = engagedParty;
    }  

  /*  
	private RelatedPartyInterface engagedParty;
	public RelatedPartyInterface getEngagedParty() {
		return engagedParty;
	}
	public void setEngagedParty(RelatedPartyInterface val) {
		this.engagedParty = val;
	}
	
*/	
	
    public String getId() {
        return id;
    }

    public void setId(String customerId) {
        this.id = customerId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }

    public void setSchemaLocation(String schemaLocation) {
        this.schemaLocation = schemaLocation;
    }

    public String getAtReferredType() {
        return atReferredType;
    }

    public void setAtReferredType(String atReferredType) {
        this.atReferredType = atReferredType;
    }

    public String getAtBaseType() {
        return atBaseType;
    }

    public void setAtBaseType(String atBaseType) {
        this.atBaseType = atBaseType;
    }
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, new DtoRefectionToStringStyle());
	}    
}
