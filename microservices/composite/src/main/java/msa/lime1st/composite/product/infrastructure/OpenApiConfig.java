package msa.lime1st.composite.product.infrastructure;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * type = SecuritySchemeType.OAUTH@ 보안 스키마는 OAuth 2.0 기반이다.
 * flows: 권한 코드 승인 흐름을 사용한다.
 * authorizationUrl, tokenUrl 매개 변수(application.yml 에 정의된)를 사용해 권한 코드 및 접근 토큰 전달용 URL 을 구성한다.
 * scopes 스웨거 UI 에서 API 를 호출하는 데 필요한 스코프를 정의한다.
 */
@SecurityScheme(
    name = "security_auth", type = SecuritySchemeType.OAUTH2,
    flows = @OAuthFlows(
        authorizationCode = @OAuthFlow(
            authorizationUrl = "${springdoc.oAuthFlow.authorizationUrl}",
            tokenUrl = "${springdoc.oAuthFlow.tokenUrl}",
            scopes = {
                @OAuthScope(name = "product:read", description = "read scope"),
                @OAuthScope(name = "product:write", description = "write scope")
            }
        )
    )
)
@Configuration
public class OpenApiConfig {

    @Value("${api.common.version}")                        String apiVersion;
    @Value("${api.common.title}")                   String apiTitle;
    @Value("${api.common.description}")    String apiDescription;
    @Value("${api.common.termsOfService}")          String apiTermsOfService;
    @Value("${api.common.license}")                   String apiLicense;
    @Value("${api.common.licenseUrl}")               String apiLicenseUrl;
    @Value("${api.common.externalDocDesc}")                 String apiExternalDocDesc;
    @Value("${api.common.externalDocUrl}")                  String apiExternalDocUrl;
    @Value("${api.common.contact.name}")              String apiContactName;
    @Value("${api.common.contact.url}")               String apiContactUrl;
    @Value("${api.common.contact.email}")             String apiContactEmail;

    @Bean
    public OpenAPI getOpenApiDocumentation() {
        return new OpenAPI()
            .info(new Info().title(apiTitle)
                .description(apiDescription)
                .version(apiVersion)
                .contact(new Contact()
                    .name(apiContactName)
                    .url(apiContactUrl)
                    .email(apiContactEmail))
                .termsOfService(apiTermsOfService)
                .license(new License()
                    .name(apiLicense)
                    .url(apiLicenseUrl)))
            .externalDocs(new ExternalDocumentation()
                .description(apiExternalDocDesc)
                .url(apiExternalDocUrl));
    }
}
