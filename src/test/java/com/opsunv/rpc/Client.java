package com.opsunv.rpc;

import com.opsunv.rpc.client.RpcClient;
import com.opsunv.rpc.client.RpcClientImpl;

/**
 * 启动客户端
 * @author opsun
 *
 */
public class Client {
	public static void main(String[] args) throws Exception{
		final RpcClient client = new RpcClientImpl();
		client.init();
		
		System.out.println(client.invoke("sayHello", new Object[]{"joseph"}));
		System.out.println(client.invoke("getList", new Object[]{}));
		System.out.println(client.invoke("print", new Object[]{"joseph"}));
		
		//调用异常
		try{
			System.out.println(client.invoke("print", new Object[]{}));
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		//方法未找到
		try{
			System.out.println(client.invoke("asdf", new Object[]{}));
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		
		//多线程调用
		for(int i=0;i<30;i++){
			final int a = i%3; 
			new Thread(){
				public void run() {
					try{
						switch(a){
							case 0:System.out.println(client.invoke("sayHello", new Object[]{"joseph"}));break;
							case 1:System.out.println(client.invoke("getList", new Object[]{}));break;
							case 2:System.out.println(client.invoke("print", new Object[]{"joseph"}));break;
						}
					}catch (Exception e) {
						e.printStackTrace();
					}
					
				};
			}.start();
		}
	}
}
