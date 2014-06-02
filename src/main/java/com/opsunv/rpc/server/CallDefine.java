package com.opsunv.rpc.server;

import java.lang.reflect.Method;

/**
 * 定义RPC可调用的方法
 * @author opsun
 *
 */
public class CallDefine {
	
	//实例
	private Object obj;
	
	//方法名
	private Method method;
	
	public CallDefine() {
	}
	
	
	/**
	 * @param obj
	 * @param method
	 */
	public CallDefine(Object obj, Method method) {
		this.obj = obj;
		this.method = method;
	}



	public Object invoke(Object[] args) throws Exception{
		return method.invoke(obj, args);
	}

	/**
	 * @return the obj
	 */
	public Object getObj() {
		return obj;
	}

	/**
	 * @param obj the obj to set
	 */
	public void setObj(Object obj) {
		this.obj = obj;
	}

	/**
	 * @return the method
	 */
	public Method getMethod() {
		return method;
	}

	/**
	 * @param method the method to set
	 */
	public void setMethod(Method method) {
		this.method = method;
	}
	
}
