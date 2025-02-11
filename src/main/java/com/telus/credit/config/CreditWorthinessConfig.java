package com.telus.credit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("credit.worthiness")
public class CreditWorthinessConfig {
    private String overrideCreditAssessmentType;
    private String auditCreditAssessmentType;
    private String bureauConsentSubType;
    private String manualOverrideSubType;

    public String getOverrideCreditAssessmentType() {
        return overrideCreditAssessmentType;
    }

    public void setOverrideCreditAssessmentType(String overrideCreditAssessmentType) {
        this.overrideCreditAssessmentType = overrideCreditAssessmentType;
    }

    public String getAuditCreditAssessmentType() {
        return auditCreditAssessmentType;
    }

    public void setAuditCreditAssessmentType(String auditCreditAssessmentType) {
        this.auditCreditAssessmentType = auditCreditAssessmentType;
    }

    public String getBureauConsentSubType() {
        return bureauConsentSubType;
    }

    public void setBureauConsentSubType(String bureauConsentSubType) {
        this.bureauConsentSubType = bureauConsentSubType;
    }

    public String getManualOverrideSubType() {
        return manualOverrideSubType;
    }

    public void setManualOverrideSubType(String manualOverrideSubType) {
        this.manualOverrideSubType = manualOverrideSubType;
    }

  /*  @Bean
    public WLNCreditProfileManagementProxyServicePortType getWLNCreditProfileManagementProxyServicePortType() {
        WLNCreditProfileManagementProxyServiceStub proxyServiceStub = new WLNCreditProfileManagementProxyServiceStub();
        return proxyServiceStub.getWLNCreditProfileManagementProxyServicePort();
    }
*/

}
