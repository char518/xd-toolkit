package cn.com.common.util.concurrent.queuing;

/**
 * 排队操作的执行结果——包括：本次排队操作的执行结果、当前可用许可的大小、当前等待队列的大小、排队过程中发生的异常 、目标方法的执行返回结果
 *
 * @author wuliwei
 */
public interface QueuingResult {
    /**
     * @return String 本次排队操作的执行结果
     */
    public String getResult();

    /**
     * @return int 当前可用许可的大小
     */
    public int getCurPermits();

    /**
     * @return int 当前等待队列的大小
     */
    public int getCurQueueSize();

    /**
     * @return Throwable 本次排队操作过程中发生的异常，如果整个过程没有异常发生，则该值为null
     */
    public Throwable getThrowable();

    /**
     * @return Object 需要排队的目标方法的执行返回结果，如果该方法返回类型为void，则该值为null；如果返回类型为基础类型int
     * ，则该值为Integer类型；其他返回基础类型同理
     */
    public Object getInvokeReturn();
}
