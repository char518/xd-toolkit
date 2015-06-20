package cn.com.common.util.concurrent.spmc;

/**
 * 数据生产者<br/>
 * Created by wuliwei on 2015/6/14.
 *
 * @author wuliwei
 */
public interface DataProducer {
    /**
     * 所有待处理的数据都由数据生产者输出，数据消费者会通过fetchData方法主动拉取数据<br/>
     * 注意：<br/>
     * 1、fetchData的实现应该对数据的状态进行正确的管理，确保未处理的数据能被返回，避免返回处理中的数据
     * 2、fetchData的实现应该根据实际处理能力控制好返回的数据量，避免返回大批量的数据
     *
     * @return
     */
    public Object[] fetchData();
}
