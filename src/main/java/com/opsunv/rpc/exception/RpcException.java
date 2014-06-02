package com.opsunv.rpc.exception;

public class RpcException extends Exception{
	private static final long serialVersionUID = 1L;
	
	public RpcException(String msg) {
		super(msg);
	}
}
