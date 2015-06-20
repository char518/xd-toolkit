package cn.com.common.util.concurrent.spmc.disruptor;

import cn.com.common.util.concurrent.spmc.DataConsumer;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;

/**
 * 数据消费者代理<br/>
 * Created by wuliwei on 2015/6/14.
 *
 * @author wuliwei
 */
final class DataConsumerProxy implements DataConsumer, EventHandler<DataHolder>, WorkHandler<DataHolder> {
    private DataConsumer dataConsumer;

    public DataConsumerProxy(DataConsumer dataConsumer) {
        this.dataConsumer = dataConsumer;
    }

    public void handleData(Object data) {
        this.dataConsumer.handleData(data);
    }

    public void onEvent(DataHolder dataHolder) throws Exception {
        if (null != dataHolder && null != dataHolder.getData()) {
            handleData(dataHolder.getData());
        }
    }

    public void onEvent(DataHolder dataHolder, long sequence, boolean endOfBatch) throws Exception {
        onEvent(dataHolder);
    }
}
