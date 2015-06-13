package cn.com.common.util.excel;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author wuliwei
 */
public interface ExcelExporter {
    /**
     * 根据参数中提供的数据源和输出流，将数据源中的数据输出到输出流，<br/>
     * 导出完成后，方法体内自行关闭输出流
     *
     * @param dp 数据源
     * @param os 目标输出流
     * @return 导出操作结果：成功为true，失败为false
     */
    public void export(DataProvider dp, OutputStream os) throws IOException;

    /**
     * 根据参数中提供的数据源和输出流，以ZIP压缩的形式将数据源中的数据输出到输出流，<br/>
     * 导出完成后，方法体内自行关闭输出流
     *
     * @param dp   数据源
     * @param os   目标输出流
     * @param name ZIP压缩文档中的条目名
     * @throws IOException
     */
    public void exportZip(DataProvider dp, OutputStream os, String name) throws IOException;
}
