package cn.com.common.util.concurrent.spmc.original;

import cn.com.common.util.concurrent.spmc.DataConsumer;
import cn.com.common.util.concurrent.spmc.DataProducer;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wuliwei on 2015/6/20.
 *
 * @author wuliwei
 */
public class OriginalSpmcTest {

    @Test
    public void testTps() throws Exception {
        long st = System.currentTimeMillis();
        final int totalCount = 100000000, pageSize = 1000;
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
        OriginalSpmc spmc = new OriginalSpmc();
        spmc.setDataProducer(dataProducer);
        spmc.setDataConsumer(dataConsumer);
        spmc.setProducerCount(producerCount);
        spmc.setConsumerCount(consumerCount);
        spmc.setBufferSize(bufferSize);
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

    @Test
    @Ignore
    public void testCorrect() throws Exception {
        int totalCount = 10000, pageSize = 1000;
        int producerCount = 1, consumerCount = 1, bufferSize = 128;
        AtomicInteger proDataCount = new AtomicInteger(0);
        AtomicInteger conDataCount = new AtomicInteger(0);
        // Map必须是同步的
        // 虽然代码里对map的操作都是是先put后get，而且没有remove操作，
        // 但是有put操作，会导致map重新hash进而导致get操作无法获取到本来已存在的key
        Map<String, AtomicInteger> dataMap = Collections.synchronizedMap(new HashMap<String, AtomicInteger>());
        Map<String, AtomicInteger> threadMap = Collections.synchronizedMap(new HashMap<String, AtomicInteger>());

        test(totalCount, pageSize, producerCount, consumerCount, bufferSize, proDataCount, conDataCount, dataMap,
                threadMap, false, true);

        totalCount = 10000;
        pageSize = 1000;
        producerCount = 1;
        consumerCount = 2;
        bufferSize = 128;
        proDataCount = new AtomicInteger(0);
        conDataCount = new AtomicInteger(0);
        dataMap = Collections.synchronizedMap(new HashMap<String, AtomicInteger>());
        threadMap = Collections.synchronizedMap(new HashMap<String, AtomicInteger>());

        test(totalCount, pageSize, producerCount, consumerCount, bufferSize, proDataCount, conDataCount, dataMap,
                threadMap, false, true);

        totalCount = 10000;
        pageSize = 1000;
        producerCount = 2;
        consumerCount = 2;
        bufferSize = 128;
        proDataCount = new AtomicInteger(0);
        conDataCount = new AtomicInteger(0);
        dataMap = Collections.synchronizedMap(new HashMap<String, AtomicInteger>());
        threadMap = Collections.synchronizedMap(new HashMap<String, AtomicInteger>());

        test(totalCount, pageSize, producerCount, consumerCount, bufferSize, proDataCount, conDataCount, dataMap,
                threadMap, false, true);

        totalCount = 10000;
        pageSize = 1000;
        producerCount = 2;
        consumerCount = 2;
        bufferSize = 128;
        proDataCount = new AtomicInteger(0);
        conDataCount = new AtomicInteger(0);
        dataMap = Collections.synchronizedMap(new HashMap<String, AtomicInteger>());
        threadMap = Collections.synchronizedMap(new HashMap<String, AtomicInteger>());

        test(totalCount, pageSize, producerCount, consumerCount, bufferSize, proDataCount, conDataCount, dataMap,
                threadMap, false, false);

        totalCount = 10000;
        pageSize = 1000;
        producerCount = 2;
        consumerCount = 2;
        bufferSize = 128;
        proDataCount = new AtomicInteger(0);
        conDataCount = new AtomicInteger(0);
        dataMap = Collections.synchronizedMap(new HashMap<String, AtomicInteger>());
        threadMap = Collections.synchronizedMap(new HashMap<String, AtomicInteger>());

        try {
            // 强制停止会生产数量与消费数量不一致，属于预期结果
            test(totalCount, pageSize, producerCount, consumerCount, bufferSize, proDataCount, conDataCount, dataMap,
                    threadMap, true, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void test(final int totalCount, final int pageSize, int producerCount, int consumerCount, int bufferSize,
                      final AtomicInteger proDataCount, final AtomicInteger conDataCount,
                      final Map<String, AtomicInteger> dataMap, final Map<String, AtomicInteger> threadMap,
                      boolean force, boolean autoFetch) {
        long st = System.currentTimeMillis();
        final Random r = new Random(System.currentTimeMillis());

        DataProducer dataProducer = new DataProducer() {
            public Object[] fetchData() {
                try {
                    Object[] datas = new Object[r.nextInt(pageSize + 1)];
                    int index = 0;
                    String key = "pro-" + Thread.currentThread().getName();
                    if (!threadMap.containsKey(key)) {
                        threadMap.put(key, new AtomicInteger(0));
                    }
                    for (int i = 0; i < datas.length; i++) {
                        index = proDataCount.incrementAndGet();
                        datas[i] = new int[]{r.nextInt(1000), r.nextInt(1000), index};
                        dataMap.put(String.valueOf(index), new AtomicInteger(0));
                        threadMap.get(key).incrementAndGet();
                    }
                    return datas;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return new Object[0];
            }
        };
        DataConsumer dataConsumer = new DataConsumer() {
            public void handleData(Object data) {
                try {
                    int[] tempData = (int[]) data;
                    String s = (tempData[2] + "\t" + Thread.currentThread().getName() + ", " + tempData[0] + " + "
                            + tempData[1] + " = " + (tempData[0] + tempData[1]));
                    //System.out.println(s);
                    conDataCount.incrementAndGet();
                    dataMap.get(String.valueOf(tempData[2])).incrementAndGet();
                    String key = "con-" + Thread.currentThread().getName();
                    if (!threadMap.containsKey(key)) {
                        threadMap.put(key, new AtomicInteger(0));
                    }
                    threadMap.get(key).incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        OriginalSpmc mpmc = new OriginalSpmc();
        mpmc.setDataProducer(dataProducer);
        mpmc.setDataConsumer(dataConsumer);
        mpmc.setProducerCount(producerCount);
        mpmc.setConsumerCount(consumerCount);
        mpmc.setBufferSize(bufferSize);
        mpmc.setAutoFetchData(autoFetch);
        mpmc.start();
        do {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
            if (!autoFetch) {
                mpmc.fetchData();
            }
        } while (proDataCount.get() < totalCount);
        if (force) {
            mpmc.stop(force);
        } else {
            mpmc.stop();
        }
        do {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        } while (proDataCount.get() != conDataCount.get() && !force);

        long et = System.currentTimeMillis();
        long useTime = et - st;
        System.out.println("---------------------------------");
        System.out.println(
                "producerCount " + producerCount + ", consumerCount " + consumerCount + ", bufferSize " + bufferSize);
        System.out.println(
                "produce count " + proDataCount.get() + ", consume count " + conDataCount.get() + ", total time (ms) "
                        + useTime + ", tps " + (int) (
                        conDataCount.get() * 1.0D / useTime * 1000));
        long proCount = 0, conCount = 0;
        for (Map.Entry<String, AtomicInteger> etr : threadMap.entrySet()) {
            if (etr.getKey().startsWith("pro")) {
                System.out.println(etr.getKey() + " produce count " + etr.getValue().get());
                proCount += etr.getValue().get();
            } else if (etr.getKey().startsWith("con")) {
                System.out.println(etr.getKey() + " consume count " + etr.getValue().get());
                conCount += etr.getValue().get();
            }
        }
        Assert.assertTrue("produce count not equals error", proCount == proDataCount.get());
        Assert.assertTrue("consume count not equals error", conCount == conDataCount.get());
        if (!force) {
            Assert.assertTrue("handle count not equals error",
                    dataMap.size() == proDataCount.get() && dataMap.size() == conDataCount.get());
            for (Map.Entry<String, AtomicInteger> etr : dataMap.entrySet()) {
                Assert.assertTrue(etr.getKey() + " consume " + etr.getValue().get() + " times error",
                        etr.getValue().get() == 1);
            }
        }
    }
}