package com.imooc.springcloudstream.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;


/*
*
* 第二步：对上述输入通道创建监听与处理逻辑。
* */
@EnableBinding(ExampleBinder.class)
public class ExampleReceiver {

    private static Logger logger = LoggerFactory.getLogger(ExampleReceiver.class);



    /*
    * 消息消费逻辑执行了3次，然后抛出了最终执行失败的异常。
    * 设置重复次数
    *默认情况下Spring Cloud Stream会重试3次，
    * 我们也可以通过配置的方式修改这个默认配置，比如下面的配置可以将重试次数调整为1次：
      spring.cloud.stream.bindings.example-topic-input.consumer.max-attempts=1
     对于一些纯内部计算逻辑，不需要依赖外部环境，如果出错通常是代码逻辑错误的情况下，不
     论我们如何重试都会继续错误的业务逻辑可以将该参数设置为0，避免不必要的重试影响消息处理的速度。
     *
     * 如果在重试过程中消息处理成功了，还会有异常信息吗？
     * 答案是不会。因为重试过程是消息处理的一个整体，如果某一次重试成功了，对所收到消息的消费成功了。
     *
     *
     *  @Slf4j
        @Component
        static class TestListener {

            int counter = 1;

            @StreamListener(TestTopic.INPUT)
            public void receive(String payload) {
                log.info("Received: " + payload + ", " + counter);
                if (counter == 3) {
                    counter = 1;
                    return;
                } else {
                    counter++;
                    throw new RuntimeException("Message consumer failed!");
                }
            }

        }

    *通过加入一个计数器，当重试是第3次的时候，不抛出异常来模拟消费逻辑处理成功了
    * 虽然前两次消费抛出了异常，但是并不影响最终的结果，也不会打印中间过程的异常，避免了对日志告警产生误报等问题。
    * 如果消息在重试了还是失败之后，目前的配置唯一能做的就是将异常信息记录下来，进行告警。
    * 由于日志中有消息的消息信息描述，所以应用维护者可以根据这些信息来做一些补救措施
    *
    *
    * 重试的应用场景
    *
    * */
    @StreamListener(ExampleBinder.NAME)
    public void receive(String payload) {
        logger.info("Received: " + payload);
        //消息消费的时候主动抛出了一个异常来模拟消息的消费失败。
        throw new RuntimeException("抛出异常");
    }


    /**
     * 消息消费失败的降级处理逻辑
     *
     * @param message
     */
    @ServiceActivator(inputChannel = "example-topic.stream-exception-handler.errors")
    public void error(Message<?> message) {
        logger.info("Message consumer failed, call fallback!");
    }


}
