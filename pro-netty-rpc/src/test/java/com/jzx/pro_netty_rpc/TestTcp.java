//package com.jzx.pro_netty_rpc;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.junit.Test;
//
//import com.jzx.netty.client.ClientRequest;
//import com.jzx.netty.client.TcpClient;
//import com.jzx.netty.util.Response;
//import com.jzx.user.bean.User;
//
//public class TestTcp {
//
//	@Test
//	public void testGetResponse() {
//		ClientRequest  request = new ClientRequest();
//		request.setContent("测试TCP长连接请求");
//		Response resp = TcpClient.send(request);
//		System.out.println(resp.getResult());
//	}
//	
//	@Test
//	public void testSaveUser() {
//		ClientRequest  request = new ClientRequest();
//		User u = new User();
//		u.setId(1);
//		u.setName("张三");
//		request.setCommand("com.jzx.user.controller.UserController.saveUser");
//		request.setContent(u);
//		Response resp = TcpClient.send(request);
//		System.out.println(resp.getResult());
//	}
//	
//	@Test
//	public void testSaveUsers() {
//		ClientRequest  request = new ClientRequest();
//		List<User> users = new ArrayList<User>();
//		User u = new User();
//		u.setId(1);
//		u.setName("张三");
//		users.add(u);
//		request.setCommand("com.jzx.user.controller.UserController.saveUsers");
//		request.setContent(users);
//		Response resp = TcpClient.send(request);
//		System.out.println(resp.getResult());
//	}
//}
