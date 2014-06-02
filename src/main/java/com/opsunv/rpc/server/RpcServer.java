package com.opsunv.rpc.server;

public interface RpcServer {
	public void init();
	
	public void start();
	
	public void stop();
	
	/**
	 * 添加call方法
	 * @param key
	 * @param callDefine
	 * @return
	 */
	public boolean addCallDefine(String key,CallDefine callDefine);
}
