package led.discovery.app;

import java.io.File;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.velocity.app.VelocityEngine;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application extends ResourceConfig implements ServletContextListener {
	private Logger log = LoggerFactory.getLogger(Application.class);
	public final static String DATA_DIR = "LED_DATA_DIR";
	public final static String CACHE_DIR = "LED_CACHE_DIR";
	public final static String VELOCITY = "LED_VELOCITY";

	public Application() {
		log.info("Inizialise resources.");
		packages("led.discovery.app.resources");
	}

	public void contextDestroyed(ServletContextEvent arg0) {
		log.debug("Context destroyed.");
	}

	public void contextInitialized(ServletContextEvent arg0) {
		log.info("Initializing context.");
		ServletContext ctx = arg0.getServletContext();
		String dataDir = System.getProperty("led.dataDir");
		if (dataDir == null || (!new File(dataDir).exists())) {
			log.error("Invalid or missing led.dataDir: " + dataDir);
			log.error("was: {}", dataDir);
			throw new RuntimeException();
		}
		ctx.setAttribute(DATA_DIR, dataDir);
		String cacheDir = System.getProperty("led.cacheDir");
		if (cacheDir == null) {
			log.error("Invalid or missing system property: led.cacheDir");
			throw new RuntimeException("Missing parameter led.cacheDir");
		}
		log.info("cacheDir: {}", cacheDir);
		try {
			if (!new File(cacheDir).exists()) {
				new File(cacheDir).mkdirs();
			}
		} catch (SecurityException exe) {
			throw exe;
		}
		ctx.setAttribute(CACHE_DIR, cacheDir);

		// Initialise template engine
		VelocityEngine engine = new VelocityEngine();
		Properties p = new Properties();
		p.setProperty("resource.loader", "webapp");
		p.setProperty("webapp.resource.loader.class", "org.apache.velocity.tools.view.WebappResourceLoader");
		p.setProperty("webapp.resource.loader.path", "/WEB-INF/templates/");
//		p.setProperty("resource.loader", "class");
//		p.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
//		p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		p.setProperty("file.resource.loader.cache", "false");
		engine.setApplicationAttribute("javax.servlet.ServletContext", ctx);
		engine.init(p);
		ctx.setAttribute(VELOCITY, engine);
		log.info("Initialized.");
	}
}