package com.telus.credit.common;

public enum ErrorCode {

    C_1100("Credit Profile information is required"),
    C_1101("ValidFor time period is invalid.  The start date needs to be before the end date and the end date needs to be after the start date"),
    C_1102("Invalid bureau decision code"),
    C_1103("Invalid referred type. Please specify individual or organization"),
    C_1104("Invalid birth date"),
    C_1105("Contact medium mediumType is required"),
    C_1106("EngagedParty cannot have both individual and organization identifications"),
    C_1107("Only one identification for a given identification type is supported"),
    C_1108("Engaged Party is required"),
    C_1109("Country code is invalid"),
    C_1110("Province code is invalid"),
    C_1111("Invalid identification type"),
    C_1112("Originator application Id is required"),
    C_1113("UserId is required"),
    C_1114("Invalid credit program name"),
    C_1115("Invalid credit class code"),
    C_1116("Invalid warning category code"),
    C_1117("Invalid warning status code"),
    C_1118("Warning detection date is required"),
    C_1119("Timestamp needs to be in UTC"),
    C_1120("Invalid credit profile id"),
    C_1121("Invalid contact medium id"),
    C_1122("Invalid warning history id"),
    C_1123("Missing identification Id"),
    C_1124("Credit Risk Rating must be positive"),
    C_1125("Invalid identificationId"),
    C_1126("Invalid Line Of Business"),
    C_1401("Customer ID must be numeric"),
	C_1500("At least one valid search parameter is required.")

	
	
	;

    private static final String PREFIX = "C_";

    private String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String code() {
        return name().replace(PREFIX, "");
    }

    public String getMessage() {
        return this.message;
    }

    public static ErrorCode from(String code) {
        for (ErrorCode value : values()) {
            if (value.name().equals(PREFIX + code) || value.name().equalsIgnoreCase(code)) {
                return value;
            }
        }

        return null;
    }
}
