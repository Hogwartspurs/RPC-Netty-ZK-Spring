package com.jzx.client;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSONObject;
import com.jzx.client.annotation.RemoteInvoke;
import com.jzx.client.param.Response;
import com.jzx.user.bean.User;
import com.jzx.user.remote.UserRemote;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RemoteInvokingTest.class)
@ComponentScan("com.jzx")
public class RemoteInvokingTest {

	@RemoteInvoke
	private UserRemote userRemote;
	
	@Test
	public void testSaveUser() {
		
		User u = new User();
		u.setId(1);
		u.setName("张三");
		Response response = userRemote.saveUser(u);
		System.out.println(JSONObject.toJSONString(response));
	}
	
	@Test
	public void testSaveUsers() {

		List<User> users = new ArrayList<User>();
		User u = new User();
		u.setId(1);
		u.setName("张三");
		users.add(u);
		userRemote.saveUsers(users);
	}
}
