package AzureRESTExample;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        String urlString = "https://management.azure.com//subscriptions/75886f86-369e-4449-9d47-963be0a68d11/resourceGroups/newresource/providers/Microsoft.Storage/storageAccounts/newresourcediag286/services/file/providers/microsoft.insights/metricDefinitions?api-version=2015-07-01";
        try {
            executeRESTAPI(urlString);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void executeRESTAPI(String urlString) throws Exception{

        final RequestConfig config = RequestConfig.custom().setConnectTimeout(3000)
                .setConnectionRequestTimeout(12000).setSocketTimeout(5000)
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
        HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config).useSystemProperties()
                .setRetryHandler(new StandardHttpRequestRetryHandler()).build();


        URL url  = new URL(urlString);
        AuthenticationResult result = getAccessTokenFromClientCredentials();
        System.out.println(result.getExpiresOnDate());
        final HttpGet httpget = new HttpGet(urlString);
        httpget.setHeader(HttpHeaders.ACCEPT, "application/json");
        httpget.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + result.getAccessToken());
        httpget.setHeader(HttpHeaders.ACCEPT_LANGUAGE, "en-us");
        httpget.setHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, default");


        try {
            final HttpHost target = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
            // don't close the response object, just the underlying stream. this
            // let's it be reused by the pool manager
            HttpResponse response = httpClient.execute(target,httpget);

        } catch (final HttpResponseException e) {
            if (e.getStatusCode() == Constants.NOT_FOUND && urlString.contains("Microsoft.Storage")) {
                throw new FileNotFoundException(urlString);
            } else {
                LOG.error(kModule,"Fatal transport error processsing get11 request" + e + " : " + url);
            }
        } catch (final IOException e) {
            LOG.error(kModule,"Fatal transport error processsing get request " + urlString, e);
            throw e;
        } catch (Exception e) {
            LOG.error(kModule,"Exception inside processGetRequest()" + e);
        }
    }

    public static AuthenticationResult getAccessTokenFromClientCredentials()
            {
        AuthenticationResult result = null;
        try {
            final String adTenantId = "1194df16-3ae0-49aa-b48b-5c4da6e13689";
            final String clientId = "d1e2eddc-54af-491a-b726-3ce79247976c";
            final String clientSecret = "txHL4IPqX9F6uDKHmPOZkU9nMwhspvANn117C2SLcNI=";
            final String loginUrl = "https://login.windows.net/";
            final String baseUrl = "https://management.azure.com/";
            final AuthenticationContext context = new AuthenticationContext(
                    loginUrl + adTenantId, false,
                    Executors.newCachedThreadPool());
            // be very careful here, trailing slash is required
            final Future<AuthenticationResult> future = context.acquireToken(
                    baseUrl, new ClientCredential(clientId, clientSecret), null);
            result = future.get();

            if (result == null) {
                System.err.println("authentication result was null");
            }
        } catch (final ExecutionException e) {
            System.err.println("Error getting access token" + e);
        } catch (MalformedURLException e) {
            System.err.println(e.getMessage() + e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
                return result;
    }
}
