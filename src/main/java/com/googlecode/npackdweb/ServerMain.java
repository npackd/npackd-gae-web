package com.googlecode.npackdweb;

import java.net.URI;
import java.net.URL;
import org.eclipse.jetty.cdi.CdiDecoratingListener;
import org.eclipse.jetty.cdi.CdiServletContainerInitializer;
import org.eclipse.jetty.demos.TimeServlet;
import org.eclipse.jetty.demos.TimeSocket;
import org.eclipse.jetty.demos.logging.Logging;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.log.JavaUtilLog;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

public class ServerMain {

    public static void main(String[] args) {
        Logging.config();
        Log.setLog(new JavaUtilLog());
        try {
            new ServerMain().run();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void run() throws Exception {
        Server server = new Server(8080);

        URL webRootLocation = this.getClass().getResource(
                "/static-root/index.html");
        if (webRootLocation == null) {
            throw new IllegalStateException(
                    "Unable to determine webroot URL location");
        }

        URI webRootUri = URI.create(webRootLocation.toURI().toASCIIString().
                replaceFirst("/index.html$", "/"));
        System.err.printf("Web Root URI: %s%n", webRootUri);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.setBaseResource(Resource.newResource(webRootUri));
        context.setWelcomeFiles(new String[]{"index.html"});

        context.getMimeTypes().addMimeMapping("txt", "text/plain;charset=utf-8");

        // Enable Weld + CDI
        context.setInitParameter(
                CdiServletContainerInitializer.CDI_INTEGRATION_ATTRIBUTE,
                CdiDecoratingListener.MODE);
        context.addBean(new ServletContextHandler.Initializer(context,
                new CdiServletContainerInitializer()));
        context.addBean(new ServletContextHandler.Initializer(context,
                new org.jboss.weld.environment.servlet.EnhancedListener()));

        // Add WebSocket endpoints
        WebSocketServerContainerInitializer.configure(context,
                (servletContext, wsContainer) -> wsContainer.addEndpoint(
                        TimeSocket.class));

        // Add Servlet endpoints
        context.addServlet(TimeServlet.class, "/time/");
        context.addServlet(DefaultServlet.class, "/");

        // Add to Server
        server.setHandler(context);

        // Start Server
        server.start();
        server.join();
    }
}
