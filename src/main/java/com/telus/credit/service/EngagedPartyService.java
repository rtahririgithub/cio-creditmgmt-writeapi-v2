package com.telus.credit.service;

import com.telus.credit.model.RelatedParty;
import com.telus.credit.model.RelatedPartyInterface;
import com.telus.credit.model.RelatedPartyToPatch;
import com.telus.credit.model.TelusChannel;

public interface EngagedPartyService {

    String createEngagedParty(RelatedPartyToPatch engagedParty, TelusChannel auditCharacteristic);
    void patchEngagedParty(String id, RelatedParty relatedParty, TelusChannel auditCharacteristic, String consentCode, String lineOfBusiness);

    RelatedPartyInterface getEngagedParty(String partyId);
}
