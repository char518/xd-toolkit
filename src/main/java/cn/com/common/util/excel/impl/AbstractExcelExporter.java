package cn.com.common.util.excel.impl;

import cn.com.common.util.excel.DataProvider;
import cn.com.common.util.excel.ExcelExporter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Excel导出工具适配器
 *
 * @author wuliwei
 */
public abstract class AbstractExcelExporter implements ExcelExporter {
    protected int maxRowPerSheet = 50000;
    protected String charsetName = "UTF-8";

    /**
     * 设置每个工作表最大行数
     *
     * @param pMaxRowPerSheet 工作表最大行数，默认是50000
     */
    public void setMaxRowPerSheet(int pMaxRowPerSheet) {
        maxRowPerSheet = pMaxRowPerSheet;
    }

    /**
     * 设置编码
     *
     * @param pCharsetName 编码，默认是UTF-8
     */
    public void setCharsetName(String pCharsetName) {
        charsetName = pCharsetName;
    }

    /**
     * 根据参数中提供的数据源和输出流，以ZIP压缩的形式将数据源中的数据输出到输出流，<br/>
     * 导出完成后，方法体内自行关闭输出流
     *
     * @param pDp  数据源
     * @param pOs  目标输出流
     * @param name ZIP压缩文档中的条目名
     * @throws IOException
     */
    public void exportZip(DataProvider pDp, OutputStream pOs, String name) throws IOException {
        ZipOutputStream zos = null;
        ZipEntry ze = null;
        try {
            zos = new ZipOutputStream(pOs);
            ze = new ZipEntry(name);
            zos.putNextEntry(ze);
            export(pDp, zos);
            zos.flush();
        } finally {
            close(zos);
            close(pOs);
        }
    }

    /**
     * 输出操作
     *
     * @param pOs 目标输出流
     * @param str 输出内容
     * @throws IOException
     */
    protected void write(OutputStream pOs, String str) throws IOException {
        byte[] b = str.getBytes(charsetName);
        pOs.write(b, 0, b.length);
    }

    protected static void close(OutputStream pOs) throws IOException {
        if (null == pOs) {
            return;
        }
        pOs.close();
    }

}
