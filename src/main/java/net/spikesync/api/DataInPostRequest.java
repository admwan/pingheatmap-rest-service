package net.spikesync.api;

import javax.validation.constraints.NotEmpty;


//@Component("DataInPostRequest")
public class DataInPostRequest {

    @NotEmpty(message = "Attribute value #1 in POST request data can not be empty!")
    String postReqDataElm1;

    @NotEmpty(message ="Attribute value #2 in POST request data can not be empty!")
    String postReqDataElm2;

   
    public void setPostRequestDataVal1(String attrVal1) {
        this.postReqDataElm1 = attrVal1;
    }
  
    public String getPostRequestDataVal1() {
        return postReqDataElm1;
    }

    public void setPostRequestDataVal2(String attrVal2) {
		this.postReqDataElm2 = attrVal2;
	}
    
    
    public String getPostRequestDataVal2() {
		return postReqDataElm2;
	}
    
}