package cn.com.common.util.concurrent.spmc;

/**
 * 数据消费者<br/>
 * Created by wuliwei on 2015/6/14.
 *
 * @author wuliwei
 */
public interface DataConsumer {
    /**
     * 当有数据到达时，上游将待处理的数据通过handleData方法推送给消费者处理<br/>
     * 注意：<br/>
     * 1、handleData方法的实现应该无状态化，即多线程同时推送数据给同一个实例都能正确处理而不出现并发的问题<br/>
     * 2、handleData方法的实现应该具备幂等性，即任何情况接收到同一笔数据都能根据实际的业务要求进行正确处理<br/>
     *
     * @param data 待处理的数据
     */
    public void handleData(Object data);
}
