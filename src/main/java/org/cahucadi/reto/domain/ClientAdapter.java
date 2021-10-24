package org.cahucadi.reto.domain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


public class ClientAdapter {
	
	public static String decryptService(String code){

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String > response =
                  restTemplate.getForEntity(
                  "https://test.evalartapp.com/extapiquest/code_decrypt/"+code,
                  String.class);
                String decrypt = response.getBody();
                return  decrypt.replace("\"", "");
    }
	
}
