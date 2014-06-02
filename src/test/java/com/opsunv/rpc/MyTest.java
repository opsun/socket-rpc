package com.opsunv.rpc;

import java.util.ArrayList;
import java.util.List;

public class MyTest {
	public String sayHello(String name){
		return "hello "+name;
	}
	
	public List<String> getList(){
		List<String> list = new ArrayList<String>();
		for(int i=0;i<10;i++){
			list.add("a"+i);
		}
		
		return list;
	}
	
	public void print(String name){
		System.out.println(name);
	}
	
}
