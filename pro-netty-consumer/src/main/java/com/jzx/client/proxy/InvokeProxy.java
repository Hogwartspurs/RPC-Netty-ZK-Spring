package com.jzx.client.proxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Component;

import com.jzx.client.annotation.RemoteInvoke;
import com.jzx.client.core.TcpClient;
import com.jzx.client.param.ClientRequest;
import com.jzx.client.param.Response;

@Component
public class InvokeProxy implements BeanPostProcessor{

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		
		Field[] fields = bean.getClass().getDeclaredFields();
		for(Field field : fields) {
			if(field.isAnnotationPresent(RemoteInvoke.class)){
				field.setAccessible(true);
				final Map<Method, Class>methodClassMap = new HashMap<Method,Class>();
				putMethodClass(methodClassMap, field);
				Enhancer enhancer = new Enhancer();
				enhancer.setInterfaces(new Class[]{field.getType()});
				enhancer.setCallback(new MethodInterceptor() {
					@Override
					public Object intercept(Object instance, Method method, Object[] args, MethodProxy proxy) throws Throwable {
						//采用netty客户端，去调用服务器
						
						ClientRequest request = new ClientRequest();
						request.setCommand(methodClassMap.get(method).getName()+"."+method.getName());
						request.setContent(args[0]);
						Response resp = TcpClient.send(request);
						
						return resp;
					}
				});
								
				try {
					field.set(bean, enhancer.create());
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		
		return bean;
	}

	
	// 将属性的所有方法和属性的接口类型放入到methodClassMap中
	private void putMethodClass(Map<Method, Class> methodClassMap, Field field) {
		Method[] methods = field.getType().getDeclaredMethods();
		for (Method m:methods) {
			methodClassMap.put(m, field.getType());
		}
		
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
