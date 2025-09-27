package com.kairos.agentic_framework.agent;

import java.util.Map;

public interface Context {
	
	public Object getFieldValue(String fieldName);
	
	public void setFieldValue(String fieldName, Object value);
	
	public Map<String,Object> getValues();
	
	public boolean isComplete();
	

}
