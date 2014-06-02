package com.opsunv.rpc.client;

import com.opsunv.rpc.exception.RpcException;

/**
 * 
 * @author opsun
 *
 */
public interface RpcClient {
	
	public void init();
	
	public void destory();
	
	/**
	 * 调用远程方法
	 * @param method
	 * @param obj
	 * @return
	 * @throws RpcException
	 */
	public Object invoke(String method,Object[] obj) throws RpcException;
	
}
