package led.discovery.app;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

public class Standalone {
	public static void main(String[] args) {
		System.out.println("#1: welcome to the led discovery");
		AppCli cli = new AppCli(args);
		cli.parse();
		Server server = new Server();
		ServerConnector connector = new ServerConnector(server);

		connector.setIdleTimeout(1000 * 60 * 60);
		connector.setSoLingerTime(-1);
		connector.setPort(cli.getPort());
		server.setConnectors(new Connector[] { connector });
		System.out.println("#2: findler is starting on port " + cli.getPort());

		// Handlers
		HandlerCollection handlers = new HandlerCollection();

		// Web App
		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath("/");

		String webxmlLocation = Standalone.class.getResource("/WEB-INF/web.xml").toString();
		webapp.setDescriptor(webxmlLocation);

		String resLocation = Standalone.class.getResource("/static").toString();
		webapp.setResourceBase(resLocation);
		webapp.setParentLoaderPriority(true);
//
		// Resources
		for (String resource : new String[] { "css", "img", "vendor", "js" }) {
			ContextHandler capHandler = new ContextHandler();
			capHandler.setContextPath("/" + resource);
			ResourceHandler resHandler = new ResourceHandler();
			resHandler.setBaseResource(Resource.newClassPathResource(resource));
			capHandler.setHandler(resHandler);
			handlers.addHandler(capHandler);
		}
		handlers.addHandler(webapp);
		server.setHandler(handlers);

		System.out.println("#3: resources setup");

		try {
			server.start();
			System.out.println("#4: enjoy");
			server.join();
			System.out.println("#5: stopping server");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
		System.out.println("#6: thank you");
	}
}
