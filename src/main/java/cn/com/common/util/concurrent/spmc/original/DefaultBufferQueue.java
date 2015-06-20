package cn.com.common.util.concurrent.spmc.original;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 默认的缓冲队列
 * Created by wuliwei on 2015/6/18.
 *
 * @author wuliwei
 */
class DefaultBufferQueue implements BufferQueue {
    private Object[][][] buffers;
    private AtomicInteger[][] semaphores;
    private AtomicBoolean[] ends;
    private int producerCount;
    private int consumerCount;
    private int bufferSize;

    DefaultBufferQueue(int producerCount, int consumerCount, int bufferSize) {
        buffers = new Object[producerCount][consumerCount][bufferSize];
        semaphores = new AtomicInteger[producerCount][consumerCount];
        ends = new AtomicBoolean[producerCount];
        for (int i = 0; i < producerCount; i++) {
            for (int j = 0; j < consumerCount; j++) {
                semaphores[i][j] = new AtomicInteger(0);
            }
            ends[i] = new AtomicBoolean(false);
        }
        this.producerCount = producerCount;
        this.consumerCount = consumerCount;
        this.bufferSize = bufferSize;
    }

    /**
     * 获取生产者数量
     *
     * @return
     */
    public int getProducerCount() {
        return producerCount;
    }

    /**
     * 获取消费者数量
     *
     * @return
     */
    public int getConsumerCount() {
        return consumerCount;
    }

    /**
     * 获取队列大小
     *
     * @return
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * 尝试生产数据
     *
     * @param producerIndex 生产者索引
     * @param consumerIndex 消费者索引
     * @return
     */
    public boolean tryOffer(int producerIndex, int consumerIndex) {
        return 0 == semaphores[producerIndex][consumerIndex].get();
    }

    /**
     * 生产者生产数据
     *
     * @param producerIndex 生产者索引
     * @param consumerIndex 消费者索引
     * @param bufferIndex   缓冲区索引
     * @param data
     */
    public void offer(int producerIndex, int consumerIndex, int bufferIndex, Object data) {
        buffers[producerIndex][consumerIndex][bufferIndex] = data;
    }

    /**
     * 生产者释放生产权
     *
     * @param producerIndex 生产者索引
     * @param consumerIndex 消费者索引
     * @param bufferIndex   缓冲区索引
     */
    public void releaseOffer(int producerIndex, int consumerIndex, int bufferIndex) {
        semaphores[producerIndex][consumerIndex].set(bufferIndex);
    }

    /**
     * 尝试消费数据
     *
     * @param producerIndex 生产者索引
     * @param consumerIndex 消费者索引
     * @return
     */
    public int tryPoll(int producerIndex, int consumerIndex) {
        return semaphores[producerIndex][consumerIndex].get();
    }

    /**
     * 消费者消费数据
     *
     * @param producerIndex 生产者索引
     * @param consumerIndex 消费者索引
     * @param bufferIndex   缓冲区索引
     * @return
     */
    public Object poll(int producerIndex, int consumerIndex, int bufferIndex) {
        return buffers[producerIndex][consumerIndex][bufferIndex];
    }

    /**
     * 消费者释放消费权
     *
     * @param producerIndex 生产者索引
     * @param consumerIndex 消费者索引
     */
    public void releasePoll(int producerIndex, int consumerIndex) {
        semaphores[producerIndex][consumerIndex].set(0);
    }

    /**
     * 指定生产者停产
     *
     * @param producerIndex 生产者索引
     */
    public void endBuffer(int producerIndex) {
        ends[producerIndex].set(true);
    }

    /**
     * 判断生产者是否已经停止生产
     *
     * @param producerIndex 生产者索引
     * @return
     */
    public boolean isBufferEnd(int producerIndex) {
        return ends[producerIndex].get();
    }
}
