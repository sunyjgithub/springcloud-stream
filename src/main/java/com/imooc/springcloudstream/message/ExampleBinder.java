package com.imooc.springcloudstream.message;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;




/*
*
* 第一步：创建绑定接口，绑定example-topic输入通道（默认情况下，会绑定到RabbitMQ的同名Exchange或Kafaka的同名Topic）。
*
*
*
* */

public interface ExampleBinder {

    String NAME = "example-topic";

    @Input(NAME)
    SubscribableChannel input();
}
