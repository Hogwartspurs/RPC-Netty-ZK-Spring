package com.jzx.client.core;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.ChannelFuture;

//管理ChannelFuture（连接）
public class ChannelManage {

	static CopyOnWriteArrayList<String> realSeverPath = new CopyOnWriteArrayList<String>();
	static AtomicInteger position = new AtomicInteger(0);
	public static CopyOnWriteArrayList<ChannelFuture> channelFutures = new CopyOnWriteArrayList<ChannelFuture>();
	
	public static void removeChannel(ChannelFuture channel) {
		channelFutures.remove(channel);
	}
	
	public static void addChannel(ChannelFuture channel) {
		channelFutures.add(channel);
	}
	
	public static void clear() {
		channelFutures.clear();
	}

	public static ChannelFuture get(AtomicInteger i) {
		int size = channelFutures.size();
		ChannelFuture channel = null;
		if(i.get()>size) {
			channel = channelFutures.get(0);
			position = new AtomicInteger(1);
		}else{
			channel = channelFutures.get(i.getAndIncrement());
		}
		if(!channel.channel().isActive()) {
			channelFutures.remove(channel);
			return get(position);
		}
		
		return channel;
	}
	
}
