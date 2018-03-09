package net.pisecurity.twillio;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TwilioSMS {
	private static Logger logger = LogManager.getLogger(TwilioSMS.class);

	private String url = "https://api.twilio.com/2010-04-01/Accounts/{account}/Messages.json";
	private GsonBuilder builder = new GsonBuilder();
	private TwilioAccountDetails accountDetails;

	private HttpClient client;

	public TwilioSMS(TwilioAccountDetails accountDetails) {
		super();
		this.accountDetails = accountDetails;
		HttpClientBuilder builder = HttpClientBuilder.create();
		this.client = builder.build();
	}

	public List<TwilioSMSResponse> sendSms(String[] targetNumbers, String message)
			throws ClientProtocolException, IOException, URISyntaxException {

		String s = this.url.replace("{account}", accountDetails.account);
		URI uri = new URI(s);
		HttpClientContext context = HttpClientContext.create();
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(uri.getHost(), uri.getPort()),
				new UsernamePasswordCredentials(this.accountDetails.account, this.accountDetails.apiKey));

		// Create AuthCache instance
		AuthCache authCache = new BasicAuthCache();
		// Generate BASIC scheme object and add it to the local auth cache
		BasicScheme basicAuth = new BasicScheme();
		HttpHost targetHost = new HttpHost(uri.getHost(), uri.getPort(), "https");
		authCache.put(targetHost, basicAuth);
		context.setCredentialsProvider(credsProvider);
		context.setAuthCache(authCache);

		Gson gson = this.builder.create();
		List<TwilioSMSResponse> responses = new ArrayList<>();

		for (String target : targetNumbers) {
			HttpPost request = new HttpPost(uri);

			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.addTextBody("To", target);
			builder.addTextBody("From", accountDetails.phoneNumber);
			builder.addTextBody("Body", message);

			HttpEntity multipart = builder.build();
			request.setEntity(multipart);

			HttpResponse resp = client.execute(request, context);
			InputStream is = resp.getEntity().getContent();
			int i;
			StringBuilder responseString = new StringBuilder();
			while ((i = is.read()) != -1) {
				responseString.append((char) i);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Got response :" + responseString);

			}
			TwilioSMSResponse responseObject = gson.fromJson(responseString.toString(), TwilioSMSResponse.class);

			if (responseObject.error_code!=null) {
				logger.info("Saw error response : " + responseObject);
			}
			
			responses.add(responseObject);
		}

		return responses;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException, URISyntaxException {

		GsonBuilder builder = new GsonBuilder();
		
//		try (FileReader r = new FileReader("./resources/smsresponse.json");) {
//			TwilioSMSResponse details = builder.create().fromJson(r, TwilioSMSResponse.class);
//			
//			System.out.println(details);;
//			
//		}
		
		
		try (FileReader r = new FileReader("./resources/twillio.prod.json");) {
			TwilioAccountDetails details = builder.create().fromJson(r, TwilioAccountDetails.class);
			TwilioSMS ts = new TwilioSMS(details);

			ts.sendSms(new String[] { "+447855311224","+447855311224" }, "Hello world");

		}
	}
	//
	//
	//
	// curl '/AC4f8885d8a0c948a287ec039eabce1970/Messages.json' -X POST \
	// --data-urlencode 'To=+447855311224' \
	// --data-urlencode 'From=+447480619081' \
	// --data-urlencode 'Body=test' \
	// -u AC4f8885d8a0c948a287ec039eabce1970:fde1fd3c5d8ff2052472798792cb8b1a
	//
}
