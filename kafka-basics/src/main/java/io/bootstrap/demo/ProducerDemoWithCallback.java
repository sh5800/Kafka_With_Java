package io.bootstrap.demo;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoWithCallback {

    private static Logger log = LoggerFactory.getLogger(ProducerDemoWithCallback.class.getSimpleName());

    public static void main(String[] args) {
        log.info("***Inside ProducerDemoWithCallback***");

        //create producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG,"400");


//        properties.setProperty(ProducerConfig.PARTITIONER_CLASS_CONFIG,RoundRobinPartitioner.class.getName());
        //create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);


        for(int j = 1; j <=10; j++){
            //send 30 messages
            for(int i = 1; i <= 30; i++){
                //create a Producer Record
                ProducerRecord<String, String> producerRecord = new ProducerRecord<>("demo_java","Hello world " + i);

                //send data
                producer.send(producerRecord, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata metadata, Exception e) {
                        //executes everytime a record is successfully sent or an exception is thrown
                        if(e == null){
                            log.info("Recieved new metadata \n" +
                                    "Topic: " + metadata.topic() + "\n" +
                                    "Partition: " + metadata.partition() + "\n" +
                                    "Offset: " + metadata.offset() + "\n" +
                                    "Timestamp: " + metadata.timestamp());
                        }else{
                            log.error("Error while producing ",e);
                        }
                    }
                });
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }



        //tell the producer to send all the data and block until done
        producer.flush();

        //flush and close the producer
        producer.close();
    }
}
