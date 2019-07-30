package com.prof.ruan.gateway;

import java.net.URL;

import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class HttpsConfiguration {
	@Value("${server.port}")
    private int httpPort;
	@Value("${https.port}")
    private int httpsPort;
	@Value("${https.keystore-file}")
    private String keystoreFile;
	@Value("${https.keystore-password}")
    private String keystorePassword;
	@Value("${https.keystore-type}")
    private String keystoreType;
	@Bean
    public EmbeddedServletContainerCustomizer servletContainerCustomizer() {
        return new EmbeddedServletContainerCustomizer() {

            @Override
            public void customize(ConfigurableEmbeddedServletContainer container) {
                if (container instanceof JettyEmbeddedServletContainerFactory) {
                    customizeJetty((JettyEmbeddedServletContainerFactory) container);
                }
            }

            private void customizeJetty(JettyEmbeddedServletContainerFactory container) {

                container.addServerCustomizers((Server server) -> {
                    // HTTP
                    ServerConnector connector = new ServerConnector(server);
                    connector.setPort(httpPort);

                    // HTTPS
                    SslContextFactory sslContextFactory = new SslContextFactory();
                    URL urlKeystore = getClass().getClassLoader().getResource(keystoreFile);
                    sslContextFactory.setKeyStoreResource(Resource.newResource(urlKeystore));
                    sslContextFactory.setKeyStorePassword(keystorePassword);
                    sslContextFactory.setKeyStoreType(keystoreType);

                    HttpConfiguration config = new HttpConfiguration();
                    config.setSecureScheme(HttpScheme.HTTPS.asString());
                    config.addCustomizer(new SecureRequestCustomizer());

                    ServerConnector sslConnector = new ServerConnector(
                            server,
                            new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                            new HttpConnectionFactory(config));
                    sslConnector.setPort(httpsPort);

                    server.setConnectors(new Connector[]{connector, sslConnector});
                });
            }
        };
    }
}
