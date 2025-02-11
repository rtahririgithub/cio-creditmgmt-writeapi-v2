package com.telus.credit.exceptions;

public final class ExceptionConstants {
   public static final String ERR_CODE_1000 = "1000";
   public static final String ERR_CODE_1000_MSG = "Invalid or missing mandatory request parameter(s)";
   
   public static final String ERR_CODE_401_MSG = "{ \"message\":\"Either the Bearer Token is missing or expired\"}";
   
   public static final String ERR_CODE_1400 = "1400";
   public static final String ERR_CODE_1400_MSG = "Customer not found for the given Id";
   
   public static final String ERR_CODE_1401 = "1401";
   public static final String ERR_CODE_1401_MSG = "Customer is not a merged customer";
      
   
   public static final String ERR_CODE_1501 = "1501";
   public static final String ERR_CODE_1501_MSG = "birthDate if provided must be in format YYYY-MM-dd";
   
   public static final String ERR_CODE_1502 = "1502";
   public static final String ERR_CODE_1502_MSG = "Sort attribute specified that is not supported for sorting";

   public static final String ERR_CODE_1503 = "1503";
   public static final String ERR_CODE_1503_MSG = "Fields attribute specified that is not supported";

   public static final String ERR_CODE_1504 = "1504";
   public static final String ERR_CODE_1504_MSG = "Only one parameter is allowed to have multiple values and combination of OR and AND is not supported.";
   
   public static final String ERR_CODE_1505 = "1505";
   public static final String ERR_CODE_1505_MSG = "Invalid search criteria provided. Some or all fields param(s) are invalid";
   
   
   public static final String ERR_CODE_1506 = "1506";
   public static final String ERR_CODE_1506_MSG = "Each Search parameters value should not exceed more than 10 values";
   
   
   public static final String ERR_CODE_1507 = "1507";
   public static final String ERR_CODE_1507_MSG = "Missing related party with customer role";

   public static final String ERR_CODE_1115 = "1115";
   public static final String ERR_CODE_1115_MSG = "Invalid credit class code";
   // Sys errors
   public static final String ERR_CODE_8000 = "8000";   
   public static final String ERR_CODE_8000_MSG = "Unexpected error occurred accessing database.";
   
   public static final String ERR_CODE_8001 = "8001";   
   public static final String ERR_CODE_8001_MSG = "Unexpected error occurred mapping to CreditProfile Entity .";
   
   // PubSub subscriber errors
   public static final String PUBSUB100 = "PUBSUB-100";
   public static final String PUBSUB101 = "PUBSUB-101";
   public static final String PUBSUB102 = "PUBSUB-102";
   public static final String PUBSUB103 = "PUBSUB-103";

   // audit error
   public static final String PUBSUB200 = "PUBSUB-200";
   // error on save customer
   public static final String PUBSUB201 = "PUBSUB-201";
   // error in XConv
   public static final String PUBSUB202 = "PUBSUB-202";
   // Error in CAPI
   public static final String PUBSUB203 = "PUBSUB-203";

   // General errors
   public static final String POSTGRES100 = "POSTGRES-100";
   // XConversion errors
   public static final String POSTGRES101 = "POSTGRES-101";

   // General validation errors
   public static final String DATAVALIDATION100 = "DATAVALIDATION-100";
   // invalid Number/datetime format
   public static final String DATAVALIDATION101 = "DATAVALIDATION-101";

   public static final String STACKDRIVER_METRIC = "CreditProfileAPIError";   
   //Indented use is STATIC in Nature
   private ExceptionConstants() {
	   
   }
}
