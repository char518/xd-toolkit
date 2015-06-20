package cn.com.common.util.concurrent.spmc.original;

import cn.com.common.util.concurrent.spmc.DataProducer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 生产者处理器
 * Created by wuliwei on 2015/6/18.
 *
 * @author wuliwei
 */
class DataProducerProcessor implements DataProducer, Runnable {
    private DataProducer dataProducer;
    private int producerIndex;
    private BufferQueue bufferQueue;
    private int consumerCount;
    private int bufferSize;
    private boolean autoFetchData = true;
    private int interval = 0;
    private Object[] datas;
    private int totalCount;
    private int dataCount;
    private int curConsumerIndex = 0;
    private int curBufferIndex = 0;
    private boolean sleep = false;
    private AtomicBoolean run = new AtomicBoolean(false);
    private AtomicBoolean stop = new AtomicBoolean(false);
    private AtomicBoolean forceStop = new AtomicBoolean(false);

    DataProducerProcessor(DataProducer dataProducer, int producerIndex, BufferQueue bufferQueue) {
        this.dataProducer = dataProducer;
        this.producerIndex = producerIndex;
        this.bufferQueue = bufferQueue;
        consumerCount = bufferQueue.getConsumerCount();
        bufferSize = bufferQueue.getBufferSize();
    }

    public void setAutoFetchData(boolean autoFetchData) {
        this.autoFetchData = autoFetchData;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public Object[] fetchData() {
        try {
            datas = dataProducer.fetchData();
        } catch (Throwable t) {
            datas = new Object[0];
        }
        totalCount = datas.length;
        dataCount = 0;
        return datas;
    }

    public void run() {
        // 同时只允许一个线程执行
        if (!run.compareAndSet(false, true)) {
            return;
        }
        while (!stop.get()) {
            sleep = true;
            process();
            if (!autoFetchData) {
                break;
            }
            if (sleep) {
                // 从外部拉取数据休眠
                sleep(interval);
            }
        }
        run.set(false);
    }

    public void stop(boolean force) {
        stop.set(true);
        forceStop.set(force);
        endOffer();
    }

    private void process() {
        fetchData();
        while (dataCount < totalCount) {
            sleep = true;
            curConsumerIndex = curConsumerIndex % consumerCount;
            for (; curConsumerIndex < consumerCount && dataCount < totalCount; curConsumerIndex++) {
                if (bufferQueue.tryOffer(producerIndex, curConsumerIndex)) {
                    curBufferIndex = 0;
                    for (; curBufferIndex < bufferSize && dataCount < totalCount; curBufferIndex++) {
                        bufferQueue.offer(producerIndex, curConsumerIndex, curBufferIndex, datas[dataCount]);
                        dataCount++;
                    }
                    bufferQueue.releaseOffer(producerIndex, curConsumerIndex, curBufferIndex);
                    sleep = false;
                }
            }
            if (sleep) {
                // 向队列填充数据休眠
                sleep(0);
            }
        }
    }

    private void endOffer() {
        if (forceStop.get()) {
            bufferQueue.endBuffer(producerIndex);
            return;
        } else {
            while (true) {
                if (!run.get()) {
                    // 确保无执行任务
                    bufferQueue.endBuffer(producerIndex);
                    return;
                }
                sleep(0);
            }
        }
    }

    private void sleep(long time) {
        try {
            if (time > 0) {
                Thread.sleep(time);
                return;
            }
            Thread.yield();
        } catch (Exception e) {
        }
    }
}
