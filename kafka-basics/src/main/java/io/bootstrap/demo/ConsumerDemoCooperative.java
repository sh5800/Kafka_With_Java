package io.bootstrap.demo;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerDemoCooperative {

    private static Logger log = LoggerFactory.getLogger(ConsumerDemoCooperative.class.getSimpleName());

    public static void main(String[] args) {
        log.info("***Inside ConsumerDemoCooperative***");

        String bootstrapServer = "localhost:9092";
        String groupId = "my-java-application";
        String topic = "demo_java";

        //create consumer properties
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServer);


        //create consumer configs
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG,groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        properties.setProperty(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,CooperativeStickyAssignor.class.getName());

        Consumer<String, String> consumer = new KafkaConsumer<>(properties);

        // get a reference to the main thread
        final Thread mainThread = Thread.currentThread();

        // adding the shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public  void run(){
                log.info("Detected a shutdown, let's exit by calling consumer.wakeup().....");
                consumer.wakeup();

                // join the main thread to allow the execution of the code in the main thread
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });


        try{
            //subscribe to a topic
            consumer.subscribe(Arrays.asList(topic));

            //poll for data
            while(true){
                log.info("polling");

                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                for(ConsumerRecord<String, String> record : records){
                    log.info("Key: " + record.key() + ", Value: " + record.value());
                    log.info("Partition: " + record.partition() + ", Offset: " + record.offset());
                }
            }
        }catch (WakeupException e){
            log.info("Consumer is starting to shut down ");
        }catch (Exception e){
            log.error("Unexpected exception in the consumer ", e);
        }finally {
            consumer.close(); //close the consumer, this will also commit the offset
            log.info("The consumer is gracefully shutdown");
        }


    }
}
