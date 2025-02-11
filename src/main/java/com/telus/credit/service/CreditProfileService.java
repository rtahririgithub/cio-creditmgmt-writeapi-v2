package com.telus.credit.service;

import com.telus.credit.common.RequestContext;
import com.telus.credit.dao.entity.CustomerEntity;
import com.telus.credit.model.AccountInfo;
import com.telus.credit.model.CreditProfile;
import com.telus.credit.model.Customer;
import com.telus.credit.model.ProductCategoryQualification;
import com.telus.credit.model.TelusChannel;
import com.telus.credit.model.TelusCreditProfile;

import java.util.List;

public interface CreditProfileService<T extends CreditProfile> {

    void createCreditProfile(String customerUid, TelusCreditProfile creditProfile);

    Customer createCreditProfileWithoutId(RequestContext context, TelusCreditProfile creditProfile, AccountInfo accountInfo, long receivedTime, long submitterEventTime, String eventDescription);

    void patchCreditProfileByCustId(CustomerEntity customerEntity, TelusCreditProfile creditProfile);

    //void patchCustomerCreditProfile(String customerUid, TelusCreditProfile creditProfile);

    //List<T> getCreditProfilsAndWarningsByCustomerTblPk(String customerUid);

    String getCustomerUid(String creditProfileId);

	List<TelusCreditProfile> getCreditProfiles_Warnings_And_CustomerRelation_ByCreditProfileCustomerId(String creditProfileCustomerId);
	
	void updateProdCateQual(String creditProfileId, List<ProductCategoryQualification> prodCatList,TelusChannel auditCharacteristic, String lineOfBusiness);

}
