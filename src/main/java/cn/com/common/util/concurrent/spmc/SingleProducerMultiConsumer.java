package cn.com.common.util.concurrent.spmc;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 单生产者多消费者组件<br/>
 * 使用指南：<br/>
 * 1、创建该类的实例<br/>
 * 2、设置生产者实例、消费者实例<br/>
 * 3、设置线程池大小、队列大小（必须是2的幂次方）、队列等待策略、<br/>
 * 内部自动拉取数据、自动拉取数据间隔时间（毫秒）；该过程非必选可跳过<br/>
 * 4、启动<br/>
 * 5、外部定时触发拉取数据；当内部自动拉取数据时，该过程非必选可跳过<br/>
 * 6、停止；该过程非必选<br/>
 * Created by wuliwei on 2015/6/14.
 *
 * @author wuliwei
 */
public class SingleProducerMultiConsumer {
    /**
     * 最低效的队列等待策略，但其对CPU的消耗最小并且在各种不同部署环境中能提供更加一致的性能表现
     */
    public static final String WAIT_STRATEGY_BLOCKING = "BLOCKING";
    /**
     * 性能表现跟WAIT_STRATEGY_BLOCKING差不多，对CPU的消耗也类似，但其对生产者线程的影响最小。<br/>
     * 适合用于异步日志类似的场景
     */
    public static final String WAIT_STRATEGY_SLEEPING = "SLEEPING";
    /**
     * 性能是最好的队列等待策略，适合用于低延迟的系统。<br/>
     * 在要求极高性能且事件处理线数小于CPU逻辑核心数的场景中，推荐使用此策略
     */
    public static final String WAIT_STRATEGY_YIELDING = "YIELDING";

    private DataProducer dataProducer;
    private DataConsumer dataConsumer;
    private int threadCount = 16;
    private int ringBufferSize = 1024;
    private WaitStrategy waitStrategy;
    private boolean autoFetchData = true;
    private int interval = 10;
    private ExecutorService executor;
    private Disruptor<DataHolder> disruptor;
    private RingBuffer<DataHolder> ringBuffer;
    private WaitStrategy BLOCKING_WAIT = new BlockingWaitStrategy();
    private WaitStrategy SLEEPING_WAIT = new SleepingWaitStrategy();
    private WaitStrategy YIELDING_WAIT = new YieldingWaitStrategy();
    private boolean run;

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
     * 设置线程池大小<br/>
     *
     * @param threadCount
     */
    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    /**
     * 设置队列大小（必须是2的幂次方）<br/>
     *
     * @param ringBufferSize
     */
    public void setRingBufferSize(int ringBufferSize) {
        this.ringBufferSize = ringBufferSize;
    }

    /**
     * 设置队列等待策略<br/>
     *
     * @param waitStrategy
     */
    public void setWaitStrategy(String waitStrategy) {
        if (WAIT_STRATEGY_BLOCKING.equals(waitStrategy)) {
            this.waitStrategy = BLOCKING_WAIT;
        } else if (WAIT_STRATEGY_SLEEPING.equals(waitStrategy)) {
            this.waitStrategy = SLEEPING_WAIT;
        } else if (WAIT_STRATEGY_YIELDING.equals(waitStrategy)) {
            this.waitStrategy = YIELDING_WAIT;
        }
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
        if (null == waitStrategy) {
            this.waitStrategy = YIELDING_WAIT;
        }
        executor = new ThreadPoolExecutor(threadCount, threadCount,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(threadCount));
        disruptor = new Disruptor<DataHolder>(new DataHolderFactory(),
                ringBufferSize, executor, ProducerType.SINGLE,
                waitStrategy);
        disruptor.handleEventsWith(new DataConsumerProxy(dataConsumer));
        disruptor.start();
        ringBuffer = disruptor.getRingBuffer();
        run = true;
        if (autoFetchData) {
            Thread t = new Thread() {
                public void run() {
                    while (run) {
                        try {
                            Thread.sleep(interval);
                            fetchData();
                        } catch (Exception e) {
                        }
                    }
                }
            };
            t.start();
        }
    }

    /**
     * 停止<br/>
     */
    public void stop() {
        run = false;
        disruptor.shutdown();
        executor.shutdown();
    }

    /**
     * 触发拉取数据
     */
    public void fetchData() {
        try {
            List<Object> datas = dataProducer.fetchData();
            if (null != datas) {
                for (Object data : datas) {
                    publish(data);
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * 发布数据
     *
     * @param data
     */
    private void publish(Object data) {
        long sequence = ringBuffer.next();
        try {
            DataHolder holder = ringBuffer.get(sequence);
            holder.setData(data);
        } finally {
            ringBuffer.publish(sequence);
        }
    }
}
