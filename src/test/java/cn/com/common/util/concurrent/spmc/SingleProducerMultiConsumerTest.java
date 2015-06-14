package cn.com.common.util.concurrent.spmc;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Created by wuliwei on 2015/6/14.
 *
 * @author wuliwei
 */
public class SingleProducerMultiConsumerTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testStart() throws Exception {
        final AtomicInteger count = new AtomicInteger(0);
        long st = System.currentTimeMillis();
        SingleProducerMultiConsumer spmc = new SingleProducerMultiConsumer();
        spmc.setDataProducer(new DataProducer() {
            public List<Object> fetchData() {
                List<Object> datas = new ArrayList<Object>();
                Random r = new Random(System.currentTimeMillis());
                for (int i = 0; i < 10000; i++) {
                    datas.add(new int[]{r.nextInt(1000), r.nextInt(1000)});
                }
                return datas;
            }
        });
        spmc.setDataConsumer(new DataConsumer() {
            public void handleData(Object data) {
                int[] arr = (int[]) data;
                System.out.println(count.incrementAndGet() + "\t" + Thread.currentThread().getName() + ", " + arr[0] + " + " + arr[1] + " = " + (arr[0] + arr[1]));
            }
        });
        spmc.setThreadCount(16);
        spmc.setRingBufferSize(1024);
        spmc.setWaitStrategy(SingleProducerMultiConsumer.WAIT_STRATEGY_YIELDING);
        spmc.setAutoFetchData(true);
        spmc.setInterval(1);
        spmc.start();
        try {
            Thread.sleep(10 * 1000);
        } catch (Exception e) {
        }
        spmc.stop();
        try {
            Thread.sleep(2 * 1000);
        } catch (Exception e) {
        }
        long et = System.currentTimeMillis();
        System.out.println("handle count " + count.get() + ", used time " + (et - st));
    }
}