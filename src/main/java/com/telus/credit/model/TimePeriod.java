package com.telus.credit.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.cloud.firestore.annotation.Exclude;
import com.telus.credit.model.helper.PatchField;

public class TimePeriod {
   private String endDateTime;
   private String startDateTime;

   @JsonIgnore
   private boolean endDateTimeDirty = false;
   @JsonIgnore
   private boolean startDateTimeDirty = false;

   public String getEndDateTime() {
      return endDateTime;
   }
   public void setEndDateTime(String endDateTime) {
      this.endDateTime = endDateTime;
      this.endDateTimeDirty = true;
   }
   public String getStartDateTime() {
      return startDateTime;
   }
   public void setStartDateTime(String startDateTime) {
      this.startDateTime = startDateTime;
      this.startDateTimeDirty = true;
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getEndDateTimePatch() {
      return endDateTimeDirty ? PatchField.of(endDateTime) : null;
   }

   @JsonIgnore
   @Exclude
   public PatchField<String> getStartDateTimePatch() {
      return startDateTimeDirty ? PatchField.of(startDateTime) : null;
   }

   @Override
   public String toString() {
      return new ToStringBuilder(this)
              .append("endDateTime", endDateTime)
              .append("startDateTime", startDateTime)
              .toString();
   }
}
