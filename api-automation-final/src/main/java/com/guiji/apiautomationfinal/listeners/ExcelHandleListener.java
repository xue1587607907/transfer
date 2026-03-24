package com.guiji.apiautomationfinal.listeners;

import com.guiji.apiautomationfinal.entity.CaseInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.fesod.sheet.context.AnalysisContext;
import org.apache.fesod.sheet.read.listener.ReadListener;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Getter
@Slf4j
@Component
public class ExcelHandleListener implements ReadListener<CaseInfo> {
    private final List<CaseInfo> caseInfoList = new ArrayList<>();

    @Override
    public void invoke(CaseInfo caseInfo, AnalysisContext analysisContext) {
        if ("Y".equalsIgnoreCase(caseInfo.getRun())) {
            caseInfoList.add(caseInfo);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        log.info("read excel completed==========");
    }
}