package com.imooc.springcloudstream;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;


/*
* 使用@Output创建一个同名的输出绑定，这样发出的消息才能被上述启动的实例接收到
*
*
* 如何解决上述消息重复消费的问题呢？我们只需要在配置文件中增加如下配置即可：

spring.cloud.stream.bindings.example-topic.group=aaa
*1当我们指定了某个绑定所指向的消费组之后，往当前主题发送的消息在每个订阅消费组中，
* 只会有一个订阅者接收和消费，从而实现了对消息的负载均衡。
* 只所以之前会出现重复消费的问题，是由于默认情况下，任何订阅都会产生一个匿名消费组，
* 所以每个订阅实例都会有自己的消费组，从而当有消息发送的时候，就形成了广播的模式。

另外，需要注意上述配置中example-topic是在代码中@Output和@Input中传入的名字。

*
* */
@EnableBinding(value = {ExampleApplicationTests.ExampleBinder.class})
public class ExampleApplicationTests extends SpringcloudStreamApplicationTests {

    @Autowired
    private ExampleBinder exampleBinder;

    @Test
    public void exampleBinderTester() {
        exampleBinder.output().send(MessageBuilder.withPayload("Produce a message from : http://blog.didispace.com").build());
    }

    public interface ExampleBinder {

        String NAME = "example-topic";

        @Output(NAME)
        MessageChannel output();

    }

}
