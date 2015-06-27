package cn.com.common.util.concurrent.spmc.original;

import cn.com.common.util.concurrent.spmc.DataConsumer;
import cn.com.common.util.concurrent.spmc.DataProducer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 单生产者多消费者组件（实际支持多生产者多消费者模式）<br/>
 * 使用指南：<br/>
 * 1、创建该类的实例<br/>
 * 2、设置生产者实例、消费者实例<br/>
 * 3、设置生产者数量、消费者数量、队列大小、<br/>
 * 内部自动拉取数据、自动拉取数据间隔时间（毫秒）；该过程非必选可跳过<br/>
 * 4、启动<br/>
 * 5、外部定时触发拉取数据；当内部自动拉取数据时，该过程非必选可跳过<br/>
 * 6、停止；该过程非必选<br/>
 * Created by wuliwei on 2015/6/16.
 *
 * @author wuliwei
 */
public class OriginalSpmc {
    private DataProducer dataProducer;
    private DataConsumer dataConsumer;
    private int producerCount = 1;
    private int consumerCount = 4;
    private int bufferSize = 128;
    private boolean autoFetchData = true;
    private int fetchDataInterval = 10;
    private int interval = 1;
    private DataProducerProcessor[] dataProducerProcessors;
    private DataConsumerProcessor[] dataConsumerProcessors;
    private ExecutorService executorService;

    /**
     * 设置生产者实例<br/>
     *
     * @param dataProducer
     */
    public void setDataProducer(DataProducer dataProducer) {
        this.dataProducer = dataProducer;
    }

    /**
     * 设置消费者实例<br/>
     *
     * @param dataConsumer
     */
    public void setDataConsumer(DataConsumer dataConsumer) {
        this.dataConsumer = dataConsumer;
    }

    /**
     * 设置生产者数量<br/>
     *
     * @param producerCount
     */
    public void setProducerCount(int producerCount) {
        this.producerCount = producerCount;
    }

    /**
     * 设置消费者数量<br/>
     *
     * @param consumerCount
     */
    public void setConsumerCount(int consumerCount) {
        this.consumerCount = consumerCount;
    }

    /**
     * 设置队列大小<br/>
     *
     * @param bufferSize
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * 设置是否内部自动拉取数据<br/>
     *
     * @param autoFetchData
     */
    public void setAutoFetchData(boolean autoFetchData) {
        this.autoFetchData = autoFetchData;
    }

    /**
     * 设置内部自动拉取数据的时间间隔（毫秒）<br/>
     *
     * @param fetchDataInterval
     */
    public void setFetchDataInterval(int fetchDataInterval) {
        this.fetchDataInterval = fetchDataInterval;
    }

    /**
     * 设置内部缓冲区空闲时等待数据的时间间隔（毫秒）<br/>
     *
     * @param interval
     */
    public void setInterval(int interval) {
        this.interval = interval;
    }

    /**
     * 启动<br/>
     */
    public void start() {
        if (null == dataProducer) {
            autoFetchData = false;
        }
        executorService = Executors.newCachedThreadPool();
        BufferQueue bufferQueue = new DefaultBufferQueue(producerCount, consumerCount, bufferSize);
        dataProducerProcessors = new DataProducerProcessor[producerCount];
        for (int i = 0; i < producerCount; i++) {
            dataProducerProcessors[i] = new DataProducerProcessor(dataProducer, i, bufferQueue);
            dataProducerProcessors[i].setAutoFetchData(autoFetchData);
            dataProducerProcessors[i].setFetchDataInterval(fetchDataInterval);
            dataProducerProcessors[i].setInterval(interval);
            if (autoFetchData) {
                executorService.execute(dataProducerProcessors[i]);
            }
        }
        dataConsumerProcessors = new DataConsumerProcessor[consumerCount];
        for (int i = 0; i < consumerCount; i++) {
            dataConsumerProcessors[i] = new DataConsumerProcessor(dataConsumer, i, bufferQueue);
            executorService.execute(dataConsumerProcessors[i]);
            dataConsumerProcessors[i].setInterval(interval);
        }
    }

    /**
     * 非强制停止，等待存量任务处理完成<br/>
     */
    public void stop() {
        stop(false);
    }

    /**
     * 停止<br/>
     */
    public void stop(boolean force) {
        // 必须先停止生产者再停止消费者
        for (int i = 0; i < producerCount; i++) {
            dataProducerProcessors[i].stop(force);
        }
        for (int i = 0; i < consumerCount; i++) {
            dataConsumerProcessors[i].stop(force);
        }
        executorService.shutdown();
    }

    /**
     * 外部触发拉取数据<br/>
     */
    public void fetchData() {
        for (int i = 0; i < producerCount; i++) {
            if (!autoFetchData && null != dataProducer) {
                executorService.execute(dataProducerProcessors[i]);
            }
        }
    }

    /**
     * 外部以指定的生产者的角色主动推送数据<br/>
     *
     * @param producerIndex 指定的生产者
     * @param datas         数据
     * @return 数据被处理则返回true，否则返回false
     */
    public boolean pushData(int producerIndex, Object[] datas) {
        if (null == dataProducer) {
            return dataProducerProcessors[producerIndex].pushData(datas);
        }
        return false;
    }
}
