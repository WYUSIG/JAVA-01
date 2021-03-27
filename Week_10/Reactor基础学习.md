## Spring-Cloud-Gateway源码系列学习

版本 v2.2.6.RELEASE



>因为Spring-Cloud-Gateway有大量的Reactor的代码，因此必须得恶补一下Reactor的知识



### Flux

>一个0-N个元素的异步序列流，可以使用.subscribe来进行订阅

##### 类结构

>Flux->CorePublisher->Publisher，Flux继承了Publisher，代表它是个是个数据流

##### range

> 该方法有两个参数，start和count，表示构建一个start开始到start+count-1的Flux

![图片](https://uploader.shimo.im/f/I5w2NNBM8fyexfuc.png!thumbnail?fileGuid=pQF8FJKFE0E27sl5)

```java
//源代码方法签名
public static Flux<Integer> range(int start, int count)
//使用例子
public static void rangeTest() {
    Flux<Integer> flux = Flux.range(1, 3); // 1->2->3
    flux.subscribe(i -> System.out.println(i));
}
//输出
1
2
3
```

##### combineLatest

>参数为 T为Object[] , R为Flux 的 Function，sources为数据源，例如Flux，当只有一个数据流时，每个元素到来都会执行比较Function，Object[]只有一个数据；多个数据流时，从第一个数据流最后一个元素开始，每来一个元素就会比较一次，Object[]里面有两个数据，分别是第一个数据流最后一个元素和当前数据

![图片](https://uploader.shimo.im/f/Y16d2BHZMuBmksG0.png!thumbnail?fileGuid=pQF8FJKFE0E27sl5)

```java
//源码方法签名
public static <T, V> Flux<V> combineLatest(Function<Object[], V> combinator, Publisher<? extends T>... sources)
//单数据源使用方法
public static void combineLatestTest() {
    Flux<Integer> ints = Flux.range(1, 3);
    Flux.combineLatest(o -> {
    	return (int)o[0] > 2;
    }, ints).subscribe(i -> System.out.println(i));
}
//单数据源输出结果
false
false
true

//多数据源使用方法
public static void combineLatestTest() {
    Flux<Integer> ints1 = Flux.range(1, 3); // 1->2->3
    Flux<Integer> ints2 = Flux.range(4, 3); // 4->5->6
    Flux.combineLatest(o -> {
        //o[0]:3,o[1]:4
        //o[0]:3,o[1]:5
        //o[0]:3,o[1]:6
    	return (int)o[1] > (int)o[0];
    }, ints1， ints2).subscribe(i -> System.out.println(i));
}
//多数据源输出结果
true
true
true
```

##### concat

>合并多个数据源成一个Flux

![图片](https://uploader.shimo.im/f/JbWvOjH1szakrKsO.png!thumbnail?fileGuid=pQF8FJKFE0E27sl5)

```java
//源代码方法签名
public static <T> Flux<T> concat(Iterable<? extends Publisher<? extends T>> sources)
//使用例子
public static void concatTest() {
    Flux<Integer> ints1 = Flux.range(1, 3); // 1->2->3
    Flux<Integer> ints2 = Flux.range(4, 3); // 4->5->6
    List<Flux<Integer>> sources = new ArrayList<Flux<Integer>>(){{
        add(ints1);
        add(ints2);
    }};
    Flux.concat(sources).subscribe(i -> System.out.println(i)); // 1->2->3->4->5->6
}
```

##### create/push

>适合用于基于自定义的事件触发器，参数是一个Consumer，在consumer里面你将获得一个FluxSink对象，FluxSink有三种事件可供使用next，error和complete，但是create基于多生产者单消费者模型，而push基于单生产者单消费者模型(我也不知道怎么样才是多消费者的代码，测试也发现都是main线程执行)

```java
//源代码方法签名
public static <T> Flux<T> create(Consumer<? super FluxSink<T>> emitter)
//使用例子
Flux<String> bridge = Flux.create(sink -> {
    myEventProcessor.register( 
      new MyEventListener<String>() { 

        public void onDataChunk(List<String> chunk) {
          for(String s : chunk) {
            //onDataChunk事件与FluxSink#next事件对应
            sink.next(s); 
          }
        }

        public void processComplete() {
            //processComplete事件与FluxSink#complete事件对应
            sink.complete(); 
        }
    });
});
```

##### defer

>延迟加载，参数需要传一个Supplier(无参数，1个返回值)，返回值就是数据流，只有订阅了才会去初始化数据源

```java
//源代码方法签名
public static <T> Flux<T> defer(Supplier<? extends Publisher<T>> supplier)
//使用例子
public static void deferTest() {
    Flux flux = Flux.defer(() -> {
    	return Flux.range(1, 3);
    });
    flux.subscribe(i -> System.out.println(i)); //这一步才会去执行Supplier#get代码
}
```

##### empty

>创建一个空的流

```java
//源代码方法签名
public static <T> Flux<T> empty()
//使用例子
Flux flux = Flux.empty();
```

##### error

>传播一个错误

```java
//源代码方法签名
public static <T> Flux<T> error(Throwable error)
//示例代码
public static void errorTest() {
    Flux flux = Flux.error(new RuntimeException("exception"));
    flux.subscribe(i -> System.out.println(i), error -> System.out.println("Error " + error));
}
```

##### just

>将多个相同类型的元素转成流

```java
//方法签名
public static <T> Flux<T> just(T... data)
//使用示例
public static void justTest() {
    Flux flux = Flux.just(1,2,3);
    flux.subscribe(i -> System.out.println(i));
}
```

##### merge

>将多个流合成一个流，元素的顺序根据流的情况，如下图

![图片](https://uploader.shimo.im/f/WHyXtWRVejLmvmDq.png!thumbnail?fileGuid=pQF8FJKFE0E27sl5)

```java
//方法签名
public static <I> Flux<I> merge(Publisher<? extends I>... sources)
//使用示例
public static void mergeTest() throws InterruptedException {
    Flux<Long> longFlux = Flux.interval(Duration.ofMillis(100)).take(3);
    Flux<Long> longFlux2 = Flux.interval(Duration.ofMillis(100)).take(3);
    Flux<Long> longFlux3 = Flux.merge(longFlux,longFlux2);
    longFlux3.subscribe(l -> System.out.println(Thread.currentThread().getName()+":"+l));
    Thread.sleep(1000);
}
//输出
parallel-2:0
parallel-1:0
parallel-2:1
parallel-2:1
parallel-2:2
parallel-1:2
```

##### mergeSequential

>将多个流合成一个流，元素的顺序根据流的情况根据参数流的顺序，一个流完成才到下一个流

![图片](https://uploader.shimo.im/f/cwXvSKa4sqTpfC3Y.png!thumbnail?fileGuid=pQF8FJKFE0E27sl5)

```java
//方法签名
public static <I> Flux<I> mergeSequential(Publisher<? extends I>... sources)
//使用示例
public static void mergeSequentialTest() throws InterruptedException {
    Flux<Long> longFlux = Flux.interval(Duration.ofMillis(100)).take(3);
    Flux<Long> longFlux2 = Flux.interval(Duration.ofMillis(100)).take(3);
    Flux<Long> longFlux3 = Flux.mergeSequential(longFlux,longFlux2);
    longFlux3.subscribe(l -> System.out.println(l));
    Thread.sleep(1000);
}
//输出
0
1
2
0
1
2
```

##### filter

>使用Predicate进行过滤

![图片](https://uploader.shimo.im/f/eNLcnrc2EJn8M67u.png!thumbnail?fileGuid=pQF8FJKFE0E27sl5)

```java
//方法签名
public final Flux<T> filter(Predicate<? super T> p)
//使用示例
public static void filterTest() {
    Flux<Integer> flux = Flux.range(1,3)
            .filter(new Predicate<Integer>() {
                @Override
                public boolean test(Integer integer) {
                    return integer > 2;
                }
            });
    flux.subscribe(i -> System.out.println(i));
}
//输出
3
```

##### next

>取Flux流的第一个元素(Mono)

```java
//方法签名
public final Mono<T> next()
//使用实例
public static void nextTest() {
    Mono<Integer> mono = Flux.range(1, 3).next();
    mono.subscribe(i -> System.out.println(i));
}
//输出
1
```

##### map

>可以对流过的每个元素进行修改

![图片](https://uploader.shimo.im/f/5xeouJXBLf59o0VK.png!thumbnail?fileGuid=pQF8FJKFE0E27sl5)

```java
//方法签名
public final <V> Flux<V> map(Function<? super T, ? extends V> mapper)
//使用示例
public static void mapTest() {
    Flux<Integer> flux = Flux.range(1, 3)
            .map(i -> {
                return ++i;
            });
    flux.subscribe(i -> System.out.println(i));
}
//输出
2
3
4
```

##### flatMap

>同样可以对元素进行修改，但是flatMap 参数 Function的返回值必须是Publisher，也就是元素变成流

![图片](https://uploader.shimo.im/f/XycjTtHvHPaYz7ua.png!thumbnail?fileGuid=pQF8FJKFE0E27sl5)

```java
//方法签名
public final <R> Flux<R> flatMap(Function<? super T, ? extends Publisher<? extends R>> mapper)
//使用示例
public static void flatMapTest() throws InterruptedException {
    Flux<Long> flux = Flux.range(1, 3)
            .flatMap(i -> {
                return Flux.interval(Duration.ofMillis(i)).take(3);
            });
    flux.subscribe(i -> System.out.println(i));
    Thread.sleep(1000);
}
//输出
0
1
2
0
0
1
2
1
2
```

##### flatMapSequential

>可以对元素进行修改，跟flatMap一样，可以将流中的元素转成流，但是flatMapSequential跟原来的流的元素顺序有关

![](https://sign-pic-1.oss-cn-shenzhen.aliyuncs.com/img/1616721855(1).png)

```java
//方法签名
public final <R> Flux<R> flatMapSequential(Function<? super T, ? extends
			Publisher<? extends R>> mapper)
//使用示例
public static void flatMapSequentialTest() throws InterruptedException {
    Flux<Long> flux = Flux.range(1, 3)
            .flatMapSequential(l -> {
                if (l == 2){
                    return Flux.interval(Duration.ofSeconds(5)).take(3);
                } else {
                    return Flux.interval(Duration.ofSeconds(1)).take(3);
                }
            });
    flux.subscribe(l -> System.out.println(l));
    Thread.sleep(20000);
}
//输出
(间隔1秒)
0
(间隔1秒)
1
(间隔1秒)
2
(间隔2秒)
0
(间隔5秒)
1
(间隔5秒)
2
(间隔0秒)
0
(间隔0秒)
1
(间隔0秒)
2
```

