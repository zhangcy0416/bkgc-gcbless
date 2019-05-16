package com.bkgc.game.model.enums;


/**
 * <p>Title:      AwardWeigntEnum </p>
 * <p>Description 奖品权重枚举 </p>
 * <p>Company:    北控国彩 </p>
 *
 * @Author         zhangft
 * @CreateDate     2018/7/12 上午11:43
 */
public enum AwardWeigntEnum {

    TWO_BLESS("5af6fe8-e6bd-11e7-b01a-00163e006a3c",10000),
    FIVE_BLESS("39789d39-e6bd-11e7-b01a-00163e006a3c",5000),
    TEN_BLESS("3cbf5af2-e6bd-11e7-b01a-00163e006a3c",1000),
    DOUBLE_AWARD("4b804b5e-e6bb-11e7-b01a-00163e006a3c",500),
    THREE_AWARD("a7148608-e6bb-11e7-b01a-00163e006a3c",300),
    EIGHT_DISCOUNT("43a7d888-e6bd-11e7-b01a-00163e006a3c",1000),
    NINE_DISCOUNT("3ffe624c-e6bd-11e7-b01a-00163e006a3c",2000),
    PHONE_RECHARGE("46e625b2-e6bd-11e7-b01a-00163e006a3c",50),
    IPHONE_X("4a100b9f-e6bd-11e7-b01a-00163e006a3c",1);

    private String awardId;

    private int value;

    AwardWeigntEnum(String awardId,int value){
        this.awardId = awardId;
        this.value = value;
    }


    public String getAwardId() {
        return awardId;
    }

    public void setAwardId(String awardId) {
        this.awardId = awardId;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public static int getWeightByAwardId(String awardId){
        for (AwardWeigntEnum awardWeigntEnum : AwardWeigntEnum.values()) {
            if(awardWeigntEnum.getAwardId().equals(awardId)){
                return awardWeigntEnum.getValue();
            }
        }

        return 0;
    }

    public static int setWeightByAwardId(String awardId,int weight){
        for (AwardWeigntEnum awardWeigntEnum : AwardWeigntEnum.values()) {
            if(awardWeigntEnum.getAwardId().equals(awardId)){
                awardWeigntEnum.setValue(weight);
                return 1;
            }
        }
        return 0;
    }
}
