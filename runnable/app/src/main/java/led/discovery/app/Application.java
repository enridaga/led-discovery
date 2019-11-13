package led.discovery.app;

import java.io.File;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.stanbol.commons.jobs.api.JobManager;
import org.apache.stanbol.commons.jobs.impl.JobManagerImpl;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.app.model.FileCache;

public class Application extends ResourceConfig implements ServletContextListener {
	private Logger log = LoggerFactory.getLogger(Application.class);
	public final static String DATA_DIR = "LED_DATA_DIR";
	public final static String CACHE_DIR = "LED_CACHE_DIR";
	public final static String USER_INPUT_ENABLED = "LED_USER_INPUT_ENABLED";
	public final static String CACHE = "LED_CACHE";
	public final static String VELOCITY = "LED_VELOCITY";
	public static final String TEMPLATES = "LED_TEMPLATE";
	public static final String METHOD = "LED_METHOD";
	public static final String JOB_MANAGER = "LED_JOB_MANAGER";

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
		//
		String dataDir = System.getProperty("led.dataDir");
		if (dataDir == null || (!new File(dataDir).exists())) {
			log.error("Invalid or missing led.dataDir: " + dataDir);
			log.error("was: {}", dataDir);
			throw new RuntimeException();
		}
		ctx.setAttribute(DATA_DIR, dataDir);
		//
		String cacheDir = System.getProperty("led.cacheDir");
		if (cacheDir == null) {
			log.error("Invalid or missing system property: led.cacheDir");
			throw new RuntimeException("Invalid or missing parameter led.cacheDir");
		}
		String method = System.getProperty("led.method");
		if (method == null) {
			log.error("Invalid or missing system property: led.method (Should be the name of the properties file in the data dir)");
			log.error("was: {}", method);
			throw new RuntimeException("Invalid or missing parameter led.method");
		}
		log.info("cacheDir: {}", cacheDir);
		try {
			if (!new File(cacheDir).exists()) {
				new File(cacheDir).mkdirs();
			}
		} catch (SecurityException exe) {
			throw exe;
		}
		//
		boolean userInputEnabled = Boolean.valueOf(System.getProperty("led.userInputEnabled", "True"));
		//
		ctx.setAttribute(USER_INPUT_ENABLED, userInputEnabled);
		ctx.setAttribute(DATA_DIR, dataDir);
		//
		ctx.setAttribute(METHOD, method);
		ctx.setAttribute(CACHE_DIR, cacheDir);
		ctx.setAttribute(CACHE, new FileCache(cacheDir));
		
		// Initialise template engine
		VelocityEngine engine = new VelocityEngine();
		Properties p = new Properties();
//		p.setProperty("resource.loader", "webapp");
//		p.setProperty("webapp.resource.loader.class", "org.apache.velocity.tools.view.WebappResourceLoader");
//		p.setProperty("webapp.resource.loader.path", "/WEB-INF/templates/");
		p.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath"); 
		p.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		if(!log.isDebugEnabled()) {
			p.setProperty("file.resource.loader.cache", "true");
		}else {
			p.setProperty("file.resource.loader.cache", "false");
		}
		engine.setApplicationAttribute("javax.servlet.ServletContext", ctx);
		engine.init(p);
		
		JobManager jobManager = new JobManagerImpl();
		ctx.setAttribute(VELOCITY, engine);
		ctx.setAttribute(JOB_MANAGER, jobManager);
		ctx.setAttribute(TEMPLATES, "/WEB-INF/templates");
		log.info("Initialized.");
	}
}