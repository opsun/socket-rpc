package com.opsunv.rpc;

import java.io.Serializable;

/**
 * RPC返回
 * @author opsun
 *
 */
public class CallResponse implements Serializable{
	private static final long serialVersionUID = 1L;

	/**
	 * 调用成功
	 */
	public final static int SUCCESS = 1;
	
	/**
	 * 远程调用处理过程发生异常
	 */
	public final static int EXCEPTION = 2;
	
	/**
	 * 方法不存在
	 */
	public final static int METHOD_NOT_FOUND = 3;
	
	private String callId;
	
	private Object result;
	
	private int state;

	/**
	 * @return the callId
	 */
	public String getCallId() {
		return callId;
	}

	/**
	 * @param callId the callId to set
	 */
	public void setCallId(String callId) {
		this.callId = callId;
	}

	/**
	 * @return the result
	 */
	public Object getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(Object result) {
		this.result = result;
	}

	/**
	 * @return the state
	 */
	public int getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(int state) {
		this.state = state;
	}
	
}
