# 作业1 #
**题目：使用 GCLogAnalysis.java 自己演练一遍串行/并行/CMS/G1的案例，使用压测工具（wrk 或 sb），演练 gateway-server-0.0.1-SNAPSHOT.jar 示例，写一段对于不同GC的总结**

总结：

**串行GC：**

串行GC对新生代使用mark-copy(标记复制)算法，对老年代使用mark-sweep-compact(标记-清除-整理)。由于串行GC是单线程线程收集，从实验数据也可以看出Young GC的min/max time比其他GC要高，堆变大，GC的max time就会上升，所以SerialGC曾经作为javaME的默认垃圾收集器。新生代和老年代收集过程都需要STW

优点：简单高效，对单核CPU非常合适，适合几百M堆内存的jvm

缺点：多核CPU条件下，不能充分利用系统资源，在堆大的情况下会造成长时间STW

**并行GC**


java 5/6/7/8 server模式下的默认垃圾收集器，在新生代使用标记-复制(mark-copy)算法，在老年代使用标记-清除-整理(mark-sweep-compact)算法，-XX:ParallelGCThreads=N来指定GC线程数，默认值为CPU核心数。在多线程收集下收集时间比单线程的SerialGC短，没有进行垃圾回收的时候，不会消耗任何系统资源。但是年轻代和老年代垃圾回收都会触发STW。

优点：吞吐量优先(运行业务线程所占的比重)，适合CPU密集型应用

缺点：因为垃圾收集全线都需要STW，因此在堆很大的情况下，STW时间会过长，对低延迟敏感的应用将不能忍受

**CMS GC**

在年轻使用mark-copy(标记-复制)算法，对老年代使用并发mark-sweep(标记-清除)算法，CMS的设计目的是避免老年代垃圾收集时出现长时间的卡顿，其工作大部分时间可以与业务线程一起并发执行，并且不对老年代进行整理，而是使用空闲列表(free-lists)来管理内存空间的回收。CMS默认使用的并发线程数为CPU核心数的1/4。

优点：相比ParallelGC FullGC减少了停顿时间。

缺点：堆太大还是会有长时间的STW，一般4G以下的堆使用。而且相比并行GC，降低了吞吐量。存在失败兜底编程串行GC的风险，导致垃圾收集时间急剧上升。标记-清除算法容易导致内存碎片，碎片过多会导致在老年代还有很多剩余空间时大对象无法分配，不得不提前触发FullGC。


**G1 GC**

G1的全称是Garbage-First，意为垃圾优先，哪一块的垃圾最多就优先清理它。G1把堆划分为了多个(通常是2048个)可以存放对象的小堆(regions)。这样划分后，使得G1不必每次都去收集整个堆空间，而是以增量的方式进行处理：每次只处理一部分内存块，称为此次GC的回收集(collection set)。每次GC暂停都会收集所有年轻代内存块，但一般只收集部分老年代内存块。

优点：新生代收集和老年代收集大部分时间线都可以并发处理，而且把堆划分为多个小块，比CMS进一步降低了STW。还建立可预测的停顿时间模型，-XX: MaxGCPauseMills，G1会尽量保证控制在这个范围内，不会产生内存碎片

缺点：一般堆大小4G以上优势才明显，但是堆太大STW还是难以预测

在压测过程中，G1确实表现优秀，rps在3千左右，与其他垃圾收集器相比，最大响应时长降低，平均响应时长也降低

**助教总结：我的经验，能用G1就用G1, 99%的系统能减少烦恼。只要内存够系统运行，G1的表现远超其他旧的垃圾收集器。JDK11，CPU超过8个以上，能用ZGC就用ZGC,除非CPU特别高负载，没有资源给ZGC才选G1。**

堆增大,GC时间增大，以SerialGC YoungGC为例:

|堆大小|最大耗时|平均耗时|
|--|--|--|
|512m|30ms|17.5ms|
|1g|80ms|31.5ms|
|2g|80ms|61.7ms|
|4g|100ms|86.7ms|

