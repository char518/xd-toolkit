package cn.com.common.util.concurrent.spmc.disruptor;

/**
 * 数据持有者<br/>
 * Created by wuliwei on 2015/6/14.
 *
 * @author wuliwei
 */
final class DataHolder {
    private Object data;

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
