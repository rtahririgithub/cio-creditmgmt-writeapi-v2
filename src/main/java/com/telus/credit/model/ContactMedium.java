package com.telus.credit.model;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.cloud.firestore.annotation.Exclude;
import com.telus.credit.common.DtoRefectionToStringStyle;
import com.telus.credit.model.helper.PatchField;
import com.telus.credit.validation.ValidTimePeriod;
import com.telus.credit.validation.group.Create;
import com.telus.credit.validation.group.Patch;

@JsonInclude(Include.NON_NULL)
public class ContactMedium {
    private String id;
    
    @NotBlank(groups = Create.class, message = "1105")
    private String mediumType;
    private Boolean preferred;
    @Valid
    private MediumCharacteristic characteristic;

    @ValidTimePeriod(groups = {Create.class, Patch.class})
    private TimePeriod validFor;

    @JsonIgnore
    private boolean mediumTypeDirty = false;
    @JsonIgnore
    private boolean preferredDirty = false;
    @JsonIgnore
    private boolean characteristicDirty = false;
    @JsonIgnore
    private boolean validForDirty = false;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMediumType() {
        return mediumType;
    }

    public void setMediumType(String mediumType) {
        this.mediumType = mediumType;
        this.mediumTypeDirty = true;
    }

    public Boolean getPreferred() {
        return preferred;
    }

    public void setPreferred(Boolean preferred) {
        this.preferred = preferred;
        this.preferredDirty = true;
    }

    public MediumCharacteristic getCharacteristic() {
        return characteristic;
    }

    public void setCharacteristic(MediumCharacteristic characteristic) {
        this.characteristic = characteristic;
        this.characteristicDirty = true;
    }

    public TimePeriod getValidFor() {
        return validFor;
    }

    public void setValidFor(TimePeriod validFor) {
        this.validFor = validFor;
        this.validForDirty = true;
    }

    @JsonIgnore
    @Exclude
    public PatchField<@NotBlank(groups = Patch.class, message = "1105") String> getMediumTypePatch() {
        return PatchField.patchOrNull(mediumTypeDirty, mediumType);
    }

    @JsonIgnore
    @Exclude
    public PatchField<Boolean> getPreferredPatch() {
        return PatchField.patchOrNull(preferredDirty, preferred);
    }

    @JsonIgnore
    @Exclude
    public PatchField<MediumCharacteristic> getCharacteristicPatch() {
        return PatchField.patchOrNull(characteristicDirty, characteristic);
    }

    @JsonIgnore
    @Exclude
    public PatchField<TimePeriod> getValidForPatch() {
        return PatchField.patchOrNull(validForDirty, validFor);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, new DtoRefectionToStringStyle());
    }
}
