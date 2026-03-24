package com.guiji.apiautomationfinal.listeners;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.xml.XmlSuite;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestSummaryReporter implements IReporter {
    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        int total;
        int passed = 0;
        int failed = 0;
        int skipped = 0;
        for (ISuite suite : suites) {
            Map<String, ISuiteResult> results = suite.getResults();
            for (ISuiteResult r : results.values()) {
                ITestContext ctx = r.getTestContext();
                passed += ctx.getPassedTests().size();
                failed += ctx.getFailedTests().size();
                skipped += ctx.getSkippedTests().size();
            }
        }
        total = passed + failed;
        double rate = total == 0 ? 0 : (passed * 100.0 / total);
        String passRate = String.format("%.2f", rate);
        Map<String, Object> map = new HashMap<>();
        map.put("total", total);
        map.put("passed", passed);
        map.put("failed", failed);
        map.put("skipped", skipped);
        map.put("passRate", passRate);
        map.put("env", System.getProperty("env", "UAT"));
        try {
            File dir = new File("target/test-result");
            if (!dir.exists()) dir.mkdirs();
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(new File(dir, "test-summary.json"), map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}