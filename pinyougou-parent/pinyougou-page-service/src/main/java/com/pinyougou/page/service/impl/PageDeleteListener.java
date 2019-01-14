package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;


@Component
public class PageDeleteListener implements MessageListener {

    @Autowired
    private Destination topicPageDeleteDestination;

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage=(ObjectMessage)message;
        try {
            Long[] goodsIds = (Long[]) objectMessage.getObject();
            itemPageService.deleteItemHtml(goodsIds);

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
