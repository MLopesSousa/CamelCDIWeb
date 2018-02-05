package marcelo.lopes.de.sousa.lima;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

@Startup
@Singleton
public class Contexto {

	@Inject
	private CamelContext camelContext;

	@Inject
	private BancoMarcelo banco;

	@PostConstruct
	private void start() {

		try {
			
			/**
				A cada 5 segundos chame o bean: 'Bean 01' e o resultado jogue na fila: 'direct:entrada'
			**/	
			camelContext.addRoutes(new RouteBuilder() {
				@Override
				public void configure() throws Exception {
					from("quartz2://timer1?cron=0/5+*+*+*+*+?")
					.routeId("Rota produtora")
					.bean(banco, "doSomething")
					.id("Bean 01")
					.to("direct:entrada").id("direct:entrada");
				}

			});

			
			/** 
			 	Consuma da fila: 'direct:entrada', passe a mensagem para um processor: 'Processo 01' e jogue no sysout!
			**/
			camelContext.addRoutes(new RouteBuilder() {
				@Override
				public void configure() throws Exception {
					from("direct:entrada").routeId("Rota consumidora")
					.process(new Processor() {
						@Override
						public void process(Exchange ex) throws Exception {
							ex.getIn()
							.setBody(ex.getIn().getBody().toString().concat(" + Alterado no processor !!!!"));
						}
					})
					.id("Processo 01")
					.to("stream:out").id("stream:out");
				}
			});

		} catch (Exception e) {
			System.out.println("Erro ao adicionar a rota ao contexto !!!");
			e.printStackTrace();
		}

		
		
		
		/** 
		  	Iniciando o contexto
		**/
		try {
			System.out.println("Iniciando contexto !!!");
			camelContext.start();
		} catch (Exception e) {
			System.out.println("Erro ao iniciar contexto !!!");
			e.printStackTrace();
		}

	}


	/**
	 		Destruindo o contexto
	**/
	@PreDestroy
	private void stop() {
		System.out.println("Destruindo contexto !!!");
		try {
			camelContext.getShutdownStrategy().setTimeout(60);
			camelContext.stop();
		} catch (Exception e) {
			System.out.println("Erro ao fechar o contexto !!!");
			e.printStackTrace();
		}
	}
}
