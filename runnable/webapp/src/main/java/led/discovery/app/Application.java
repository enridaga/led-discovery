package led.discovery.app;

import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application extends ResourceConfig implements ServletContextListener {
	private Logger log = LoggerFactory.getLogger(Application.class);
	public final static String VELOCITY = "LED_VELOCITY";
	public final static String TEMPLATES = "LED_TEMPLATES";
	public Application() {
		log.error("Inizialise resources.");
		packages("led.discovery.app.resources"); // .packages("com.wordnik.swagger.jaxrs.json");
	}

	public void contextDestroyed(ServletContextEvent arg0) {
		log.debug("Context destroyed.");
	}

	public void contextInitialized(ServletContextEvent arg0) {
		log.info("Initializing context.");
		ServletContext ctx = arg0.getServletContext();

		// Initialise template engine
		VelocityEngine engine = new VelocityEngine();
		Properties p = new Properties();
//		p.setProperty("resource.loader", "webapp");
//		p.setProperty("webapp.resource.loader.class", "org.apache.velocity.tools.view.WebappResourceLoader");
//		p.setProperty("webapp.resource.loader.path", "/WEB-INF/templates/");
		p.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath"); 
		p.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		engine.setApplicationAttribute("javax.servlet.ServletContext", ctx);
		engine.init(p);
		ctx.setAttribute(VELOCITY, engine);
		ctx.setAttribute(TEMPLATES, "/WEB-INF/templates");
		log.info("Initialized.");
	}
}