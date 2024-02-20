package pl.polcodex.kafkademo;

import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
public class KafkaDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaDemoApplication.class, args);
	}

	//bean for creating new topic named "topic1"
	@Bean
	public NewTopic topic1() {
		return new NewTopic("topic1", 1, (short) 1);
	}

}

@Service
//service for sending message to kafka topic
class StringMessageProducer {

	private final KafkaTemplate<String, String> kafkaTemplate;

	public StringMessageProducer(KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	//method for sending message to kafka topic
	//returns string representation of SendResult
	public String sendMessage(String message) throws ExecutionException, InterruptedException {
		CompletableFuture<SendResult<String, String>> topic1 = kafkaTemplate.send("topic1", message);
		return topic1.get().toString();
	}
}

@Service
class StringMessageConsumer {

	private static Logger log = LoggerFactory.getLogger(StringMessageConsumer.class);

	//method for consuming message from kafka topic
	@KafkaListener(topics = "topic1")
	public void consume(String message) {
		log.info("Consumed message: {}", message);
	}
}

//Rest endpoint for sending message to kafka topic
@RestController
@RequestMapping("/kafka")
class RestEndpoint {
	private static Logger log = LoggerFactory.getLogger(RestEndpoint.class);

	private final StringMessageProducer stringMessageProducer;

	public RestEndpoint(StringMessageProducer stringMessageProducer) {
		this.stringMessageProducer = stringMessageProducer;
	}

	//curl -X POST -H "Content-Type: application/json" -d "Hello, World!" http://localhost:8080/kafka
	//Invoke-WebRequest -Method Post -Uri http://localhost:8080/kafka -Body "Hello, World!" -ContentType "application/json"
	@PostMapping
	public String sendMessage(@RequestBody String message) throws ExecutionException, InterruptedException {
		log.info("sending message='{}'", message);
		return stringMessageProducer.sendMessage(message);
	}

}








