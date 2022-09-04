package com.jzx.user.remote;

import java.util.List;

import com.jzx.client.param.Response;
import com.jzx.user.bean.User;

public interface UserRemote {

	public Response saveUser(User user);
	
	public Response saveUsers(List<User> users);
}
