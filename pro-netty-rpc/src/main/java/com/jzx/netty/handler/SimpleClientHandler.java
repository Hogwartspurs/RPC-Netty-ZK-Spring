package com.jzx.netty.handler;

import com.alibaba.fastjson.JSONObject;
import com.jzx.netty.client.DefaultFuture;
import com.jzx.netty.util.Response;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class SimpleClientHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if("ping".equals(msg.toString())) {
			ctx.channel().writeAndFlush("ping\r\n");
			return;
		}
//		ctx.channel().attr(AttributeKey.valueOf("xxxxx")).set(msg);
		Response response = JSONObject.parseObject(msg.toString(), Response.class);
		DefaultFuture.receive(response);
//		ctx.channel().close();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

	}

	

}
