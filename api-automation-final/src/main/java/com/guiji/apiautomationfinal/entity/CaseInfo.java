package com.guiji.apiautomationfinal.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fesod.sheet.annotation.ExcelProperty;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CaseInfo {
    @ExcelProperty("Run")
    private String run;

    @ExcelProperty("CaseId")
    private String caseId;

    @ExcelProperty("Country")
    private String country;

    @ExcelProperty("Application")
    private String application;

    @ExcelProperty("MTFormat")
    private String mtFormat;

    @ExcelProperty("ContentType")
    private String contentType;

    @ExcelProperty("Module")
    private String module;

    @ExcelProperty("Description")
    private String description;

    @ExcelProperty("ReqHeader")
    private String reqHeader;

    @ExcelProperty("ReqMethod")
    private String reqMethod;

    @ExcelProperty("Path")
    private String path;

    @ExcelProperty("ReqParams")
    private String reqParams;

    @ExcelProperty("ExpectedResult")
    private String expectedResult;

    @ExcelProperty("AsyncApi")
    private String asyncApi;

    @ExcelProperty("DependsOn")
    private String dependsOn;

    @ExcelProperty("ExtractExpress")
    private String extractExpress;

    private String rerunTimes;
}
