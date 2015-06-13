package cn.com.common.util.excel;

import cn.com.common.util.excel.impl.ExcelExporter4Xml;
import cn.com.common.util.excel.impl.ExcelExporterFactory;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuliwei on 2015/6/13.
 */
public class ExcelExporterTest {

    @Test
    public void testExport() throws Exception {
        File file = null;
        FileOutputStream fos = null;
        try {
            file = new File("C:/temp/测试.xls");
            if (!file.exists() || !file.isFile()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            ExcelExporter4Xml ee = ExcelExporterFactory.getExcelExporter4XmlInstance();
            ee.setCharsetName("UTF-8");
            ee.export(new DataProvider() {
                private int rowCount = 110000;
                private int colCount = 12;
                private int curRow = 0;

                public Object[] getHeadRow() {
                    String[] head = new String[colCount];
                    for (int i = 0; i < colCount; i++) {
                        head[i] = "行_" + i;
                    }
                    return head;
                }

                public List<Object[]> getNextRows() {
                    if (curRow < rowCount) {
                        String[] row = new String[colCount];
                        for (int i = 0; i < colCount; i++) {
                            row[i] = curRow + "_" + i;
                        }
                        curRow++;
                        ArrayList<Object[]> l = new ArrayList<Object[]>();
                        l.add(row);
                        return l;
                    }
                    return null;
                }

            }, fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testExportZip() throws Exception {
        File file = null;
        FileOutputStream fos = null;
        try {
            file = new File("C:/temp/测试.zip");
            if (!file.exists() || !file.isFile()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            ExcelExporter4Xml ee = ExcelExporterFactory.getExcelExporter4XmlInstance();
            ee.setCharsetName("UTF-8");
            ee.exportZip(new DataProvider() {
                private int rowCount = 110000;
                private int colCount = 12;
                private int curRow = 0;

                public Object[] getHeadRow() {
                    String[] head = new String[colCount];
                    for (int i = 0; i < colCount; i++) {
                        head[i] = "行_" + i;
                    }
                    return head;
                }

                public List<Object[]> getNextRows() {
                    if (curRow < rowCount) {
                        String[] row = new String[colCount];
                        for (int i = 0; i < colCount; i++) {
                            row[i] = curRow + "_" + i;
                        }
                        curRow++;
                        ArrayList<Object[]> l = new ArrayList<Object[]>();
                        l.add(row);
                        return l;
                    }
                    return null;
                }

            }, fos, "测试.xls");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}