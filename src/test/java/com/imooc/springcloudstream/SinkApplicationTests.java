package com.imooc.springcloudstream;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;


/*
*
* 通过@Output(SinkSender.OUTPUT)定义了一个输出通过，而该输出通道的名称为input，
* 与前文中的Sink中定义的消费通道同名，
* 所以这里的单元测试与前文的消费者程序组成了一对生产者与消费者。
*
*
*
* pring Cloud Stream构建的应用程序与消息中间件之间是通过绑定器Binder相关联的，
* 绑定器对于应用程序而言起到了隔离作用，它使得不同消息中间件的实现细节对应用程序来说是透明的。
*
* 在没有绑定器这个概念的情况下，我们的Spring Boot应用要直接与消息中间件进行信息交互的时候，由于各消息中间件构建的初衷不同，
* 它们的实现细节上会有较大的差异性，这使得我们实现的消息交互逻辑就会非常笨重，因为对具体的中间件实现细节有太重的依赖，
* 当中间件有较大的变动升级、或是更换中间件的时候，我们就需要付出非常大的代价来实施。
*
*
* 通过定义绑定器作为中间层，完美地实现了应用程序与消息中间件细节之间的隔离。
* 通过向应用程序暴露统一的Channel通道，使得应用程序不需要再考虑各种不同的消息中间件实现。
* 当我们需要升级消息中间件，或是更换其他消息中间件产品时，我们要做的就是更换它们对应的Binder绑定器而不需要修改任何Spring Boot的应用逻辑
*
* 目前版本的Spring Cloud Stream为主流的消息中间件产品RabbitMQ和Kafka提供了默认的Binder实现
*
*
* 在快速入门的示例中，我们通过RabbitMQ的Channel进行发布消息给我们编写的应用程序消费，
* 而实际上Spring Cloud Stream应用启动的时候，在RabbitMQ的Exchange中也创建了一个名为input的Exchange交换器，
* 由于Binder的隔离作用，应用程序并无法感知它的存在，应用程序只知道自己指向Binder的输入或是输出通道
*
* 我们启动的两个应用程序分别是“订阅者-1”和“订阅者-2”，他们都建立了一条输入通道绑定到同一个Topic（RabbitMQ的Exchange）上。
* 当该Topic中有消息发布进来后，连接到该Topic上的所有订阅者可以收到该消息并根据自身的需求进行消费操作。
*
*
* 相对于点对点队列实现的消息通信来说，Spring Cloud Stream采用的发布-订阅模式可以有效的降低消息生产者与消费者之间的耦合，
* 当我们需要对同一类消息增加一种处理方式时，
* 只需要增加一个应用程序并将输入通道绑定到既有的Topic中就可以实现功能的扩展，而不需要改变原来已经实现的任何内容。
*
*
*
* 在现实的微服务架构中，我们每一个微服务应用为了实现高可用和负载均衡，实际上都会部署多个实例。
* 很多情况下，消息生产者发送消息给某个具体微服务时，只希望被消费一次，按照上面我们启动两个应用的例子，
* 虽然它们同属一个应用，但是这个消息出现了被重复消费两次的情况。为了解决这个问题，在Spring Cloud Stream中提供了消费组的概念。
*
* 如果在同一个主题上的应用需要启动多个实例的时候，我们可以通过spring.cloud.stream.bindings.input.group属性为应用指定一个组名，
* 这样这个应用的多个实例在接收到消息的时候，只会有一个成员真正的收到消息并进行处理
*
* 默认情况下，当我们没有为应用指定消费组的时候，Spring Cloud Stream会为其分配一个独立的匿名消费组。
* 所以，如果同一主题下所有的应用都没有指定消费组的时候，当有消息被发布之后，所有的应用都会对其进行消费，因为它们各自都属于一个独立的组中。
* 大部分情况下，我们在创建Spring Cloud Stream应用的时候，建议最好为其指定一个消费组，以防止对消息的重复处理，除非该行为需要这样做
*
*
* 将消息通道进行分组 spring.cloud.stream.bindings.input.group  将绑定到input通道的应用放在xx组
* 通过引入消费组的概念，我们已经能够在多实例的情况下，保障每个消息只被组内一个实例进行消费
* 我们可以观察到，消费组并无法控制消息具体被哪个实例消费
* 也就是说，对于同一条消息，它多次到达之后可能是由不同的实例进行消费的
*
* 也就是说，对于同一条消息，它多次到达之后可能是由不同的实例进行消费的
* 但是对于一些业务场景，就需要对于一些具有相同特征的消息每次都可以被同一个消费实例处理
*
*
* 消息分区的概念：
*
* 比如：一些用于监控服务，为了统计某段时间内消息生产者发送的报告内容，监控服务需要在自身内容聚合这些数据，
* 那么消息生产者可以为消息增加一个固有的特征ID来进行分区，使得拥有这些ID的消息每次都能被发送到一个特定的实例上实现累计统计的效果，
* 否则这些数据就会分散到各个不同的节点导致监控结果不一致的情况
*
*
*而分区概念的引入就是为了解决这样的问题：
* 当生产者将消息数据发送给多个消费者实例时，保证拥有共同特征的消息数据始终是由同一个消费者实例接收和处理。
*
* Spring Cloud Stream为分区提供了通用的抽象实现，用来在消息中间件的上层实现分区处理，
* 所以它对于消息中间件自身是否实现了消息分区并不关心，
* 这使得Spring Cloud Stream为不具备分区功能的消息中间件也增加了分区功能扩展。
*
*
*
* 那么什么是消费组呢？为什么要用消费组？它解决什么问题呢？摘录一段之前博文的内容，来解答这些疑问：

  通常在生产环境，我们的每个服务都不会以单节点的方式运行在生产环境，
  * 当同一个服务启动多个实例的时候，这些实例都会绑定到同一个消息通道的目标主题（Topic）上。
  * 默认情况下，当生产者发出一条消息到绑定通道上，这条消息会产生多个副本被每个消费者实例接收和处理（出现上述重复消费问题）。
  * 但是有些业务场景之下，我们希望生产者产生的消息只被其中一个实例消费，这个时候我们需要为这些消费者设置消费组来实现这样的功能。

*
*
*
*
*


 *
* */
@EnableBinding(value = {SinkApplicationTests.SinkSender.class})
public class SinkApplicationTests extends SpringcloudStreamApplicationTests {


    @Autowired
    private SinkSender sinkSender;

    @Test
    public void sinkSenderTester() {
        sinkSender.output().send(MessageBuilder.withPayload("produce a message ：http://blog.didispace.com").build());
    }

    public interface SinkSender {

        String OUTPUT = "input";

        @Output(SinkSender.OUTPUT)
        MessageChannel output();

    }
}
