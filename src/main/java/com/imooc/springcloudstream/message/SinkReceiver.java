package com.imooc.springcloudstream.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;


@EnableBinding(Sink.class)
public class SinkReceiver {

    private static Logger logger = LoggerFactory.getLogger(SinkReceiver.class);

    /*
    * @StreamListener：该注解主要定义在方法上，作用是将被修饰的方法注册为消息中间件上数据流的事件监听器，
    * 注解中的属性值对应了监听的消息通道名
    *
    * 我们通过@StreamListener(Sink.INPUT)注解将receive方法注册为对input消息通道的监听处理器
    *
    *
    *
    *
    * */
    @StreamListener(Sink.INPUT)
    public void receive(Object payload) {
        logger.info("Received: " + payload);
    }
}