串行->并行->CMS->G1，STW时长变短，以1g堆大小为例：

|垃圾收集器|总STW时长|
|--|--|
|SerialGC|460ms|
|ParallelGC|440ms|
|ConcMarkSweepGC|370ms|
|G1GC|200ms|

串行->并行->CMS->G1，整体延迟在降低，以4g堆为例：
|垃圾收集器|平均响应时长|
|--|--|
|SerialGC|1.9ms|
|ParallelGC|1.6ms|
|ConcMarkSweepGC|1ms|
|G1GC|1.2ms|

# 实验过程数据 #

**使用 GCLogAnalysis.java 自己演练一遍串行/并行/CMS/G1的案例**

**SerialGC**

---

实验1

java -XX:+UseSerialGC -Xms512m -Xmx512m -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis

实验结果：总共发生了22次GC，其中MinorGC 12次，FullGC 10次。MinorGC的最低和最高耗时分别为10ms和30ms，平均耗时17.5ms。FullGC的最低和最高耗时分别为30ms和50ms，平均耗时41ms。Old区已使用99%且频繁FullGC，差点发生OOM。GC造成总STW 620ms。多次运行取中位数：共生成对象次数:11214。

---

实验2

java -XX:+UseSerialGC -Xms1g -Xmx1g -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis

实验结果：总共发生了14次GC，其中MinorGC 13次，FullGC 1次。MinorGC的最低和最高耗时分别为10ms和80ms，平均耗时31.5ms。FullGC耗时为50ms。GC造成总STW 460ms。多次运行取中位数：共生成对象次数:15729。

---

实验3

java -XX:+UseSerialGC -Xms2g -Xmx2g -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis

实验结果：总共发生了6次GC，其中MinorGC发生了6次，没有发生FullGC。MinorGC的最低和最高耗时分别为50ms和80ms，平均耗时61.7ms。GC造成总STW 370ms。多次运行取中位数：共生成对象次数:15716。

---

实验4

java -XX:+UseSerialGC -Xms4g -Xmx4g -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis

实验结果：总共发生了3次GC，其中MinorGC发生了3次，没有发生FullGC。MinorGC的最低和最高耗时分别为80ms和100ms，平均耗时86.7ms。GC造成总STW 260ms。多次运行取中位数：共生成对象次数:15761。

---




**ParallelGC**

---

实验1

java -XX:+UseParallelGC -Xms512m -Xmx512m -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis

实验结果：总共发生了41次GC，其中MinorGC 27次，FullGC 14次。MinorGC的最低耗时小于10ms，最高耗时10ms，平均耗时6.67ms。FullGC的最低和最高耗时分别为30ms和50ms，平均耗时39.3ms。GC造成总STW 730ms。多次运行取中位数：共生成对象次数:9291。

---

实验2

java -XX:+UseParallelGC -Xms1g -Xmx1g -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis

实验结果：总共发生了28次GC，其中MinorGC 26次，FullGC 2次。MinorGC的最低和最高耗时分别为10ms和20ms，平均耗时13.5ms。FullGC的最低和最高耗时分别为40ms和50ms，平均耗时45ms。GC造成总STW 440ms，多次运行取中位数：共生成对象次数:16352。

---

实验3

java -XX:+UseParallelGC -Xms2g -Xmx2g -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis

实验结果：总共发生了12次GC，其中MinorGC 12次，没有发生FullGC。MinorGC的最低和最高耗时分别为10ms和30ms，平均耗时24.2ms。GC造成总STW 290ms。多次运行取中位数：共生成对象次数:18273。

---

实验4

java -XX:+UseParallelGC -Xms4g -Xmx4g -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis

实验结果：总共发生了4次GC，其中MinorGC 4次，没有发生FullGC。MinorGC的最低和最高耗时分别为40ms和70ms，平均耗时55ms。GC造成总STW 220ms。多次运行取中位数：共生成对象次数:18504。

