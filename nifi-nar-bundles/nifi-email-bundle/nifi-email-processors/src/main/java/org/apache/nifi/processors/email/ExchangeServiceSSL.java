package org.apache.nifi.processors.email;

import microsoft.exchange.webservices.data.EWSConstants;
import microsoft.exchange.webservices.data.core.CookieProcessingTargetAuthenticationStrategy;
import microsoft.exchange.webservices.data.core.EwsSSLProtocolSocketFactory;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import java.security.GeneralSecurityException;

public class ExchangeServiceSSL extends ExchangeService {

    public ExchangeServiceSSL(ExchangeVersion requestedServerVersion) throws GeneralSecurityException {
        super(requestedServerVersion);
        initializeHttpClientTrustAll();
    }

    private void initializeHttpClientTrustAll() throws GeneralSecurityException {

        SSLContext trustAllSslContext = new SSLContextBuilder().loadTrustMaterial(null, (TrustStrategy) (arg0, arg1) -> true).build();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register(EWSConstants.HTTP_SCHEME, new PlainConnectionSocketFactory())
                .register(EWSConstants.HTTPS_SCHEME,
                        new EwsSSLProtocolSocketFactory(trustAllSslContext, NoopHostnameVerifier.INSTANCE)
                )
                .build();

        HttpClientConnectionManager httpConnectionManager = new PoolingHttpClientConnectionManager(registry);
        AuthenticationStrategy authStrategy = new CookieProcessingTargetAuthenticationStrategy();

        httpClient = HttpClients.custom()
                .setConnectionManager(httpConnectionManager)
                .setTargetAuthenticationStrategy(authStrategy)
                .setSslcontext(trustAllSslContext)
                .build();
    }
}
