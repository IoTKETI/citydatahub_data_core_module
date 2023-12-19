package kr.re.keti.sc.datacoreui.common.service;

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Component of data core api request 
 * @FileName DataCoreRequestFactory.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Component
public class DataCoreRequestFactory {
	private RestTemplate restTemplate = new RestTemplate();

	/**
	 * Constructor of DataCoreRequestFactory 
	 */
    public DataCoreRequestFactory() {
        this.restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestWithBodyFactory());
    }

    /**
     * HttpComponents HttpClient to create requests with body
     */
    private static final class HttpComponentsClientHttpRequestWithBodyFactory extends HttpComponentsClientHttpRequestFactory {
    	/**
    	 * Create http uri request
    	 */
        @Override
        protected HttpUriRequest createHttpUriRequest(HttpMethod httpMethod, URI uri) {
            if (httpMethod == HttpMethod.GET) {
                return new HttpGetRequestWithEntity(uri);
            }
            return super.createHttpUriRequest(httpMethod, uri);
        }
    }

    /**
     * Http get request with entity
     */
    private static final class HttpGetRequestWithEntity extends HttpEntityEnclosingRequestBase {
        public HttpGetRequestWithEntity(final URI uri) {
            super.setURI(uri);
        }

        /**
         * Get http method
         */
        @Override
        public String getMethod() {
            return HttpMethod.GET.name();
        }
    }

    /**
     * Get Rest template
     * @return
     */
    public RestTemplate getRestTemplate() {
        return restTemplate;
    }
}
