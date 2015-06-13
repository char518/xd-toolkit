package cn.com.common.util.excel;

import java.util.List;

/**
 * @author wuliwei
 */
public interface DataProvider {
    /**
     * 获取表格标题行信息，数组长度与表格列数一致
     *
     * @return EXCEL表格标题行
     */
    public Object[] getHeadRow();

    /**
     * 获取表格下一批数据，如果已到表格尾，则返回null或空的List
     *
     * @return 表格下一批数据
     */
    public List<Object[]> getNextRows();
}
