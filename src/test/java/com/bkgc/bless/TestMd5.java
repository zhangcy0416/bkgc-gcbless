package com.bkgc.bless;


import com.bkgc.common.utils.signature.MD5;

import java.math.BigDecimal;
import java.util.Random;

/**
 * Created by zhangft on 2018/1/2.
 */
public class TestMd5 {

    /**
     * <p>Title:      将随机码的固定几位用秘钥进行加密 </p>
     * <p>Description  </p>
     *
     * @Author        zhangft
     * @CreateDate    2018/1/2 下午4:37
     */
    public static String encodeRandStr(String randStr){

        if (randStr == null) {
            throw new RuntimeException("randStr参数不能为空");
        }

        if (randStr.length() != 7) {
            throw new RuntimeException("randStr的长度必须为7位");
        }

        //加密 1，3，7的数字，秘钥为 gc123
        String str = randStr.substring(0, 1) + randStr.substring(2, 3) + randStr.substring(6,7) + "gc123";
        System.out.println("加密前字串=" + str);

        String md5Str = MD5.MD5Encode(str);

        return randStr + md5Str.substring(0, 2);
    }

    /**
     * <p>Title:      验证随机码的正确性 </p>
     * <p>Description  </p>
     *
     * @Author        zhangft
     * @CreateDate    2018/1/2 下午4:37
     */
    public static boolean checkRandStr(String randStr){

        if(randStr == null){
            throw new RuntimeException("randStr参数不能为空");
        }

        if (randStr.length() != 9) {
            throw new RuntimeException("randStr的长度必须为9位");
        }

        String str = randStr.substring(0, 1) + randStr.substring(2, 3) + randStr.substring(6, 7) + "gc123";

        String md5Str = MD5.MD5Encode(str);

        String checkStr = randStr.substring(7, 9);

        if (checkStr.equals(md5Str.substring(0, 2))) {
            return true;
        }

        return false;
    }

    public static void main2(String[] args) {
//        String str = "abcdefg";
//        String retStr = TestMd5.encodeRandStr(str);
//
//        System.out.println(retStr);

        String enStr = "9LGmJX97a";


        System.out.println(TestMd5.checkRandStr(enStr));

    }

    public static void main3(String[] args) {
        System.out.println("--------测试continue2-------");

        for (int i = 1; i < 10; i++) {

            System.out.println("i=" + i);
            for (int j = 0; j < 10; j++) {
                try {
                    if (j == 3) continue;

                }catch (Exception e){
                    e.printStackTrace();
                }

                System.out.println("    j==" + j);
            }
        }
    }

    public static void main4(String[] args) {
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            //System.out.println(random.nextInt(10));
            System.out.println(new Random().nextDouble());
        }
    }

    public static void main5(String[] args) {
        BigDecimal a= new BigDecimal("1.11");
        System.out.println(a.toString());
    }

    public static void main(String[] args) {
        String version = "2.4.4";
        int i = version.compareTo("2.4.3");
        System.out.println(i);
    }
}
