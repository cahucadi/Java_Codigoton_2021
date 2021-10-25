package org.cahucadi.reto.domain;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class ClientAdapter {

	/**
	 * @param code from @Postload event
	 * @return decrypt decrypted code using web service
	 */
	public static String decryptService(String code) {

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate
				.getForEntity("https://test.evalartapp.com/extapiquest/code_decrypt/" + code, String.class);
		String decrypt = response.getBody();
		return decrypt.replace("\"", "");
	}

}
