package com.bkgc.bless.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bkgc.bean.bless.SitePlayContent;
import com.bkgc.bless.mapper.SitePlayContentMapper;
import com.bkgc.common.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Transactional
@Service
public class SiteService {

    @Autowired
    private SitePlayContentMapper sitePlayContentMapper;


    public JSONObject getPlayContentList() {
        JSONObject data = new JSONObject();
        SitePlayContent content_select = new SitePlayContent();
        content_select.setIsshow(1);
        JSONArray list = new JSONArray();
        List<SitePlayContent> contentList = sitePlayContentMapper.selectByContent(content_select);
        for (SitePlayContent content : contentList) {
            JSONObject d = new JSONObject();
            d.put("Title", content.getTitle());
            d.put("ImageUrl", "https://s20130.8fubao.com" + content.getImageurl());
            int contentType = content.getContenttype();
            String contentId = content.getContentid();
            String link = "";
            if (contentType == 1) {
                link = "http://bless.8fubao.com/bvms/"
                        + "CaptureEnvelopeGroup.action?groupId="
                        + contentId;
            }
            d.put("Link", link);
            d.put("ContentType", contentType);
            d.put("ContentId", contentId);
            list.add(d);
        }
        data.put("List", list);
        return ResultUtil.buildSuccessResult(data);
    }
}
