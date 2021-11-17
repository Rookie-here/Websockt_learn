package com.Config;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;

//虽然@Component默认是单例模式的，但springboot还是会为每个websocket连接初始化一个bean
@Component
//将类定义成websocket服务端，注解的值用于监听用户连接的url
@ServerEndpoint("/websocket/{username}")
public class WebSocketConfig {
	//静态变量，用来记录当前在线连接数
	private static int onlineCount = 0;
	
	
	//存放每个客户端对应的Session对象，Key可以为用户标识
	private static ConcurrentHashMap<String,Session> webSocketMap = new ConcurrentHashMap<String,Session>();
	
	//连接创建时触发
	@OnOpen
	public void onOpen(@PathParam("username") String username,Session session) {
		WebSocketConfig.webSocketMap.put(username, session);
		addOnlineCount();
		System.out.println(username + "加入>>>>>>当前连接为 " + getOnlineCount());
		WebSocketConfig.sendMessageToAllOnlineUser(username + " 加入聊天");
	}
	
	//在连接断开时触发
	@OnClose
	public void onClose(@PathParam("username") String username) {
		WebSocketConfig.webSocketMap.remove(username);
		subOnlineCount();
		System.out.println(username +"已断开>>>>>>当前链接为 " + getOnlineCount());
	}
	
	//接收到消息时触发
	@OnMessage
	public void onMessage(@PathParam("username")String username,String msg,Session session) {
		WebSocketConfig.sendMessageToAllOnlineUser(username +": "+ msg);
	}
	@OnError
	public void onError(Session session,Throwable error) {
		System.out.println("发生错误");
		error.printStackTrace();
	}
	
    /**
     * 向所有在线用户发送消息(遍历 向每一个用户发送)
     * @param msg
     */
    public static void sendMessageToAllOnlineUser(String msg){
		for(Map.Entry<String,Session> entry:webSocketMap.entrySet()) {
			try {
				sendMessage(entry.getValue(),msg);
			}catch(Exception e) {	
				e.printStackTrace();
				continue;
			}
		}
    }
 
    /**
     * 向指定用户发送信息，每个用户一个session
     */
    private static void sendMessage(Session session,String message) {
    	if(session ==null) {
    		return ;
    	}
    	try {
			session.getBasicRemote().sendText(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
    public static synchronized int getOnlineCount() {
        return WebSocketConfig.onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketConfig.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketConfig.onlineCount--;
    }
}
