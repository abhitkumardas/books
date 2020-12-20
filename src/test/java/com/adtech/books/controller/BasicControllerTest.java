package com.adtech.books.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BasicControllerTest {

    @Value("${security.oauth2.client.client-id}")
    private String clientId;

    @Value("${security.oauth2.client.client-secret}")
    private String clientSecret;

    @Value("${auth.server-uri}")
    private String authServerUri;

    @Autowired
    private MockMvc mockMvc;

    RestTemplate restTemplate = new RestTemplate();

    private String testOauthUserName="asitk";
    private String testOauthPassword="12345";
    private String jwtToken="";


    @Test
    @BeforeAll
    public void shouldBeUnAuthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/welcome")
                .accept(MediaType.ALL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getTokenSuccessfully() throws Exception {
        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_FORM_URLENCODED));
        headers.add("Authorization", "Basic " + encodedCredentials);

        HttpEntity<String> request = new HttpEntity<String>(headers);
        String serverIpAddress = "127.0.0.1"; // only local communication
        String serverPort = "9000";

        String apiUrl = "http://" + serverIpAddress + ":" + serverPort + "/oauth/token"
                + "?grant_type=password&" + "username=" + testOauthUserName 
                + "&password=" + testOauthPassword;

        ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseEntity.getBody());
        jwtToken = root.findValue("access_token").asText();
        assertNotNull(responseEntity.getBody());
        assertNotNull(root.path("name").asText());

        HttpStatus code = responseEntity.getStatusCode();
    }

    @Test
    @AfterAll
    public void shouldBeAuthorized() throws Exception {

        String serverIpAddress = "127.0.0.1"; // only local communication
        String serverPort = "9001";
        String apiUrl = "http://" + serverIpAddress + ":" + serverPort + "/welcome";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+ jwtToken);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);

        Assert.assertTrue(responseEntity.getBody().contains("Welcome to Book App !!!"));
    }


}