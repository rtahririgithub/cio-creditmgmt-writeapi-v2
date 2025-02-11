package com.telus.credit.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.telus.credit.common.DtoRefectionToStringStyle;

@JsonInclude(Include.NON_NULL)
public class CustomerPubSub extends Customer {
   	private static final long serialVersionUID = 1L;

	private AccountInfo accountInfo;
	private String createUpdateFlag;
	private long eventReceivedTime;
	private long submitterEventTime;
	private String eventDescription;



	public CustomerPubSub() {
		// Serializer/deserializer
	}

	/**
	 * Because credit sync service can not deserialize dynamic instances,
	 * we need to use separate attribute of each data type
	 *
	 * @param customer
	 */
	public CustomerPubSub(Customer customer, AccountInfo accountInfo, String createUpdateFlag, long eventReceivedTime) {
		this.setId(customer.getId());
		this.setCreditProfile(customer.getCreditProfile());
		this.setAccountInfo(accountInfo);
		this.setCreateUpdateFlag(createUpdateFlag);
		this.setEventReceivedTime(eventReceivedTime);

	}

	public CustomerPubSub(Customer customer, AccountInfo accountInfo, String createUpdateFlag, long eventReceivedTime,String eventDescription,long submitterEventTime) {
		
		this.setId(customer.getId());
		this.setCreditProfile(customer.getCreditProfile());
		this.setAccountInfo(accountInfo);
		this.setCreateUpdateFlag(createUpdateFlag);
		this.setEventReceivedTime(eventReceivedTime);
		this.setEventDescription(eventDescription);
		this.setSubmitterEventTime(submitterEventTime);

	}

	public AccountInfo getAccountInfo() {
		return accountInfo;
	}

	public void setAccountInfo(AccountInfo accountInfo) {
		this.accountInfo = accountInfo;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, new DtoRefectionToStringStyle());
	}

   public String getCreateUpdateFlag() {
      return createUpdateFlag;
   }

   public void setCreateUpdateFlag(String createUpdateFlag) {
      this.createUpdateFlag = createUpdateFlag;
   }

   public long getEventReceivedTime() {
      return eventReceivedTime;
   }

   public void setEventReceivedTime(long eventReceivedTime) {
      this.eventReceivedTime = eventReceivedTime;
   }
	public long getSubmitterEventTime() {
	return submitterEventTime;
}

public void setSubmitterEventTime(long submitterEventTime) {
	this.submitterEventTime = submitterEventTime;
}

	public String getEventDescription() {
		return eventDescription;
	}

	public void setEventDescription(String eventDescription) {
		this.eventDescription = eventDescription;
	}



}
