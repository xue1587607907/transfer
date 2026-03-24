package com.guiji.apiautomationfinal.config;


import com.guiji.apiautomationfinal.Application;
import io.cucumber.spring.CucumberContextConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@CucumberContextConfiguration
@SpringBootTest(classes = Application.class)
public class CucumberTestConfig {
}