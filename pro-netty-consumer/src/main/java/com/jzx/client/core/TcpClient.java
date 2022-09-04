package com.jzx.client.core;


import java.util.List;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;

import com.alibaba.fastjson.JSONObject;
import com.jzx.client.constant.Constants;
import com.jzx.client.handler.SimpleClientHandler;
import com.jzx.client.param.ClientRequest;
import com.jzx.client.param.Response;
import com.jzx.client.zk.ZookeeperFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class TcpClient {
	
	static final Bootstrap b = new Bootstrap();
	static ChannelFuture f = null;
	static {
		String host = "localhost";
		int port = 8080;
		EventLoopGroup group = new NioEventLoopGroup();
		
		b.group(group)
		.channel(NioSocketChannel.class)
		.option(ChannelOption.SO_KEEPALIVE, true)
		.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
			ch.pipeline().addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Delimiters.lineDelimiter()[0]));
			ch.pipeline().addLast(new StringDecoder());
			ch.pipeline().addLast(new SimpleClientHandler());
			ch.pipeline().addLast(new StringEncoder());
			}
		});
		
		CuratorFramework client = ZookeeperFactory.create();
		try {
			List<String>severPaths = client.getChildren().forPath(Constants.SERVER_Path);
			
			CuratorWatcher watcher = new ServerWatcher();
			//zk监听服务器的变化(监听路径下的子路径的服务器的状态变化），服务器宕机后调用ServerWatcher的process方法
			client.getChildren().usingWatcher(watcher).forPath(Constants.SERVER_Path);
			for(String severPath : severPaths) {
				String[] str = severPath.split("#");
				int weight = Integer.valueOf(str[2]);
				if(weight>0) {
					for(int w=0; w<=weight; w++) {
						ChannelManage.realSeverPath.add(str[0]+"#"+str[1]+"#"+str[2]);
						
						ChannelFuture channelFuture = b.connect(str[0],Integer.valueOf(str[1]));
						ChannelManage.addChannel(channelFuture);
					}
				}
				
			}
			if(ChannelManage.realSeverPath.size()>0) {
				String[] hostAndport = ChannelManage.realSeverPath.toArray()[0].toString().split("#");    //获取服务器列表第一个
				host = hostAndport[0];
				port = Integer.valueOf(hostAndport[1]);
			}
			f = b.connect(host, port).sync();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
//		try {
//			f = b.connect("localhost", 8080).sync();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}
	
	//实现连接的简单轮询管理
	public static Response send(ClientRequest request) {
		f = ChannelManage.get(ChannelManage.position);
		f.channel().writeAndFlush(JSONObject.toJSONString(request));
		f.channel().writeAndFlush("\r\n");
		DefaultFuture df = new DefaultFuture(request);
		
		return df.get();
	}
}
