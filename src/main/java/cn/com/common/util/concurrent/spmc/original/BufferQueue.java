package cn.com.common.util.concurrent.spmc.original;

/**
 * 缓冲队列
 * Created by wuliwei on 2015/6/18.
 *
 * @author wuliwei
 */
interface BufferQueue {
    /**
     * 获取生产者数量
     *
     * @return
     */
    int getProducerCount();

    /**
     * 获取消费者数量
     *
     * @return
     */
    int getConsumerCount();

    /**
     * 获取队列大小
     *
     * @return
     */
    int getBufferSize();

    /**
     * 尝试生产数据
     *
     * @param producerIndex 生产者索引
     * @param consumerIndex 消费者索引
     * @return
     */
    boolean tryOffer(int producerIndex, int consumerIndex);

    /**
     * 生产者生产数据
     *
     * @param producerIndex 生产者索引
     * @param consumerIndex 消费者索引
     * @param bufferIndex   缓冲区索引
     * @param data
     */
    void offer(int producerIndex, int consumerIndex, int bufferIndex, Object data);

    /**
     * 生产者释放生产权
     *
     * @param producerIndex 生产者索引
     * @param consumerIndex 消费者索引
     * @param bufferIndex   缓冲区索引
     */
    void releaseOffer(int producerIndex, int consumerIndex, int bufferIndex);

    /**
     * 尝试消费数据
     *
     * @param producerIndex 生产者索引
     * @param consumerIndex 消费者索引
     * @return
     */
    int tryPoll(int producerIndex, int consumerIndex);

    /**
     * 消费者消费数据
     *
     * @param producerIndex 生产者索引
     * @param consumerIndex 消费者索引
     * @param bufferIndex   缓冲区索引
     * @return
     */
    Object poll(int producerIndex, int consumerIndex, int bufferIndex);

    /**
     * 消费者释放消费权
     *
     * @param producerIndex 生产者索引
     * @param consumerIndex 消费者索引
     */
    void releasePoll(int producerIndex, int consumerIndex);

    /**
     * 指定生产者停产
     *
     * @param producerIndex 生产者索引
     */
    void endBuffer(int producerIndex);

    /**
     * 判断生产者是否已经停止生产
     *
     * @param producerIndex 生产者索引
     * @return
     */
    boolean isBufferEnd(int producerIndex);
}
