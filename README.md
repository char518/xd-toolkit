# xd-toolkit
工具集（工作过程中常用到的工具类、工具模块）

## 并发排队等待
    应用场景：
    a、队列人数已达到最大限制数，屏蔽新访问用户加入队列；<br/>
    b、队列中等待用户，等待时间超出最长等待时间，移出队列<br/>
### 工具类
    cn.com.common.util.concurrent.queuing.QueuingProxy
### 测试类（可作为使用示例参考）
    cn.com.common.util.concurrent.queuing.QueuingProxyTest

## 对象转换器
    应用场景：
    a、同类型对象的复制
    b、不同类型对象之间的转换
    c、对象与Map互转
    d、编写这个工具类的原始出发点：在使用MongoDB的时候，发现普通对象与Document的转换不太方便， 
    Document不支持某些对象类型；在这个场景下，使用这个工具类会非常方便
### 工具类
    cn.com.common.util.convert.DefaultConverter
### 测试类（可作为使用示例参考）
    cn.com.common.util.convert.DefaultConverterTest

## EXCEL导出
    应用场景：
    a、数据导出excel格式，而由于数据量大，经常内存溢出或者输出的文件偏大导致下载失败
### 工具类
    cn.com.common.util.excel.impl.ExcelExporter4Xml
### 测试类（可作为使用示例参考）
    cn.com.common.util.excel.ExcelExporterTest
