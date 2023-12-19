package kr.re.keti.sc.datacoreusertool.common.configuration;

import java.nio.charset.Charset;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Class of rest template configuration
 * @FileName RestTemplateConfiguration.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Configuration
public class RestTemplateConfiguration {
	
	@Value("${http.pool.max.total}")
    private Integer maxTotal;
	@Value("${http.pool.defaultMaxPerRoute}")
	private Integer defaultMaxPerRoute;
	@Value("${http.pool.connection.timeout}")
	private Integer connectionTimeout;
	@Value("${http.pool.connection.request.timeout}")
	private Integer connectionRequestTimeout;
	@Value("${http.pool.read.timeout}")
	private Integer readTimeout;
	@Value("${http.pool.validate.after.inactivity}")
	private Integer validateAfterInactivity;
 
	@Bean
    public RestTemplate restTemplate() {
       RestTemplate restTemplate = new RestTemplate(httpRequestFactory());
       restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        return restTemplate;
    }
 
    @Bean
    public ClientHttpRequestFactory httpRequestFactory() {
        return new HttpComponentsClientHttpRequestFactory(httpClient());
    }
 
    @Bean
    public HttpClient httpClient() {

        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        connectionManager.setMaxTotal(maxTotal);
        connectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
        connectionManager.setValidateAfterInactivity(validateAfterInactivity);

        RequestConfig requestConfig = RequestConfig.custom()
                //The time for the server to return data (response) exceeds the throw of read timeout
                .setSocketTimeout(readTimeout)
                //The time to connect to the server (handshake succeeded) exceeds the throw connect timeout
                .setConnectTimeout(connectionTimeout) 
                //The timeout to get the connection from the connection pool. If the connection is not available after the timeout, the following exception will be thrown
                // org.apache.http.conn.ConnectionPoolTimeoutException: Timeout waiting for connection from pool
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .build();

        return HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .build();
    }
}
