package msa.lime1st.cloud.authorization;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"eureka.client.enabled=false"})
@AutoConfigureMockMvc
class AuthorizationApplicationTests {

	@Autowired
	MockMvc mvc;

	@Test
	void contextLoads() {
	}

	@Test
	void requestTokenUsingClientCredentialsGrantType() throws Exception {

		// secret 은 clientId:ClientSecret 을 base64 인코딩 한 값
		// console 에서 echo -n "ClientId:ClientSecret" | base64
		// echo "cmVhZGVyOnNlY3JldA==" | base64 --decode

		this.mvc.perform(post("/oauth2/token")
				.param("grant_type", "client_credentials")
				.header("Authorization", "Basic cmVhZGVyOnJlYWRlcg=="))
			.andExpect(status().isOk());
	}

	@Test
	void requestOpenidConfiguration() throws Exception {

		this.mvc.perform(get("/.well-known/openid-configuration"))
			.andExpect(status().isOk());
	}

	@Test
	void requestJwkSet() throws Exception {

		this.mvc.perform(get("/oauth2/jwks"))
			.andExpect(status().isOk());
	}
}
