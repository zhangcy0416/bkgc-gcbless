package com.bkgc.bless.utils;

import java.util.HashMap;
import java.util.Map;

public class Reward4Position {

	private Map map ;

	public Reward4Position() {
		Map m = new HashMap<>();
		m.put("5af6fe8-e6bd-11e7-b01a-00163e006a3c", "3,7");//two_bless
		m.put("39789d39-e6bd-11e7-b01a-00163e006a3c", "9");//five_bless
		m.put("3cbf5af2-e6bd-11e7-b01a-00163e006a3c", "1");//ten_bless
		m.put("3ffe624c-e6bd-11e7-b01a-00163e006a3c", "5");//ninety_discount
		m.put("43a7d888-e6bd-11e7-b01a-00163e006a3c", "8");//eighty_discount
		m.put("46e625b2-e6bd-11e7-b01a-00163e006a3c", "4");//portable_source
		m.put("4a100b9f-e6bd-11e7-b01a-00163e006a3c", "6");//luxury
		m.put("4b804b5e-e6bb-11e7-b01a-00163e006a3c", "0");//double_award
		m.put("a7148608-e6bb-11e7-b01a-00163e006a3c", "2");//treble_award
		this.map = m;
	}

    private static Reward4Position reward4Position=null;

    public static Reward4Position getInstance() {
         if (reward4Position == null) {
        	 reward4Position = new Reward4Position();
         }
        return reward4Position;
    }

	public Map getMap() {
		return map;
	}





}
