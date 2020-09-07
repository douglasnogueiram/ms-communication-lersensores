package com.br.ms.communication.buyprocess.service.processar;

import java.io.IOException;

import com.br.ms.communication.buyprocess.service.bank.PagamentoRetorno;
import com.br.ms.communication.buyprocess.service.bank.BankService;
import com.br.ms.communication.buyprocess.gateway.json.CompraChaveJson;
import com.br.ms.communication.buyprocess.gateway.json.CompraFinalizadaJson;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;


import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

@Service
public class ListenerService {

	@Autowired
	private BankService bank;

	//@Autowired
	//private RabbitTemplate rabbitTemplate;
	
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	
	@Autowired
	private Gson gson;

	@Value("${topico.entrada}")
	private String nomeFilaRepublicar;

	@Value("${topico.finalizado}")
	private String nomeFilaFinalizado;

	//@HystrixCommand(fallbackMethod = "republicOnMessage")
	//@RabbitListener(queues="${fila.entrada}")
	//@HystrixProperty(name = "hystrix.command.default.execution.timeout.enabled", value = "false")
	@KafkaListener(topics = "${topico.entrada}", groupId = "group_id")
    public void onMessage(String message) throws JsonSyntaxException, IOException  {
		
		System.out.println("Chegou mensagem na fila de entrada...");
		String json = message;
		
		System.out.println("Mensagem recebida:"+json);
		

		CompraChaveJson compraChaveJson = gson.fromJson(json, CompraChaveJson.class);

		PagamentoRetorno pg = bank.pagar(compraChaveJson);
		

		CompraFinalizadaJson compraFinalizadaJson = new CompraFinalizadaJson();
		compraFinalizadaJson.setCompraChaveJson(compraChaveJson);
		compraFinalizadaJson.setPagamentoOK(pg.isPagamentoOK());
		compraFinalizadaJson.setMensagem(pg.getMensagem());
		
		
		String jsonFinalizado = gson.toJson(compraFinalizadaJson);
		
		/*
		org.codehaus.jackson.map.ObjectMapper obj = new org.codehaus.jackson.map.ObjectMapper();
		String jsonFinalizado = obj.writeValueAsString(compraFinalizadaJson);
		*/

		kafkaTemplate.send(nomeFilaFinalizado, jsonFinalizado);
		System.out.println("Mensagem republicada: " + jsonFinalizado);
    }

	public void republicOnMessage(String message) throws IOException  {
		System.out.println("Republicando mensagem...");
		//rabbitTemplate.convertAndSend(nomeFilaRepublicar, message);
		kafkaTemplate.send(nomeFilaRepublicar, message);
	}
}