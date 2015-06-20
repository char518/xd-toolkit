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
    private int interval = 0;
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
     * 设置内部自动拉取数据间隔时间（毫秒）<br/>
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
        executorService = Executors.newCachedThreadPool();
        BufferQueue bufferQueue = new DefaultBufferQueue(producerCount, consumerCount, bufferSize);
        dataProducerProcessors = new DataProducerProcessor[producerCount];
        for (int i = 0; i < producerCount; i++) {
            dataProducerProcessors[i] = new DataProducerProcessor(dataProducer, i, bufferQueue);
            dataProducerProcessors[i].setAutoFetchData(autoFetchData);
            dataProducerProcessors[i].setInterval(interval);
            if (autoFetchData) {
                executorService.execute(dataProducerProcessors[i]);
            }
        }
        dataConsumerProcessors = new DataConsumerProcessor[consumerCount];
        for (int i = 0; i < consumerCount; i++) {
            dataConsumerProcessors[i] = new DataConsumerProcessor(dataConsumer, i, bufferQueue);
            executorService.execute(dataConsumerProcessors[i]);
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
     * 触发拉取数据<br/>
     */
    public void fetchData() {
        try {
            for (int i = 0; i < producerCount; i++) {
                if (!autoFetchData) {
                    executorService.execute(dataProducerProcessors[i]);
                }
            }
        } catch (Exception e) {
        }
    }
}
