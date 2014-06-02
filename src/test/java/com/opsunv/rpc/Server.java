package com.opsunv.rpc;

import com.opsunv.rpc.server.CallDefine;
import com.opsunv.rpc.server.RpcServer;
import com.opsunv.rpc.server.RpcServerImpl;


/**
 * 启动服务端
 * @author opsun
 *
 */
public class Server {
	
	public static void main(String[] args) throws Exception {
		RpcServer server = new RpcServerImpl();
		
		MyTest obj = new MyTest();
		
		server.addCallDefine("sayHello", new CallDefine(obj,Utils.getMethod(MyTest.class, "sayHello")));
		server.addCallDefine("getList", new CallDefine(obj,Utils.getMethod(MyTest.class, "getList")));
		server.addCallDefine("print", new CallDefine(obj,Utils.getMethod(MyTest.class, "print")));
		
		server.init();
		server.start();
	}
}
