package com.guiji.apiautomationfinal.features;


import com.guiji.apiautomationfinal.config.CucumberTestConfig;
import com.guiji.apiautomationfinal.utils.RestAssuredUtils;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.testng.Assert;

@Slf4j
public class UserStepDef extends CucumberTestConfig {
    private final RestAssuredUtils restAssuredUtils;
    @Value("${tfx.base_url}")
    String base_url;
    String result = "";
    String full_url;

    @Autowired
    public UserStepDef(RestAssuredUtils restAssuredUtils) {
        this.restAssuredUtils = restAssuredUtils;
    }

    @Before
    void before() {
        result = null;
    }

    @Given("Api path is {string}")
    public void apiPathIs(String apiPath) {
        full_url = base_url + apiPath;
        log.info("API PATH==>{}", full_url);
    }

    @When("send {string} request to get result")
    public void sendRequestToGetResult(String requestMethod) {
        Response res = restAssuredUtils.sendRequest(full_url, requestMethod);
        res.prettyPrint();
        result = res.asString();
    }

    @Then("validate result if including {string}")
    public void validateResultIfIncludingExpectedRes(String arg0) {
        Assert.assertTrue(result.toLowerCase().contains(arg0));
    }

    @And("also validate result if including {string}")
    public void alsoValidateResultIfIncludingExpectedRes(String arg0) {
        Assert.assertTrue(result.toLowerCase().contains(arg0));
    }
}