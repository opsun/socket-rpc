package com.opsunv.rpc.server;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.opsunv.rpc.CallInfo;
import com.opsunv.rpc.CallResponse;

public class RpcServerImpl implements RpcServer{
	private Map<String, CallDefine> calls = new ConcurrentHashMap<String, CallDefine>();
	
	private TcpServer tcpServer;
	
	@Override
	public boolean addCallDefine(String key, CallDefine callDefine) {
		calls.put(key, callDefine);
		return true;
	}

	@Override
	public void init() {
		tcpServer = new TcpServer(this);
	}

	@Override
	public void start() {
		try {
			tcpServer.init();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
		tcpServer.destory();
	}
	
	public CallResponse invoke(CallInfo info){
		System.out.println("rpc："+info);
		
		CallResponse response = new CallResponse();
		response.setCallId(info.getCallId());
		
		//如果方法名为空直接返回
		if(info.getName()==null){
			response.setState(CallResponse.METHOD_NO_FOUND);
			return response;
		}
		
		CallDefine callDefine = calls.get(info.getName());
		
		//不存在访问的rpc方法,直接返回
		if(callDefine==null){
			response.setState(CallResponse.METHOD_NO_FOUND);
		}else{
			
			//调用方法
			try {
				Object rs = callDefine.invoke(info.getParameters());
				response.setResult(rs);
				response.setState(CallResponse.SUCCESS);
			} catch (Exception e) {
				//简单标示异常
				response.setState(CallResponse.EXCEPTION);
			}
		}
		
		return response;
	}

}
