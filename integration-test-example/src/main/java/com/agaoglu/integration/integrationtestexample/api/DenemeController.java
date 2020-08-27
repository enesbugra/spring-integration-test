package com.agaoglu.integration.integrationtestexample.api;

import com.agaoglu.integration.integrationtestexample.entity.Deneme;
import com.agaoglu.integration.integrationtestexample.producer.KafkaMessageProducer;
import com.agaoglu.integration.integrationtestexample.repository.DenemeRepository;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;

@RestController
public class DenemeController {

    @Autowired
    private DenemeRepository denemeRepository;

    @Autowired
    private KafkaMessageProducer kafkaMessageProducer;

    @GetMapping("createEntity")
    public ResponseEntity<String> createEntity(@PathParam("name") String name){
        Deneme deneme = new Deneme();
        deneme.setAge(10);
        deneme.setName(name);
        denemeRepository.save(deneme);
        kafkaMessageProducer.sendMessage("asd");
        return ResponseEntity.ok().body("ok");
    }
}
