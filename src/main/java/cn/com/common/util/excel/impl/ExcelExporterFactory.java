package cn.com.common.util.excel.impl;

/**
 * Excel导出工具工厂
 *
 * @author wuliwei
 */
public class ExcelExporterFactory {
    /**
     * @return Xml格式的Excel导出工具对象
     */
    public static ExcelExporter4Xml getExcelExporter4XmlInstance() {
        return new ExcelExporter4Xml();
    }
}
