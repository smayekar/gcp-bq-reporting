package io.ctl.cloudintegration.gcp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class GcpBqApplication {
    @Value("${spring.rest.ctl.url}")
    private String ctlUrl;

    @Bean
    public Docket orderApi() {
        return new Docket(DocumentationType.SWAGGER_2).groupName("GCP BigQuery Persistence")
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .build()
                .apiInfo(apiInfo())
                .useDefaultResponseMessages(false);
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("GCP BigQuery Persistence API")
                .description("API for running GCP BigQuery Queries")
                .termsOfServiceUrl(ctlUrl)
                .contact(new Contact("CenturyLink Inc.", ctlUrl, ""))
                .licenseUrl(ctlUrl).version("1.0").build();
    }


    public static void main(String[] args) {
        SpringApplication.run(GcpBqApplication.class, args);
    }
}