---

**ConcMarkSweepGC**

---

实验1

java -XX:+UseConcMarkSweepGC -Xms512m -Xmx512m -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis

实验结果：总共发生了22次GC，其中YoungGC 19次，FullGC 3次。YoungGC的最低和最高耗时分别为10ms和30ms，平均耗时21.5ms。Initial Mark发生了10次，Final Remark发生了7次。GC造成总STW 660ms。多次运行取中位数：共生成对象次数:11676。

---

实验2

java -XX:+UseConcMarkSweepGC -Xms1g -Xmx1g -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis

实验结果：总共发生了16次GC，其中YoungGC 14次，FullGC 2次。YoungGC的最低和最高耗时分别为20ms和40ms，平均耗时26.4ms。Initial Mark发生了2次，Final Remark发生了2次。GC造成总STW 370ms。多次运行取中位数：共生成对象次数:16133。

---

实验3

java -XX:+UseConcMarkSweepGC -Xms2g -Xmx2g -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis

实验结果：总共发生了10次GC，其中YoungGC 10次，没有发生FullGC。YoungGC的最低和最高耗时分别为20ms和50ms，平均耗时35.0ms。GC造成总STW 350ms。多次运行取中位数：共生成对象次数:16280。

---

实验4

java -XX:+UseConcMarkSweepGC -Xms4g -Xmx4g -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis

实验结果：总共发生了10次GC，其中YoungGC 10次，没有发生FullGC。YoungGC的最低和最高耗时分别为20ms和50ms，平均耗时40ms。GC造成总STW 400ms。多次运行取中位数：共生成对象次数:15459。

---

**G1GC**

---

实验1

java -XX:+UseG1GC -Xms512m -Xmx512m -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis

实验结果：总共发生了42次GC，其中YoungGC 39次，FullGC 3次。YoungGC的最低和最高耗时分别为0ms和10ms，平均耗时2.58ms。GC造成总STW 250ms。多次运行取中位数：共生成对象次数:11604。

---

实验2

java -XX:+UseG1GC -Xms1g -Xmx1g -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis

实验结果：总共发生了19次GC，其中YoungGC 13次，FullGC 6次。YoungGC的最低和最高耗时分别为0ms和10ms，平均耗时6.92ms。GC造成总STW 200ms，并发处理时间9.96ms。多次运行取中位数：共生成对象次数:15347。

---

实验3

java -XX:+UseG1GC -Xms2g -Xmx2g -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis

实验结果：总共发生了12次GC，其中YoungGC 12次，没有发生FullGC。YoungGC的最低和最高耗时分别为10ms和30ms，平均耗时13.3ms。GC造成总STW 160ms。多次运行取中位数：共生成对象次数:16141。

---

实验4

java -XX:+UseG1GC -Xms4g -Xmx4g -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis

实验结果：总共发生了12次GC，其中YoungGC 12次，没有发生FullGC。YoungGC的最低和最高耗时分别为10ms和30ms，平均耗时13.3ms。GC造成总STW 160ms。多次运行取中位数：共生成对象次数:16141。

**使用压测工具（wrk 或 sb），演练 gateway-server-0.0.1-SNAPSHOT.jar 示例。**

压测命令：sb -u http://localhost:8088/api/hello -c 40 -N 30 -B false

**SerialGC**

---

实验1

java -XX:+UseSerialGC -Xms512m -Xmx512m -jar gateway-server-0.0.1-SNAPSHOT.jar

测试结果：rps：2628.8，最小响应时长：0ms，最大响应时长：373ms，平均响应时长：2.1ms。

---

实验2：

java -XX:+UseSerialGC -Xms1g -Xmx1g -jar gateway-server-0.0.1-SNAPSHOT.jar

测试结果：rps：2526，最小响应时长：0ms，最大响应时长：335ms，平均响应时长：2.2ms。

---

实验3：

