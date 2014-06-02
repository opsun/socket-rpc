package com.opsunv.rpc;

import java.io.Serializable;
import java.util.UUID;

/**
 * RPC调用描述信息
 * @author opsun
 *
 */
public class CallInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String callId = UUID.randomUUID().toString();

	private String name;
	
	private Object[] parameters;
	
	public CallInfo() {
	}

	/**
	 * @param name
	 * @param parameters
	 */
	public CallInfo(String name, Object[] parameters) {
		this.name = name;
		this.parameters = parameters;
	}



	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the parameters
	 */
	public Object[] getParameters() {
		return parameters;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(Serializable[] parameters) {
		this.parameters = parameters;
	}
	
	public String getCallId() {
		return callId;
	}
	
	@Override
	public String toString() {
		return "callId="+callId+",name="+name;
	}
}
