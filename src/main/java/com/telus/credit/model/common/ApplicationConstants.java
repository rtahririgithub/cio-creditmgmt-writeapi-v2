package com.telus.credit.model.common;

public class ApplicationConstants {

	public static final String PROFILE_BASE_TYPE = "CreditProfile";
	public static final String PROFILE_TYPE = "TelusCreditProfile";

	public static final String INDIVIDUAL_IDENTIFICATION_BASE_TYPE = "IndividualIdentification";
	public static final String INDIVIDUAL_IDENTIFICATION_TYPE = "TelusIndividualIdentification";

	public static final String ORGANIZATION_IDENTIFICATION_BASE_TYPE = "OrganizationIdentification";
	public static final String ORGANIZATION_IDENTIFICATION_TYPE = "OrganizationIdentification";
	
	public static final String CUSTOMER_BASE_TYPE = "Customer";
	public static final String CUSTOMER_TYPE = "TelusCustomer";

	public static final String FR_LANG = "fr";
	public static final String EN_LANG = "en";
	
	public static final String LIMIT_KEY = "limit";
	public static final String SORT_KEY = "sort";
	public static final String BIRTH_DATE_KEY = "birthDate";
	
	public static final String SORT_BY_RISK_RATING = "creditProfile.creditRiskRating";
	public static final String SORT_BY_START_DTM = "creditProfile.validFor.startDateTime";
	
	public static final String FILTER_KEY = "fields";
	public static final String FILTER_BY_PROFILE = "creditProfile";
	public static final String FILTER_BY_PARTY = "engagedParty";
	public static final String FILTER_BY_NONE = "none";
	
	public static final String DEFAULT_DT_TM = "1800-01-02T00:00:00.000Z";
	
	private ApplicationConstants() {
		// constants
	}
}
