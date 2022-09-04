package com.jzx.client.core;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;

import com.jzx.client.constant.Constants;
import com.jzx.client.zk.ZookeeperFactory;

import io.netty.channel.ChannelFuture;

public class ServerWatcher implements CuratorWatcher {

	@Override
	public void process(WatchedEvent event) throws Exception {
		
		CuratorFramework client = ZookeeperFactory.create();
		String path = event.getPath();   //event(宕机后的服务器）的路径为：定义好的Constants.SERVER_Path
		System.out.println("监听到服务器状态变化");
		client.getChildren().usingWatcher(this).forPath(path);
		List<String> serverPaths = client.getChildren().forPath(path);	
		
		ChannelManage.realSeverPath.clear();
		for(String severPath : serverPaths) {
			String[] str = severPath.split("#");
			
			int weight = Integer.valueOf(str[2]);
			if(weight>0) {
				for(int w=0; w<=weight; w++) {
					ChannelManage.realSeverPath.add(str[0]+"#"+str[1]+"#"+str[2]);
				}
			}
		}
		
		ChannelManage.clear();
//		 重新连接并放入连接容器管理
		for(String realServer:ChannelManage.realSeverPath) {
			String[] str = realServer.split("#");
			try {
				int weight = Integer.valueOf(str[2]);
				if(weight>0) {
					for(int w=0; w<=weight; w++) {
						ChannelFuture channelFuture = TcpClient.b.connect(str[0],Integer.valueOf(str[1]));
						ChannelManage.addChannel(channelFuture);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		
	}

}
