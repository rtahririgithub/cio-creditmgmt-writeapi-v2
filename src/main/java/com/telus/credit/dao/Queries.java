package com.telus.credit.dao;

public class Queries {

    private Queries() {
        // Utils
    }

    public static final String SELECT_CUSTOMER =
            "SELECT t1.* FROM CRPROFL.CUSTOMER t1   WHERE t1.CUSTOMER_ID =?";

    public static final String SELECT_CUSTOMER_CREDIT_PROFILE_CUSTOMER_ID =
            "SELECT t1.* FROM CRPROFL.CUSTOMER t1 WHERE t1.CREDIT_PROFILE_CUSTOMER_ID =?";
    
    public static final String SELECT_CUSTOMER_PARTY =
            "SELECT t1.*, t2.* FROM  CRPROFL.CUSTOMER t1  LEFT JOIN  CRPROFL.PARTY t2 on t1.PARTY_ID = t2.PARTY_ID  WHERE t1.CUSTOMER_ID =?";

    public static final String SELECT_CUSTOMER_PARTY_BY_LINEOFBUSINESS=
            "SELECT t1.*, t2.* FROM  CRPROFL.CUSTOMER t1  LEFT JOIN CRPROFL.PARTY t2 on t1.PARTY_ID = t2.PARTY_ID WHERE t1.CUSTOMER_ID =? AND t1.line_of_business=?";    
}
