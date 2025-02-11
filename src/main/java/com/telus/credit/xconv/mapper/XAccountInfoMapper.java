package com.telus.credit.xconv.mapper;

import com.telus.credit.dao.entity.XCreditProfileEntity;
import com.telus.credit.model.AccountInfo;

public class XAccountInfoMapper {
    private XAccountInfoMapper() {
        // utils
    }

    public static AccountInfo fromEntity(XCreditProfileEntity xcpe) {
        if (xcpe == null) {
            return null;
        }

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setAccountType(xcpe.getAccountType());
        accountInfo.setAccountSubType(xcpe.getAccountSubtype());
        accountInfo.setStatus(xcpe.getStatus());
        accountInfo.setBrandId(xcpe.getBrandId());
        accountInfo.setLanguage(xcpe.getLanguage());
        accountInfo.setStatusDate(xcpe.getStatusDate());
        accountInfo.setStartServiceDate(xcpe.getStartServiceDate());
        accountInfo.setStatusActivityCode(xcpe.getStatusActivityCode());
        accountInfo.setStatusActivityReasonCode(xcpe.getStatusActivityReasonCode());
        accountInfo.setDealerCode(xcpe.getDealerCode());
        accountInfo.setSalesRepCode(xcpe.getSalesRepCode());
        accountInfo.setCorpAcctRepCode(xcpe.getCorpAcctRepCode());
        accountInfo.setFullName(xcpe.getFullname());
        accountInfo.setTitle(xcpe.getTitle());
        accountInfo.setFirstName(xcpe.getFirstname());
        accountInfo.setMiddleInitial(xcpe.getMiddleInitial());
        accountInfo.setLastName(xcpe.getLastname());
        accountInfo.setLegalBusinessName(xcpe.getLegalBusinessName());

        return accountInfo;
    }
}
