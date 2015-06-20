package cn.com.common.util.concurrent.spmc.original;

import cn.com.common.util.concurrent.spmc.DataConsumer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 消费者处理器
 * Created by wuliwei on 2015/6/18.
 *
 * @author wuliwei
 */
class DataConsumerProcessor implements DataConsumer, Runnable {
    private DataConsumer dataConsumer;
    private int consumerIndex;
    private BufferQueue bufferQueue;
    private int producerCount;
    private int bufferSize;
    private int dataCount;
    private int curProducerIndex;
    private int curBufferIndex;
    private boolean sleep = false;
    private Object[] datas;
    private AtomicBoolean run = new AtomicBoolean(false);
    private AtomicBoolean stop = new AtomicBoolean(false);
    private AtomicBoolean forceStop = new AtomicBoolean(false);

    DataConsumerProcessor(DataConsumer dataConsumer, int consumerIndex, BufferQueue bufferQueue) {
        this.dataConsumer = dataConsumer;
        this.consumerIndex = consumerIndex;
        this.bufferQueue = bufferQueue;
        producerCount = bufferQueue.getProducerCount();
        bufferSize = bufferQueue.getBufferSize();
        datas = new Object[bufferSize];
    }

    public void handleData(Object data) {
        dataConsumer.handleData(data);
    }

    public void run() {
        // 同时只允许一个线程执行
        if (!run.compareAndSet(false, true)) {
            return;
        }
        while (!stop.get() || hasData()) {
            sleep = true;
            process();
            if (sleep) {
                sleep();
            }
        }
        run.set(false);
    }

    public void stop(boolean force) {
        stop.set(true);
        forceStop.set(force);
        endPoll();
    }

    private void process() {
        for (curProducerIndex = 0; curProducerIndex < producerCount; curProducerIndex++) {
            if (0 < (dataCount = bufferQueue.tryPoll(curProducerIndex, consumerIndex))) {
                for (curBufferIndex = 0; curBufferIndex < dataCount; curBufferIndex++) {
                    datas[curBufferIndex] = bufferQueue.poll(curProducerIndex, consumerIndex, curBufferIndex);
                }
                bufferQueue.releasePoll(curProducerIndex, consumerIndex);
                for (curBufferIndex = 0; curBufferIndex < dataCount; curBufferIndex++) {
                    try {
                        handleData(datas[curBufferIndex]);
                    } catch (Throwable t) {
                    }
                }
                sleep = false;
            }
        }
    }

    private boolean hasData() {
        for (curProducerIndex = 0; curProducerIndex < producerCount && !forceStop.get(); curProducerIndex++) {
            if (!bufferQueue.isBufferEnd(curProducerIndex)) {
                return true;
            } else if (0 < (bufferQueue.tryPoll(curProducerIndex, consumerIndex))) {
                return true;
            }
        }
        return false;
    }

    private void endPoll() {
        if (forceStop.get()) {
            return;
        } else {
            while (true) {
                if (!run.get() && !hasData()) {
                    // 确保无执行任务
                    return;
                }
                sleep();
            }
        }
    }

    private void sleep() {
        try {
            Thread.yield();
        } catch (Exception e) {
        }
    }
}
