package com.bkgc.game.util;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@Slf4j
public class SerializeUtils {

    public static String serialize(Object obj){
        byte[] bytes = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            bytes = baos.toByteArray();
            baos.close();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("序列化异常，异常信息={}", e.getMessage(), e);
        }
        return bytes.toString();
    }

    public static Object deSerialize(byte[] bytes){
        Object obj=null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            obj = ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("反序列化异常，异常信息={}", e.getMessage(), e);
        }
        return obj;
    }
}
