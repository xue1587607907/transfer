package com.guiji.apiautomationfinal.utils;


import com.guiji.apiautomationfinal.constant.Constant;
import com.guiji.apiautomationfinal.entity.CaseInfo;
import com.guiji.apiautomationfinal.listeners.ExcelHandleListener;
import lombok.SneakyThrows;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.read.listener.ReadListener;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

public class ExcelUtils {
    @SneakyThrows
    public static List<CaseInfo> getExcelData(String filePath, int sheetIndex) {
        Workbook workbook = new XSSFWorkbook(new FileInputStream(filePath));
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        handleMergedCells(sheet);
        File tempFile = File.createTempFile("temp", ".xlsx");
        tempFile.deleteOnExit();
        try (FileOutputStream fileOut = new FileOutputStream(tempFile)) {
            workbook.write(fileOut);
        }
        workbook.close();
        return FesodSheet.read(tempFile, CaseInfo.class, new ExcelHandleListener()).sheet(sheetIndex).doReadSync();
    }

    @SneakyThrows
    public static List<CaseInfo> getExcelDataBySheetName(String filePath, String sheetName) {
        Workbook workbook = new XSSFWorkbook(new FileInputStream(filePath));
        Sheet sheet = workbook.getSheet(sheetName);
        handleMergedCells(sheet);
        File tempFile = File.createTempFile("temp", ".xlsx");
        tempFile.deleteOnExit();
        try (FileOutputStream fileOut = new FileOutputStream(tempFile)) {
            workbook.write(fileOut);
        }
        workbook.close();
        return FesodSheet.read(tempFile, CaseInfo.class, new ExcelHandleListener()).sheet(sheetName).doReadSync();
    }

    @SneakyThrows
    public static <T> void getExcelDataBySheetNameAsync(String filePath, String sheetName, ReadListener<T> readListener) {
        Workbook workbook = new XSSFWorkbook(new FileInputStream(filePath));
        Sheet sheet = workbook.getSheet(sheetName);
        handleMergedCells(sheet);
        File tempFile = File.createTempFile("temp", ".xlsx");
        tempFile.deleteOnExit();
        try (FileOutputStream fileOut = new FileOutputStream(tempFile)) {
            workbook.write(fileOut);
        }
        workbook.close();
        FesodSheet.read(tempFile, CaseInfo.class, readListener).sheet(sheetName).doRead();
    }

    private static void handleMergedCells(Sheet sheet) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            int firstRow = mergedRegion.getFirstRow();
            int lastRow = mergedRegion.getLastRow();
            int firstCol = mergedRegion.getFirstColumn();
            int lastCol = mergedRegion.getLastColumn();
            Row row = sheet.getRow(firstRow);
            Cell cell = row.getCell(firstCol);
            String value = cell.toString();
            for (int r = firstRow; r <= lastRow; r++) {
                for (int c = firstCol; c <= lastCol; c++) {
                    Row currentRow = sheet.getRow(r);
                    if (currentRow == null) {
                        currentRow = sheet.createRow(r);
                    }
                    Cell currentCell = currentRow.getCell(c);
                    if (currentCell == null) {
                        currentCell = currentRow.createCell(c);
                    }
                    currentCell.setCellValue(value);
                }
            }
        }
    }

    @SneakyThrows
    public static void main(String[] args) throws Exception {
        ExcelHandleListener excelHandleListener = new ExcelHandleListener();
        getExcelDataBySheetNameAsync(Constant.DATA_PATH, "FXRate", excelHandleListener);
        getExcelDataBySheetNameAsync(Constant.DATA_PATH, "InnerAccount", excelHandleListener);
    }

    @SneakyThrows
    public static void writeTestResultToExcel(CaseInfo caseInfo, String testResultOutputPath) {
        XSSFWorkbook excel = new XSSFWorkbook(new FileInputStream(testResultOutputPath));
        XSSFSheet sheet = excel.getSheetAt(0);
        int targetRow = findRowByValue(sheet, caseInfo.getCaseId());
        int lastCellNum = sheet.getRow(targetRow).getLastCellNum();
        XSSFRow fillRow = sheet.getRow(targetRow);
        XSSFCell cell = fillRow.getCell(lastCellNum - 1);
        XSSFCellStyle style = excel.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        cell.setCellStyle(style);
        FileOutputStream fos = new FileOutputStream(testResultOutputPath);
        excel.write(fos);
        fos.close();
        excel.close();
    }

    public static int findRowByValue(Sheet sheet, String value) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().equalsIgnoreCase(value)) {
                    return row.getRowNum();
                }
            }
        }
        return -1;
    }
}
