package marcelo.lopes.de.sousa.lima;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;

@Startup
@Singleton
public class Contexto {

	@Inject
	private CamelContext camelContext;

	@PostConstruct
	private void start() {

		try {

			camelContext.addRoutes(new RouteBuilder() {
				@Override
				public void configure() throws Exception {
					from("quartz2://timer1?cron=0/2+*+*+*+*+?").setBody().simple("I was fired at ${header.fireTime}")
							.to("direct:entrada");
				}

			});

			camelContext.addRoutes(new RouteBuilder() {
				@Override
				public void configure() throws Exception {
					from("direct:entrada").routeId("Rota consumidora").to("stream:out");
				}
			});

		} catch (Exception e) {
			System.out.println("Erro ao adicionar a rota ao contexto !!!");
			e.printStackTrace();
		}

		System.out.println("Iniciando contexto !!!");

		try {
			camelContext.start();
		} catch (Exception e) {
			System.out.println("Erro ao iniciar contexto !!!");
			e.printStackTrace();
		}

		// ProducerTemplate pc = camelContext.createProducerTemplate();
		// pc.sendBody("direct:entrada", "Conteudo da mensagem !!!");
	}

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
