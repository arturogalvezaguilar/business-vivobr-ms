package com.lamark.business.vivobr.core.request;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
public class MORequestJboss {
    private String keyword ; 
    private String msiSdn;
    private String shortCode;
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public String getMsiSdn() {
		return msiSdn;
	}
	public void setMsiSdn(String msiSdn) {
		this.msiSdn = msiSdn;
	}
	public String getShortCode() {
		return shortCode;
	}
	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	} 
  
    
}
