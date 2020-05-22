package com.mastertek.web.rest.vm;

public class GetSimilarityVM {

	String operator = "GetPictureSimilarity";
	
	String picinfo1;
	
	String picinfo2;

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getPicinfo1() {
		return picinfo1;
	}

	public void setPicinfo1(String picinfo1) {
		this.picinfo1 = picinfo1;
	}

	public String getPicinfo2() {
		return picinfo2;
	}

	public void setPicinfo2(String picinfo2) {
		this.picinfo2 = picinfo2;
	}
	
	
}
