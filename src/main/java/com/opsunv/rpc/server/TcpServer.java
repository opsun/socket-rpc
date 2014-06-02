package com.opsunv.rpc.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.opsunv.rpc.CallInfo;
import com.opsunv.rpc.CallResponse;
import com.opsunv.rpc.Utils;

/**
 * TCP服务端
 * @author opsun
 *
 */
public class TcpServer {
	private int port = 5333;
	
	private Selector selector;
	
	private ServerSocketChannel serverChannel;
	
	private ExecutorService pool;
	
	//线程池大小
	private int maxThread = 50;
	
	private volatile boolean flag = false;
	
	private RpcServerImpl rpcServerImpl;
	
	public TcpServer(RpcServerImpl rpcServerImpl) {
		this.rpcServerImpl = rpcServerImpl;
	}
	
	public void init() throws IOException{
		pool = Executors.newFixedThreadPool(maxThread);
		
		serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		serverChannel.socket().bind(new InetSocketAddress(port));
		
		//获取通道管理器
		selector = Selector.open();
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		
		flag = true;
		new Master().start();
		System.out.println("服务端Socket启动");
	}
	
	/**
	 * 关闭处理线程池和socket
	 */
	public void destory(){
		flag = false;
		pool.shutdown();
		
		try {
			selector.close();
			serverChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private class Master extends Thread{
		@Override
		public void run() {
			try {
				while(flag){
					selector.select();
					Iterator<SelectionKey> it = selector.selectedKeys().iterator();
					while(it.hasNext()){
						SelectionKey key = it.next();
						it.remove();
						
		                if(key.isAcceptable()){
		                    ServerSocketChannel server = (ServerSocketChannel)key.channel();
		                    //获得客户端连接通道
		                    SocketChannel channel = server.accept();
		                    channel.configureBlocking(false);
		                    //在与客户端连接成功后，为客户端通道注册SelectionKey.OP_READ事件。
		                    channel.register(selector, SelectionKey.OP_READ);
		                    System.out.println("客户端连接.");
		                }else if(key.isReadable()){
		                	proccess(key);
		                }
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private void proccess(SelectionKey key){
			SocketChannel channel = (SocketChannel)key.channel();
			try{
				//定义每个read开始的前4字节是一个请求数据区的长度
	            ByteBuffer buffer = ByteBuffer.allocate(4);
	            
            	int read = channel.read(buffer);
            	if(read<1){
            		//没有数据,继续监听OP_READ
            		key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                    key.selector().wakeup();
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
	            if(obj instanceof CallInfo){
	            	//放入线程池中处理
	            	pool.execute(new Worker(channel,(CallInfo)obj));
	            }
	           
			}catch (IOException e) {
				key.cancel();
            	try {
					channel.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * 消息处理器
	 * @author opsun
	 *
	 */
	private class Worker implements Runnable{
		private SocketChannel channel;
		
		private CallInfo callInfo;
		
		/**
		 * @param channel
		 * @param callInfo
		 */
		public Worker(SocketChannel channel, CallInfo callInfo) {
			super();
			this.channel = channel;
			this.callInfo = callInfo;
		}

		@Override
		public void run() {
			CallResponse response = rpcServerImpl.invoke(callInfo);
        	byte[] rs = Utils.pack(response);
        	
        	//把数据写回客户端完成本次调用
        	try {
				channel.write(ByteBuffer.wrap(rs));
			} catch (IOException e) {
				e.printStackTrace();
			}
           
		}
		
	}
}
