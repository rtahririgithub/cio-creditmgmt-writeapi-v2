package com.telus.credit.pubsub.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.credit.config.CreditPubSubConfig;
import com.telus.credit.config.CreditWorthinessConfig;
import com.telus.credit.model.TelusCreditProfile;
import com.telus.credit.model.TelusIndividualIdentification;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@SpringBootTest(
        classes = {TelusMDMEventSender.class, CreditPubSubConfig.class, CreditWorthinessConfig.class}
)
@ActiveProfiles("cloud-it03")
public class TelusMDMEventSenderTest {

   private static final String eventType = "CREATE";

    private static final String eventTypeUpdate = "UPDATE";

   private TelusMDMEventSender mdmEventSender;

   @BeforeEach
    public void setup() {

       CreditPubSubConfig creditPubSubConfig = new CreditPubSubConfig();
       creditPubSubConfig.setPubSubURL("https://enterprisemessagewebk-is02.tsl.telus.com/publisher/publish/PT05.Party.Customer.CreditInfo");
       creditPubSubConfig.setUsername("9343");
       creditPubSubConfig.setPassword("removed");//TODO DO NOT HARDCODE ANY PASSWORD IN THE CODE. get the password from application property file 

       CreditWorthinessConfig creditWorthinessConfig = new CreditWorthinessConfig();
       creditWorthinessConfig.setAuditCreditAssessmentType("AUDIT");
       creditWorthinessConfig.setBureauConsentSubType("BUREAU_CONSENT");
       creditWorthinessConfig.setOverrideCreditAssessmentType("OVRD_ASSESSMENT");
       creditWorthinessConfig.setManualOverrideSubType("MANUAL_OVERRIDE");

       mdmEventSender =  new TelusMDMEventSender(creditPubSubConfig, new RestTemplate(), creditWorthinessConfig);
    }



    @Test
    public void testCreateJSONMessageForCreditIds()  throws IOException {
        TelusCreditProfile creditProfile = getTelusCreditProfile();
        List<TelusIndividualIdentification> telusIndividualIdentifications = creditProfile.getRelatedParties().get(0).getEngagedParty().getIndividualIdentification();
        System.out.println("Size:"+telusIndividualIdentifications.size());
        String message = mdmEventSender.createJSONMessageForCreditIds(telusIndividualIdentifications, "10", "1323", "CREATE");
        mdmEventSender.publish(message);
        System.out.println("message::" + message);
    }
    @Test
    public void testUpdate()  throws IOException {
        TelusCreditProfile creditProfile = getTelusCreditProfile();
        List<TelusIndividualIdentification> telusIndividualIdentifications = creditProfile.getRelatedParties().get(0).getEngagedParty().getIndividualIdentification();
        System.out.println("Size:"+telusIndividualIdentifications.size());
        String message = mdmEventSender.createJSONMessageForCreditIds(telusIndividualIdentifications, "120642729", "1323", "UPDATE");
        mdmEventSender.publish(message);
        System.out.println("message::" + message);
    }
    @Test
    public void testCreateJSONMessageForDOB()  throws IOException {
        String message = mdmEventSender.createJSONMessageForDOB("10", new Date(), "3235");
        mdmEventSender.publish(message);
        System.out.println("message:"+message);
    }
    private TelusCreditProfile getTelusCreditProfile() throws IOException {
        String payload = IOUtils.toString(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("credit-profile-request-update-DL.json")), StandardCharsets.UTF_8);
        return  new ObjectMapper().readValue(payload, TelusCreditProfile.class);
    }
}
