package com.jzx.client.core;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.jzx.client.param.ClientRequest;
import com.jzx.client.param.Response;

public class DefaultFuture {

	public final static ConcurrentHashMap<Long, DefaultFuture> allDefaultFuture = new ConcurrentHashMap<Long, DefaultFuture>();
	
	public Lock lock = new ReentrantLock();
	public Condition condition = lock.newCondition();
	private Response response;
	private long timeout = 2*60*1000l;
	private long startTime = System.currentTimeMillis();
	
	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public long getStartTime() {
		return startTime;
	}

	public DefaultFuture(ClientRequest request) {
		allDefaultFuture.put(request.getId(), this);
	}

	public Response get() {
		lock.lock();
		try {                   //一直等待响应，才解锁
			while(!done()) {  //response 响应为空进入循环
				condition.await();			
			}
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			lock.unlock();
		}
		return this.response;
	}
	public Response get(long time) {
		lock.lock();
		try {
			while(!done()) { //response 响应为空进入循环
				condition.await(time, TimeUnit.SECONDS);
				if (System.currentTimeMillis()-startTime>time) {
					System.out.println("请求超时了！！");
					break;
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			lock.unlock();
		}
		return this.response;
	}

	public static void receive(Response response) {
		DefaultFuture df = allDefaultFuture.get(response.getId());
		if(df!=null) {
			Lock lock = df.lock;
			lock.lock();
			try{
				df.setResponse(response);
				df.condition.signal();
				allDefaultFuture.remove(df);
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				lock.unlock();
			}
		}
	}
	
	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

	private boolean done() {
		if(this.response!=null) {
			return true;
		}
		return false;
	}
	
	static class FutureThread extends Thread{

		@Override
		public void run() {
			Set<Long>ids = allDefaultFuture.keySet();
			for(Long id:ids) {
				DefaultFuture df = allDefaultFuture.get(id);
				if(df==null) {
					allDefaultFuture.remove(df);
				}else {
					
					//若链路超时
					if(df.getTimeout()<System.currentTimeMillis()-df.getStartTime()) {
						Response resp = new Response();
						resp.setId(id);
						resp.setMsg("请求链路超时");
						resp.setCode("333333");
						receive(resp);
					}
				}
			}
			
		}
		
	}

	static {
		FutureThread futureThread = new FutureThread();
		futureThread.setDaemon(true);
		futureThread.start();
	}
}
