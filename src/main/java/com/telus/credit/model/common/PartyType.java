package com.telus.credit.model.common;

/**
 * Party type is Individual or Organization
 */
public enum PartyType {

	INDIVIDUAL("Individual"),
	ORGANIZATION("Organization");
	
	private final String type;
	
	PartyType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
