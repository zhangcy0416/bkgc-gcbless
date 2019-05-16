package com.bkgc.game.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * Created by gmg on on 2017-12-27 16:43.
 */
@Data
public class LottoAutoawardOrderExpend {

    private String ticketno;
    private Date awardtime;
    private Integer awardmoney;
    private String lottoCode;
    private String lottoName;
    private String orderno;
}