java -XX:+UseSerialGC -Xms2g -Xmx2g -jar gateway-server-0.0.1-SNAPSHOT.jar

测试结果：rps：2622.4，最小响应时长：0ms，最大响应时长：298ms，平均响应时长：1.9ms。

---

实验4

java -XX:+UseSerialGC -Xms4g -Xmx4g -jar gateway-server-0.0.1-SNAPSHOT.jar

测试结果：rps：2758.3，最小响应时长：0ms，最大响应时长：418ms，平均响应时长：1.9ms。

---

**ParallelGC**

---

实验1：

java -XX:+UseParallelGC -Xms512m -Xmx512m -jar gateway-server-0.0.1-SNAPSHOT.jar

测试结果：rps：2768.3，最小响应时长：0ms，最大响应时长：339ms，平均响应时长：1.8ms。

---

实验2：

java -XX:+UseParallelGC -Xms1g -Xmx1g -jar gateway-server-0.0.1-SNAPSHOT.jar

测试结果：rps：2795.7，最小响应时长：0ms，最大响应时长：312ms，平均响应时长：1.4ms。

---

实验3：

java -XX:+UseParallelGC -Xms2g -Xmx2g -jar gateway-server-0.0.1-SNAPSHOT.jar

测试结果：rps：2717.5，最小响应时长：0ms，最大响应时长：327ms，平均响应时长：1.6ms。

---

实验4：

java -XX:+UseParallelGC -Xms4g -Xmx4g -jar gateway-server-0.0.1-SNAPSHOT.jar

测试结果：2665.8rps：，最小响应时长：0ms，最大响应时长：315ms，平均响应时长：1.6ms。

---

**ConcMarkSweepGC**

---

实验1：

java -XX:+UseConcMarkSweepGC -Xms512m -Xmx512m -jar gateway-server-0.0.1-SNAPSHOT.jar

测试结果：2588.3rps：，最小响应时长：0ms，最大响应时长：275ms，平均响应时长：1.6ms。

---

实验2：

java -XX:+UseConcMarkSweepGC -Xms1g -Xmx1g -jar gateway-server-0.0.1-SNAPSHOT.jar

测试结果：2624rps：，最小响应时长：0ms，最大响应时长：257ms，平均响应时长：1.9ms。

---

实验3：

java -XX:+UseConcMarkSweepGC -Xms2g -Xmx2g -jar gateway-server-0.0.1-SNAPSHOT.jar

测试结果：1684rps：，最小响应时长：0ms，最大响应时长：200ms，平均响应时长：1.6ms。

---

实验4：

java -XX:+UseConcMarkSweepGC -Xms4g -Xmx4g -jar gateway-server-0.0.1-SNAPSHOT.jar

测试结果：3225rps：，最小响应时长：0ms，最大响应时长：103ms，平均响应时长：1ms。

---

**G1GC**

---

实验1：

java -XX:+UseG1GC -Xms512m -Xmx512m -jar gateway-server-0.0.1-SNAPSHOT.jar

测试结果：2982rps：，最小响应时长：0ms，最大响应时长：88ms，平均响应时长：1.3ms。

---

实验2：

java -XX:+UseG1GC -Xms1g -Xmx1g -jar gateway-server-0.0.1-SNAPSHOT.jar

测试结果：3299.8rps：，最小响应时长：0ms，最大响应时长：80ms，平均响应时长：0.8ms。

---

实验3：

java -XX:+UseG1GC -Xms2g -Xmx2g -jar gateway-server-0.0.1-SNAPSHOT.jar

测试结果：rps：3108.5，最小响应时长：0ms，最大响应时长：93ms，平均响应时长：1.1ms。

---

实验4：

java -XX:+UseG1GC -Xms4g -Xmx4g -jar gateway-server-0.0.1-SNAPSHOT.jar

测试结果：rps：3081.2，最小响应时长：0ms，最大响应时长：121ms，平均响应时长：1.2ms。




