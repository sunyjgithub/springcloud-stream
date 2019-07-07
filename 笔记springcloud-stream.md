# Getting Started
Spring Cloud Stream如何消费自己生产的消息？

在放出标准答案前，先放出一个常见的错误姿势和告警信息（以便您可以通过搜索引擎找到这里_）。
以下错误基于Spring Boot 2.0.5、Spring Cloud Finchley SR1。

首先，根据入门示例，为了生产和消费消息，需要定义两个通道：一个输入、一个输出。比如下面这样：

public interface TestTopic {

    String OUTPUT = "example-topic";
    String INPUT = "example-topic";

    @Output(OUTPUT)
    MessageChannel output();

    @Input(INPUT)
    SubscribableChannel input();

}
通过INPUT和OUTPUT使用相同的名称，让生产消息和消费消息指向相同的Topic，从而实现消费自己发出的消息。



接下来，创建一个HTTP接口，并通过上面定义的输出通道触来生产消息，比如：

@Slf4j
@RestController
public class TestController {

    @Autowired
    private TestTopic testTopic;

    @GetMapping("/sendMessage")
    public String messageWithMQ(@RequestParam String message) {
        testTopic.output().send(MessageBuilder.withPayload(message).build());
        return "ok";
    }

}

已经有生产消息的实现，下面来创建对输入通道的监听，以实现消息的消费逻辑。

@Slf4j
@Component
public class TestListener {

    @StreamListener(TestTopic.INPUT)
    public void receive(String payload) {
        log.info("Received: " + payload);
        throw new RuntimeException("BOOM!");
    }

}


最后，在应用主类中，使用@EnableBinding注解来开启它，比如：

@EnableBinding(TestTopic.class)
@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

}
然而在启动的瞬间，你可能收到了下面这样的错误：

Invalid bean definition with name 'example-topic' defined in com.didispace.stream.TestTopic: 
bean definition with this name already exists，
没有启动成功的原因是已经存在了一个名为example-topic的Bean，那么为什么会重复创建这个Bean呢？
--------------------- 

实际上，在F版的Spring Cloud Stream中，当我们使用@Output和@Input注解来定义消息通道时，
都会根据传入的通道名称来创建一个Bean。
而在上面的例子中，我们定义的@Output和@Input名称是相同的，
因为我们系统输入和输出是同一个Topic，这样才能实现对自己生产消息的消费。
--------------------- 


既然这样，我们定义相同的通道名是行不通了，那么我们只能通过定义不同的通道名，
并为这两个通道配置相同的目标Topic来将这一对输入输出指向同一个实际的Topic。
对于上面的错误程序，只需要做如下两处改动：

第一步：修改通道名，使用不同的名字

public interface TestTopic {

    String OUTPUT = "example-topic-output";
    String INPUT = "example-topic-input";

    @Output(OUTPUT)
    MessageChannel output();

    @Input(INPUT)
    SubscribableChannel input();

}
--------------------- 
第二步：在配置文件中，为这两个通道设置相同的Topic名称，比如： 输入输出通道对应的物理目标（exchange或topic名），比如：
spring.cloud.stream.bindings.example-topic-input.destination=aaa-topic
spring.cloud.stream.bindings.example-topic-output.destination=aaa-topic

设置这两个通道指向相同的topic  这样，这两个输入输出通道就会都指向名为aaa-topic的Topic了。


消费自己生产的消息成功了！读者也还可以访问一下应用的/actuator/beans端点，
看看当前Spring上下文中有哪些Bean，应该可以看到有下面Bean，也就是上面分析的两个通道的Bean对象


"example-topic-output": {
    "aliases": [],
    "scope": "singleton",
    "type": "org.springframework.integration.channel.DirectChannel",
    "resource": null,
    "dependencies": []
},
"example-topic-input": {
    "aliases": [],
    "scope": "singleton",
    "type": "org.springframework.integration.channel.DirectChannel",
    "resource": null,
    "dependencies": []
},        
