package com.opsunv.rpc.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import com.opsunv.rpc.CallInfo;
import com.opsunv.rpc.CallResponse;
import com.opsunv.rpc.Utils;
import com.opsunv.rpc.exception.RpcException;

/**
 * 基于Socket nio的Rpc调用客户端。
 * TODO 完善异常处理，通知方式，calls中可能存在的脏数据处理.
 * @author opsun
 *
 */
public class RpcClientImpl implements RpcClient{
	private Map<String, Call> calls = new ConcurrentHashMap<String, Call>();
	
	private String host = "127.0.0.1";
	
	private int port = 5333;
	
	private Selector selector;
	
	private SocketChannel channel;
	
	private volatile boolean isStart = false;
	
	public RpcClientImpl() {
		
	}
	
	/**
	 * @param host
	 * @param port
	 */
	public RpcClientImpl(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public void init(){
		try{
			//获取socket通道
			channel = SocketChannel.open();
	        channel.connect(new InetSocketAddress(host, port));
	        
	        //等待连接建立
	        channel.finishConnect();
	        
	        channel.configureBlocking(false);
	        
	        //注册选择器
	        selector = Selector.open();
	        channel.register(selector, SelectionKey.OP_READ);
	        
	        isStart = true;
	        new Master().start();
	        System.out.println("客户端初始化完成..");
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@Override
	public void destory() {
		isStart = false;
		try {
			selector.close();
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public Object invoke(String method, Object[] obj) throws RpcException{
		System.out.println("call:"+method);
		CallInfo info = new CallInfo(method, obj);
		Call call = new Call();
		calls.put(info.getCallId(), call);
		try {
			send(info);
		} catch (IOException e) {
			calls.remove(info.getCallId());
			throw new RpcException("连接异常");
		}
		
		CallResponse response = call.getResponse();
		calls.remove(info.getCallId());
		
		switch(response.getState()){
			case CallResponse.EXCEPTION:
				throw new RpcException("调用异常");
			case CallResponse.METHOD_NO_FOUND:
				throw new RpcException("方法未找到");
			case CallResponse.SUCCESS:
				return response.getResult();
			default:
				return null;
		}
	}
	
	/**
	 * 向服务端发送数据
	 * @param info
	 */
	private void send(CallInfo info) throws IOException{
		SelectionKey key = channel.register(selector, SelectionKey.OP_WRITE, info);
		key.interestOps(key.interestOps()|SelectionKey.OP_WRITE);
		selector.wakeup();
		//channel.write(ByteBuffer.wrap(Utils.pack(info)));
	}

	private class Master extends Thread{
		@Override
		public void run() {
			while(isStart){
				try{
					selector.select();
		            Iterator<SelectionKey> ite = selector.selectedKeys().iterator();
		            while(ite.hasNext()){
		                SelectionKey key = ite.next();
		                ite.remove();
		                if(key.isReadable()){
		                	process(key);
		                }else if(key.isWritable()){
		                	if(key.attachment() instanceof CallInfo){
		                		channel.write(ByteBuffer.wrap(Utils.pack(key.attachment())));	
		                	}
		                	key.interestOps(SelectionKey.OP_READ);  
		                }
		            }
				}catch (Exception e) {
					e.printStackTrace();
				}
	            
	        }
		}
		
		private void process(SelectionKey key){
			SocketChannel channel = (SocketChannel)key.channel();
			try{
				//定义每个read开始的前4字节是一个请求数据区的长度
	            ByteBuffer buffer = ByteBuffer.allocate(4);
				int read = channel.read(buffer);
				
				if(read<1){
            		//没有数据,继续监听OP_READ
//            		key.interestOps(key.interestOps() | SelectionKey.OP_READ);
//                    key.selector().wakeup();
                    return;
            	}else if(read!=4){
					return;
				}
				
				//得到数据区的长度，这里简单处理直接读取全部数据区.
				int len = Utils.bytes2Int(buffer.array());
				buffer = ByteBuffer.allocate(len);
				
				//读取数据区并反序列化
				channel.read(buffer);
	            Object obj = Utils.unseralize(buffer.array());
	            
	            if(obj instanceof CallResponse){
	            	CallResponse response = (CallResponse)obj;
	            	Call call = calls.get(response.getCallId());
	            	if(call!=null){
	            		//通知获取结果
	            		call.setResponse(response);
	            	}else{
	            		System.out.println("调用不存在..");
	            	}
	            }
			}catch (IOException e) {
				key.cancel();
            	try {
					channel.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
			}
            
		}
		
	}
	
	private class Call{
		//用阻塞队列来通知
		ArrayBlockingQueue<CallResponse> queue = new ArrayBlockingQueue<CallResponse>(1);
		
		//调用发起时间
		//long time = System.currentTimeMillis();
		
		public void setResponse(CallResponse response){
			queue.add(response);
		}
		
		public CallResponse getResponse(){
			try {
				return queue.take();
			} catch (InterruptedException e) {
				return null;
			}
		}
		
	}

	
}
