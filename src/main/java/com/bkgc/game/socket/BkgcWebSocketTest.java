//package com.bkgc.game.socket;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//import javax.websocket.*;
//import javax.websocket.server.PathParam;
//import javax.websocket.server.ServerEndpoint;
//import java.io.IOException;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * Created by admin on 2017/12/26.
// */
//@ServerEndpoint(value = "/websocketTest/{guid}")
//@Component
//public class BkgcWebSocketTest {
//
//    private static Logger log = LoggerFactory.getLogger(BkgcWebSocketTest.class);
//
//    /**
//     * 统计用户游戏用户在线数量
//     */
//    private static int onlineCount = 0;
//
//    /**
//     * 保存游戏用户的websocket连接对象集合
//     */
//    private static ConcurrentHashMap<String, BkgcWebSocketTest> websocketMap = new ConcurrentHashMap<String, BkgcWebSocketTest>();
//
//    /**
//     * 当前用户连接用户的websocket会话对象
//     */
//    private Session session;
//
//    private String guid;
//
//    @OnOpen
//    public void onOpen(Session session, @PathParam("guid") String guid) {
//        this.session = session;
//        this.guid = guid;
//        try {
//            if (StringUtils.isEmpty(guid)) {
//                sendMessage("用户id不能为空");
//                return;
//            }
//            websocketMap.put(guid, this);
//            addOnlineCount();
//            Set<String> guidSet = websocketMap.keySet();
//            try {
//                for (String guidset : guidSet) {
//                    if (guidset.equals(this.guid)) {
//                        websocketMap.get(guidset).session.getBasicRemote().sendText("我加入游戏[onOpen]，当前在线游戏为：" + websocketMap.size() + " 人");
//                    } else {
//                        websocketMap.get(guidset).session.getBasicRemote().sendText("用户:" + guid + "加入游戏[onOpen]，当前在线游戏为：" + websocketMap.size() + " 人");
//                    }
//
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            log.debug("新用户:" + guid + "加入游戏，当前在线游戏为：" + websocketMap.size() + " 人");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @OnClose
//    public void onClose() {
//        websocketMap.remove(this.guid);
//        subOnlineCount();
//        Set<String> guidSet = websocketMap.keySet();
//        try {
//            for (String guid : guidSet) {
//                if (guid.equals(this.guid)) {
//                    websocketMap.get(guid).session.getBasicRemote().sendText("我离开游戏[onClose]！！！");
//                } else {
//                    websocketMap.get(guid).session.getBasicRemote().sendText("用户[" + this.guid + "]离开游戏[onClose]！！！当前在线游戏为：" + websocketMap.size() + " 人");
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        log.debug("用户:" + guid + "退出游戏，当前在线游戏为：" + websocketMap.size() + " 人");
//    }
//
//    @OnMessage
//    public void onMessage(String message, Session session) {
//        log.debug("收到用户[" + guid + "]发来的消息:" + message);
//        Set<String> guidSet = websocketMap.keySet();
//        try {
//            for (String guid : guidSet) {
//                if (guid.equals(this.guid)) {
//                    websocketMap.get(guid).session.getBasicRemote().sendText("我的消息[onMessage]:" + message);
//                } else {
//                    websocketMap.get(guid).session.getBasicRemote().sendText("用户[" + this.guid + "]发来的消息[onMessage]:" + message);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @OnError
//    public void onError(Session session, Throwable error) {
//        log.error("发生错误");
//        error.printStackTrace();
//        websocketMap.remove(this.guid);
//        Set<String> guidSet = websocketMap.keySet();
//        try {
//            for (String guid : guidSet) {
//
//                if (guid.equals(this.guid)) {
//                    websocketMap.get(guid).session.getBasicRemote().sendText("我离开游戏[onError]！！！");
//                } else {
//                    websocketMap.get(guid).session.getBasicRemote().sendText("用户[" + this.guid + "]离开游戏[onError]！！！当前在线游戏为：" + websocketMap.size() + " 人");
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    public void sendMessage(String message) throws IOException {
//        this.session.getBasicRemote().sendText(message);
//    }
//
//    public static synchronized void addOnlineCount() {
//        BkgcWebSocketTest.onlineCount++;
//    }
//
//    public static int getOnlineCount() {
//        return onlineCount;
//    }
//
//    public static void setOnlineCount(int onlineCount) {
//        BkgcWebSocketTest.onlineCount = onlineCount;
//    }
//
//
//    public static synchronized void subOnlineCount() {
//        BkgcWebSocketTest.onlineCount--;
//    }
//}
