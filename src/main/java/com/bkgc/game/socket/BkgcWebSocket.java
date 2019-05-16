//package com.bkgc.game.socket;
//
//import com.bkgc.bean.user.AuthMember;
//import com.bkgc.bless.service.impl.AuthService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//import javax.websocket.*;
//import javax.websocket.server.PathParam;
//import javax.websocket.server.ServerEndpoint;
//import java.io.IOException;
//import java.util.Queue;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * Created by admin on 2017/12/26.
// */
//@ServerEndpoint(value = "/websocket/{guid}")
//@Component
//public class BkgcWebSocket {
//
//    private static Logger log = LoggerFactory.getLogger(BkgcWebSocket.class);
//
//    /**
//     * 统计用户游戏用户在线数量
//     */
//    private static int onlineCount = 0;
//
//    /**
//     * 保存游戏用户的websocket连接对象集合
//     */
//    private static ConcurrentHashMap<String, BkgcWebSocket> websocketMap = new ConcurrentHashMap<String, BkgcWebSocket>();
//
//    /**
//     * 当前用户连接用户的websocket会话对象
//     */
//    private Session session;
//
//    private String guid;
//
//    private String nickName;
//
//    @OnOpen
//    public void onOpen(Session session, @PathParam("guid") String guid) {
//        log.info("打开链接，guid={}", guid);
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
//                String nickName = getNickname();
//                if (nickName != null) {
//                    for (String guidset : guidSet) {
//                        if (guidset.equals(this.guid)) {
//                            Queue<String> queue = StartMessageQueue.getQueue();
//                            websocketMap.get(guid).session.getBasicRemote().sendText("[".concat(nickName).concat("]*我*").concat("加入游戏"));
//                            for (int i = 0; i <= queue.size() - 1; i++) {
//                                String message = queue.poll();
//                                websocketMap.get(guid).session.getBasicRemote().sendText(message);
//                            }
//                            StartMessageQueue.addMessage("[".concat(nickName).concat("]").concat("加入游戏"));
//
//                        } else {
//                            websocketMap.get(guid).session.getBasicRemote().sendText("[".concat(nickName).concat("]").concat("加入游戏"));
//                        }
//                    }
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
//                    websocketMap.get(guid).session.getBasicRemote().sendText("[".concat(nickName).concat("]*我*").concat("离开游戏"));
//                } else {
//                    websocketMap.get(guid).session.getBasicRemote().sendText("[".concat(nickName).concat("]").concat("离开游戏"));
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
//            String nickName = getNickname();
//            if (nickName != null) {
//                for (String guid : guidSet) {
//                    if (guid.equals(this.guid)) {
//                        websocketMap.get(guid).session.getBasicRemote().sendText("[".concat(nickName).concat("]*我*").concat(message));
//                    } else {
//                        websocketMap.get(guid).session.getBasicRemote().sendText("[".concat(nickName).concat("]").concat(message));
//                    }
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    @OnError
//    public void onError(Session session, Throwable error) {
//        log.error("发生错误");
//        error.printStackTrace();
//        websocketMap.remove(this.guid);
//        Set<String> guidSet = websocketMap.keySet();
//        try {
//            for (String guid : guidSet) {
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
//        BkgcWebSocket.onlineCount++;
//    }
//
//    public static int getOnlineCount() {
//        return onlineCount;
//    }
//
//    public static void setOnlineCount(int onlineCount) {
//        BkgcWebSocket.onlineCount = onlineCount;
//    }
//
//
//    public static synchronized void subOnlineCount() {
//        BkgcWebSocket.onlineCount--;
//    }
//
//
//    private String getNickname() {
//        if (this.nickName == null) {
//            AuthMember authMember = AuthService.getStaticAuthMemberMapper().selectByguid(guid);
//            if (authMember == null) {
//                try {
//                    session.getBasicRemote().sendText("[" + guid + "]不是有效的用户！");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (authMember.getNickname().equals(authMember.getPhone())) {
//                this.nickName = authMember.getNickname().substring(0, 3).concat("****").concat(authMember.getNickname().substring(7));
//            } else {
//                this.nickName = authMember.getNickname();
//            }
//        }
//        return this.nickName;
//    }
//}
