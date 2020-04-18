package com.mastertek.web.rest.vm;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OpenDoorInfoVM {
	
	@JsonProperty("DeviceID")
	String DeviceID;
	@JsonProperty("Chn")
	String Chn;
	String status;
	String msg;
	
	
	
	
	public OpenDoorInfoVM(String deviceID, String msg) {
		super();
		this.DeviceID = deviceID;
		this.msg = msg;
		this.Chn = "1";
		this.status="9";
	}
	public String getDeviceID() {
		return DeviceID;
	}
	public void setDeviceID(String deviceID) {
		DeviceID = deviceID;
	}
	public String getChn() {
		return Chn;
	}
	public void setChn(String chn) {
		Chn = chn;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	
}
