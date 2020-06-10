package com.mastertek.web.rest.vm;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PersonInfoVM{
	@JsonProperty("DeviceID")
	String deviceID;
	
	/*
	 * 
	0:Whitelist
	1: blacklist
	2-11: Personnel Type
	 * 
	 * */
	@JsonProperty("PersonType")
	Long personType=0l;//0:	Whitelist 	1: blacklist
	
	@JsonProperty("Name")
	String name;
	
	@JsonProperty("CardType")
	Long cardType;
	
	/*
	 * 0: permanent list
	   1: Temporary list 1 (start and end time period)
	 * */
	@JsonProperty("Tempvalid")
	Long tempvalid=0l;
	
	@JsonProperty("PersonUUID")
	String personUUID;
	
	/*
	 * 0~1
	 * */
	@JsonProperty("isCheckSimilarity")
	Long isCheckSimilarity;
	
	
	/*
	 * 2018-03-12T09:09:20
	 * */
	@JsonProperty("ValidBegin")
	String validBegin;
	
	/*
	 * 2018-03-12T09:09:20
	 * */
	
	@JsonProperty("ValidEnd")
	String validEnd;
	
	@JsonProperty("LibID")
	Long libID;
	
	@JsonProperty("CustomizeID")
	String customizeID;
	
	/*
	 *  0:CustomizeID
        1:LibID
        2:PersonUUid
	 * */
	@JsonProperty("IdType")
	Long idType = 2l;

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public Long getPersonType() {
		return personType;
	}

	public void setPersonType(Long personType) {
		this.personType = personType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getCardType() {
		return cardType;
	}

	public void setCardType(Long cardType) {
		this.cardType = cardType;
	}

	public Long getTempvalid() {
		return tempvalid;
	}

	public void setTempvalid(Long tempvalid) {
		this.tempvalid = tempvalid;
	}

	public String getPersonUUID() {
		return personUUID;
	}

	public void setPersonUUID(String personUUID) {
		this.personUUID = personUUID;
	}

	public Long getIsCheckSimilarity() {
		return isCheckSimilarity;
	}

	public void setIsCheckSimilarity(Long isCheckSimilarity) {
		this.isCheckSimilarity = isCheckSimilarity;
	}

	

	public String getValidBegin() {
		return validBegin;
	}

	public void setValidBegin(String validBegin) {
		this.validBegin = validBegin;
	}

	public String getValidEnd() {
		return validEnd;
	}

	public void setValidEnd(String validEnd) {
		this.validEnd = validEnd;
	}

	public Long getLibID() {
		return libID;
	}

	public void setLibID(Long libID) {
		this.libID = libID;
	}

	public String getCustomizeID() {
		return customizeID;
	}

	public void setCustomizeID(String customizeID) {
		this.customizeID = customizeID;
	}

	public Long getIdType() {
		return idType;
	}

	public void setIdType(Long idType) {
		this.idType = idType;
	}
	
	
}
