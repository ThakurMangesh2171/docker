//package com.vassarlabs.gp.config;
//
//import com.google.gson.Gson;
//import com.vassarlabs.gp.pojo.SecretValues;
//import io.akeyless.client.ApiClient;
//import io.akeyless.client.ApiException;
//import io.akeyless.client.model.Configure;
//import io.akeyless.client.model.ConfigureOutput;
//import io.akeyless.client.model.GetSecretValue;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import javax.annotation.PostConstruct;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Map;
//
//@Configuration
//@ConfigurationProperties("application-akeyless.properties")
//public class AkeylessConfig {
//
//	@Value("${akeyless.basePath}")
//	private String akeylessBasePath;
//
//	@Value("${akeyless.access.id}")
//	private String akeylessAccessId;
//
//
//	@Value("${akeyless.access.key}")
//	private String akeylessAcessKey;
//
//
//	@Value("${akeyless.access.password.secretName}")
//	private String akeylessSecretName;
//
//
//
//
////	@PostConstruct
//	@Bean
//	public SecretValues initializeAkeylessClient() throws ApiException {
//		// Additional configurations if required
//
//		ApiClient client = io.akeyless.client.Configuration.getDefaultApiClient();
//		client.setBasePath(akeylessBasePath);
//		SecretValues secretValues = new SecretValues();
//
//		io.akeyless.client.api.V2Api api = new io.akeyless.client.api.V2Api(client);
//
//		try {
//
//			Configure configureBody = new Configure();
//			configureBody.accessId(akeylessAccessId).accessKey(akeylessAcessKey);
//			ConfigureOutput out;
//			out = api.configure(configureBody);
//			String token = out.getToken();
//			GetSecretValue body = new GetSecretValue(); // GetSecretValue |
//			body.setToken(token);
//			body.setNames(Collections.singletonList(akeylessSecretName));
//			Map<String, String> secretNameToValueMap = api.getSecretValue(body);
//			for(String secretKeyName : secretNameToValueMap.keySet()){
//				if(secretKeyName.equals(akeylessSecretName)){
//					secretValues = new Gson().fromJson(secretNameToValueMap.get(secretKeyName),SecretValues.class);
//				}
//			}
//			System.out.println(secretNameToValueMap);
//
//		} catch (ApiException e) {
//			System.err.println("Status code: " + e.getCode());
//			System.err.println("Reason: " + e.getResponseBody());
//			System.err.println("Response headers: " + e.getResponseHeaders());
//			e.printStackTrace();
//		}
//		return secretValues;
//
//	}
//
//}