package com.feng.springboot.websocket.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.feng.springboot.websocket.common.Constants;
import com.feng.springboot.websocket.common.DateTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * WebSocket具体处理细节
 * @author Administrator
 *
 */
public class SystemWebSocketHandler implements WebSocketHandler {
	private static final Logger logger;

    private static final ArrayList<WebSocketSession> users;

    static {
        users = new ArrayList<>();
        logger = LoggerFactory.getLogger(SystemWebSocketHandler.class);
    }

    /**
     * 断开连接之后
     */
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus arg1)
			throws Exception {
		System.err.println("websocket connection closed......");
		logger.debug("websocket connection closed......");
        users.remove(session);
	}

	/**
	 * 连接成功之后
	 */
	@Override
	public void afterConnectionEstablished(WebSocketSession session)
			throws Exception {
		String userName = (String) session.getAttributes().get(Constants.WEBSOCKET_USERNAME);
		logger.debug(session.getAttributes().toString());
		logger.debug(userName + " connect to the websocket success......");
		System.err.println(userName + " connect to the websocket success......");
        users.add(session);
        if(userName!= null){
            //查询未读消息
//            int count = webSocketService.getUnReadNews((String) session.getAttributes().get(Constants.WEBSOCKET_USERNAME));
        	sendMessageToUsers(session,new TextMessage("欢迎，"+userName + " 进入房间 "),true);
//            session.sendMessage(new TextMessage("共有<b style=\"color:blue\">"+users.size() + "</b>个用户在线"));
        }
	}

	/**
	 * 消息处理
	 */
	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message)
			throws Exception {
		sendMessageToUsers(session,message,false);
	}

	/**
	 * 消息传输异常处理
	 */
	@Override
	public void handleTransportError(WebSocketSession session, Throwable arg1)
			throws Exception {
		if(session.isOpen()){
            session.close();
        }
        logger.error("websocket connection  error and closed......",arg1);
        users.remove(session);
	}

	/**
	 * 支持部分消息处理
	 */
	@Override
	public boolean supportsPartialMessages() {
		return false;
	}

	/**
     * 给所有在线用户发送消息
     * @param message
     */
    public void sendMessageToUsers(WebSocketMessage<?> message) {
        for (WebSocketSession user : users) {
            try {
                if (user.isOpen()) {
                    user.sendMessage(message);
                }
            } catch (IOException e) {
                logger.error("send message to "+user.getAttributes().get(Constants.WEBSOCKET_USERNAME)+" error!",e);
            }
        }
    }

    /**
     * 给所有在线用户发送消息
     * @param session
     * @param message
     * @param isJoin 是否为首次进入聊天室
     */
    public void sendMessageToUsers(WebSocketSession session,WebSocketMessage<?> message,boolean isJoin) {
    	String userName = (String) session.getAttributes().get(Constants.WEBSOCKET_USERNAME);
    	for (WebSocketSession user : users) {
        	try {
                if (user.isOpen()) {
//                	user.sendMessage(new TextMessage("用户'"+userName+"'说："));
//                    user.sendMessage(message);

                	/*消息内容*/
                	String msgPayload = message.getPayload().toString();

                	boolean isMe = false;
                	String toName = "",fromName="";
                	String toUserName = (String) user.getAttributes().get(Constants.WEBSOCKET_USERNAME);
                	String msgContent = msgPayload+"<br/>&nbsp;&nbsp;"+DateTools.date2Str((new Date()), DateTools.FORMAT_DATE_TIME);
                	if(!userName.equals(toUserName)) {	//如果不是同一个人发言
                		msgContent = "<b style='color:red'>"+userName+"</b>："+msgContent;
                		toName = toUserName;
                		fromName = userName;
                	}
                	else {
                		msgContent = "<b style='color:red'>我</b>："+msgContent;
                		isMe = true;
                		toName = userName;
                		fromName = userName;
                	}

                	Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                	Map<String,String> info = new LinkedHashMap<String,String>();
                	info.put("userCount", users.size()+"");
                	info.put("msg", msgPayload);
                	info.put("date", DateTools.date2Str((new Date()), DateTools.FORMAT_DATE_TIME));
//                	info.put("isMe", (isMe) ? "1" : "0");
                	info.put("toName", toName);		/*消息接受者*/
                	info.put("fromName", fromName);	/*消息发送者*/
                	info.put("isJoin", (isJoin)? "1" : "0");
                	System.out.println(gson.toJson(info));
//                	user.sendMessage(new TextMessage(msgContent));
                	user.sendMessage(new TextMessage(gson.toJson(info)));
                }
            } catch (IOException e) {
                logger.error("send message to "+user.getAttributes().get(Constants.WEBSOCKET_USERNAME)+" error!",e);
            }
        }
    }

    /**
     * 给某个用户发送消息
     * @param userName
     * @param message
     */
    public void sendMessageToUser(String userName, TextMessage message) {
        for (WebSocketSession user : users) {
            if (user.getAttributes().get(Constants.WEBSOCKET_USERNAME).equals(userName)) {
                try {
                    if (user.isOpen()) {
                        user.sendMessage(message);
                    }
                } catch (IOException e) {
                	logger.error("send message to "+user.getAttributes().get(Constants.WEBSOCKET_USERNAME)+" error!",e);
                }
                break;
            }
        }
    }
}
