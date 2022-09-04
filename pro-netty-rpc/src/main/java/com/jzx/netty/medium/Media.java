package com.jzx.netty.medium;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.jzx.netty.handler.param.ServerRequest;
import com.jzx.netty.util.Response;


public class Media {


	public static Map<String, BeanMethod>beanMap;
	static {
		beanMap = new HashMap<String, BeanMethod>();
	}
	
	private static Media m = null;
	private Media() {
		
	}
	public static Media newInstance() {
		if(m==null) {
			m=new Media();
		}
		return m;
	}
	public Response process(ServerRequest request) {
		Response result = null;
		try{
			String command = request.getCommand();
			BeanMethod beanMethod = beanMap.get(command);
			if(beanMethod==null) {
				return null;
			}
			Object bean = beanMethod.getBean();
			Method m = beanMethod.getMethod();
			Class<?> paramType = m.getParameterTypes()[0];
			Object content = request.getContent();
			
			Object args = JSONObject.parseObject(JSONObject.toJSONString(content), paramType);
			
			result = (Response) m.invoke(bean, args);
			result.setId(request.getId());
		}catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}
