package com.agaoglu.integration.integrationtestexample;

import com.agaoglu.integration.integrationtestexample.config.H2TestProfileJpaConfig;
import com.agaoglu.integration.integrationtestexample.entity.Deneme;
import com.agaoglu.integration.integrationtestexample.repository.DenemeRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { H2TestProfileJpaConfig.class, IntegrationTestExampleApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
/*@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)*/
@EnableKafka
public class IntegrationTestExampleApplicationTests {

	@Autowired
	DenemeRepository denemeRepository;

	@LocalServerPort
	private int port;

	@ClassRule
	public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true, "test");

	private static BlockingQueue<ConsumerRecord<String, String>> consumerRecords;

	private static KafkaMessageListenerContainer<String, String> container;

	@BeforeClass
	public static void setup(){
		//embeddedKafka.before();
		consumerRecords = new LinkedBlockingQueue<>();
		System.out.println("**********************************");

		ContainerProperties containerProperties = new ContainerProperties("test");

		Map<String, Object> consumerProperties = KafkaTestUtils.consumerProps(
				"sender", "false", embeddedKafka.getEmbeddedKafka());

		DefaultKafkaConsumerFactory<String, String> consumer = new DefaultKafkaConsumerFactory<>(consumerProperties);

		container = new KafkaMessageListenerContainer<>(consumer, containerProperties);
		container.setupMessageListener((MessageListener<String, String>) record -> {
			System.out.println("**********************************" + record);
			consumerRecords.add(record);
		});
		container.start();

		ContainerTestUtils.waitForAssignment(container, embeddedKafka.getEmbeddedKafka().getPartitionsPerTopic());

	}

	@Test
	public void contextLoads() throws InterruptedException {
		TestRestTemplate testRestTemplate = new TestRestTemplate();
		HttpHeaders headers = new HttpHeaders();
		HttpEntity entity = new HttpEntity(headers);
		headers.set("Accept", "application/json");
		ResponseEntity<String> a = testRestTemplate.exchange("http://localhost:"+port+"/createEntity?name=test", HttpMethod.GET, entity, String.class);
		System.out.println(a.getBody());
		Deneme deneme = denemeRepository.findById("test").orElse(null);
		System.out.println(deneme.getName());
		ConsumerRecord<String, String> received = consumerRecords.poll(10, TimeUnit.SECONDS);
		System.out.println(received.value());
		Assert.assertNotNull(deneme);
		Assert.assertEquals(deneme.getAge(), 10);
	}

}
