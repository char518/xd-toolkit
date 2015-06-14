package cn.com.common.util.concurrent.spmc;

import com.lmax.disruptor.EventFactory;

/**
 * 数据持有者工厂<br/>
 * Created by wuliwei on 2015/6/14.
 *
 * @author wuliwei
 */
final class DataHolderFactory implements EventFactory<DataHolder> {
    public DataHolder newInstance() {
        return new DataHolder();
    }
}
