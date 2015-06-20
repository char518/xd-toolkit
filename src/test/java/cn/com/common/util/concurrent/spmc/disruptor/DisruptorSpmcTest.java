package cn.com.common.util.concurrent.spmc.disruptor;

import cn.com.common.util.concurrent.spmc.DataConsumer;
import cn.com.common.util.concurrent.spmc.DataProducer;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wuliwei on 2015/6/14.
 *
 * @author wuliwei
 */
public class DisruptorSpmcTest {

    @Test
    public void testTps() throws Exception {
        long st = System.currentTimeMillis();
        final int totalCount = 1000000, pageSize = 1000;
        int producerCount = 1, consumerCount = 1, bufferSize = 512;
        final AtomicInteger proDataCount = new AtomicInteger(0);
        final AtomicInteger conDataCount = new AtomicInteger(0);

        Random r = new Random(System.currentTimeMillis());
        final Object[] datas = new Object[pageSize];
        for (int i = 0; i < datas.length; i++) {
            datas[i] = new int[]{r.nextInt(1000), r.nextInt(1000)};
        }
        DataProducer dataProducer = new DataProducer() {
            public Object[] fetchData() {
                proDataCount.addAndGet(datas.length);
                return datas;
            }
        };
        DataConsumer dataConsumer = new DataConsumer() {
            public void handleData(Object data) {
                conDataCount.incrementAndGet();
            }
        };
        DisruptorSpmc spmc = new DisruptorSpmc();
        spmc.setDataProducer(dataProducer);
        spmc.setDataConsumer(dataConsumer);
        spmc.setThreadCount(consumerCount);
        spmc.setRingBufferSize(bufferSize);
        //spmc.setWaitStrategy(DisruptorSpmc.WAIT_STRATEGY_YIELDING);
        //spmc.setAutoFetchData(true);
        //spmc.setInterval(10);
        //spmc.setSync(false);
        spmc.start();

        do {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        } while (proDataCount.get() < totalCount);
        spmc.stop();
        long et = System.currentTimeMillis();
        long useTime = et - st;
        System.out.println("total count " + conDataCount.get() + ", total time (ms) " + useTime + ", tps " + (int) (
                conDataCount.get() * 1.0D / useTime * 1000));
    }
}