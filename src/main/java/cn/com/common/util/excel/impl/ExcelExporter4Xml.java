package cn.com.common.util.excel.impl;

import cn.com.common.util.excel.DataProvider;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Xml格式的Excel导出工具
 *
 * @author wuliwei
 */
public class ExcelExporter4Xml extends AbstractExcelExporter {
    private static String workBookStart(String charsetName) {
        String wbs = "<?xml version=\"1.0\" encoding=\""
                + charsetName
                + "\"?>"
                + "\n<?mso-application progid=\"Excel.Sheet\"?>"
                + "\n<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\""
                + "\n xmlns:o=\"urn:schemas-microsoft-com:office:office\""
                + "\n xmlns:x=\"urn:schemas-microsoft-com:office:excel\""
                + "\n xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\""
                + "\n xmlns:html=\"http://www.w3.org/TR/REC-html40\">"
                + "\n<DocumentProperties xmlns=\"urn:schemas-microsoft-com:office:office\">"
                + "\n<Version>11.9999</Version>"
                + "\n</DocumentProperties>"
                + "\n<ExcelWorkbook xmlns=\"urn:schemas-microsoft-com:office:excel\">"
                + "\n<WindowHeight>10005</WindowHeight>"
                + "\n<WindowWidth>10005</WindowWidth>"
                + "\n<WindowTopX>120</WindowTopX>"
                + "\n<WindowTopY>135</WindowTopY>"
                + "\n<ActiveSheet>1</ActiveSheet>"
                + "\n<ProtectStructure>False</ProtectStructure>"
                + "\n<ProtectWindows>False</ProtectWindows>"
                + "\n</ExcelWorkbook>" + "\n<Styles>"
                + "\n<Style ss:ID=\"Default\" ss:Name=\"Normal\">"
                + "\n<Alignment ss:Vertical=\"Center\"/>" + "\n<Borders/>"
                + "\n<Interior/>" + "\n<NumberFormat/>" + "\n<Protection/>"
                + "\n</Style>" + "\n<Style ss:ID=\"s23\">"
                + "\n<Interior ss:Color=\"#C0C0C0\" ss:Pattern=\"Solid\"/>"
                + "\n</Style>" + "\n</Styles>";
        return wbs;
    }

    private static String workBookEnd() {
        return "\n</Workbook>";
    }

    private static String workSheetStart(String name) {
        return "\n<Worksheet ss:Name=\"" + name + "\">";
    }

    private static String workSheetEnd() {
        return "\n<WorksheetOptions xmlns=\"urn:schemas-microsoft-com:office:excel\">"
                + "\n<Unsynced/>"
                + "\n<ProtectObjects>False</ProtectObjects>"
                + "\n<ProtectScenarios>False</ProtectScenarios>"
                + "\n</WorksheetOptions>" + "\n</Worksheet>";
    }

    private static String tableStart() {
        return "\n<Table>";
    }

    private static String tableEnd() {
        return "\n</Table>";
    }

    private static String rowStart() {
        return "\n<Row>";
    }

    private static String rowEnd() {
        return "\n</Row>";
    }

    private static String cell(Object value) {
        if (null == value) {
            value = "";
        }
        return "\n<Cell><Data ss:Type=\"String\">" + value + "</Data></Cell>";
    }

    /**
     * 根据参数中提供的数据源和输出流，将数据源中的数据输出到输出流，<br/>
     * 导出完成后，方法体内自行关闭输出流
     *
     * @param pDp 数据源
     * @param pOs 目标输出流
     * @throws IOException
     */
    public void export(DataProvider pDp, OutputStream pOs) throws IOException {
        try {
            write(pOs, workBookStart(charsetName));
            boolean sheetStart = false, sheetEnd = false, headStart = true;
            int sheetIndex = 1, rowIndex = 0, i = 0;
            List<Object[]> rows = pDp.getNextRows();
            Object[] row = null == rows || rows.size() < 1 ? null : rows
                    .get(i++);
            while (null != row || headStart) {
                sheetStart = 0 == rowIndex % maxRowPerSheet;
                if (sheetStart) {
                    write(pOs, workSheetStart("sheet" + sheetIndex++));
                    write(pOs, tableStart());
                }
                if (headStart) {
                    headStart = false;
                    write(pOs, rowStart());
                    for (Object o : pDp.getHeadRow()) {
                        write(pOs, cell(o));
                    }
                    write(pOs, rowEnd());
                }
                if (null != row) {
                    write(pOs, rowStart());
                    for (Object o : row) {
                        write(pOs, cell(o));
                    }
                    write(pOs, rowEnd());
                    rowIndex++;
                    sheetEnd = 0 == rowIndex % maxRowPerSheet;
                    if (i < rows.size()) {
                        row = rows.get(i++);
                    } else {
                        rows = pDp.getNextRows();
                        i = 0;
                        row = null == rows || rows.size() < 1 ? null : rows
                                .get(i++);
                    }
                }
                if (sheetEnd || null == row) {
                    write(pOs, tableEnd());
                    write(pOs, workSheetEnd());
                }
            }
            write(pOs, workBookEnd());
            pOs.flush();
        } finally {
            close(pOs);
        }
    }

}
