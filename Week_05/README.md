[TOC]



## 作业相关

#### 装配Spring Bean方式：

- 通过xml注册

  ```
  <bean name="user" class="org.sign.spring.User"/>
  ```

  

- 通过Java注解注册

  * @Bean
  * @Component
  * @Import

- 通过Java API注册，BeanDefinitionRegister的派生类有AnnotationConfigApplicationContext。

  * 命名：BeanDefinitionRegister#registerBeanDefinition(beanName,beanDefinition)
  * 非命名：BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinition,beanDefinitionRegister)

  ```
  //初始化ApplicationContext
  AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
  applicationContext.register(MyConfigClass.class);
  //构造BeanDefinition
  BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition();
  beanDefinitionBuilder.addPropertyValue("id",1)
          .addPropertyValue("name","SIGN");
  BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
  //命名方式
  applicationContext.registerBeanDefinition("user",beanDefinition);
  //非命名方式
  BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinition,applicationContext);
  //启动
  applicationContext.refresh();
  applicationContext.close();
  ```

  

- 对于一种特殊的Spring Bean：单体对象(生命周期不受ioc容器管理)的注册

  ```
  ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
  beanFactory.registerSingleton("user",new User());
  ```

#### 自定义Spring Xml配置

1. 继承AbstractSingleBeanDefinitionParser实现protected Class<?> getBeanClass(Element element)和protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) 方法
2. 继承NamespaceHandlerSupport重新init方法，并在init方法中registerBeanDefinitionParser("student",自定义Parser);
3. 编写xsd文件
4. 新建spring.handlers文件，并将namespace 与 NamespaceHandler 进行映射
5. 新建spring.schemas进行xsd映射
6. 在spring配置文件在引入自定义的namespace即可

#### 单例模式

- 懒汉模式--线程不安全

  ```
  public class Singleton_01 {
  
      private static Singleton_01 instance;
  
      private Singleton_01() {
      }
  
      public static Singleton_01 getInstance() {
          if (instance != null) {
              return instance;
          }
          instance = new Singleton_01();
          return instance;
      }
  }
  ```

- 懒汉模式-线程安全

  ```
  public class Singleton_02 {
  
      private static Singleton_02 instance;
  
      private Singleton_02() {
      }
  
      public static synchronized Singleton_02 getInstance() {
          if (instance != null) {
              return instance;
          }
          instance = new Singleton_02();
          return instance;
      }
  }
  ```

- 饿汉模式--线程安全

  ```
  public class Singleton_03 {
  
      private static Singleton_03 instance = new Singleton_03();
  
      private Singleton_03() {
      }
  
      public static synchronized Singleton_03 getInstance() {
          return instance;
      }
  }
  ```

- 使用类的内部类--线程安全（推荐）

  ```
  public class Singleton_04 {
  
      private static class SingletonHolder {
          private static Singleton_04 instance = new Singleton_04();
      }
  
      private Singleton_04() {
      }
  
      public static synchronized Singleton_04 getInstance() {
          return SingletonHolder.instance;
      }
  }
  ```

- 双重锁校验--线程安全（推荐）

  ```
  public class Singleton_05 {
  
      private static Singleton_05 instance;
  
      private Singleton_05() {
      }
  
      public static Singleton_05 getInstance() {
          if (instance != null) {
              return instance;
          }
          synchronized (Singleton_05.class) {
              if (instance == null) {
                  instance = new Singleton_05();
              }
          }
          return instance;
      }
  }
  ```

- CAS(AtomicReference)--线程安全

  ```
  public class Singleton_06 {
  
      private static final AtomicReference<Singleton_06> INSTANCE = new AtomicReference<>();
  
      private Singleton_06() {
      }
  
      public static Singleton_06 getInstance() {
          do{
              Singleton_06 instance = INSTANCE.get();
              if(instance != null){
                  return instance;
              }
          }while (!INSTANCE.compareAndSet(null,new Singleton_06()));
          return INSTANCE.get();
      }
  }
  ```

- 枚举单例--线程安全 （Effective Java作者推荐,但是不适合需要的继承场景）

  ```
  public enum  Singleton_07 {
  
      INSTANCE;
  
      public void test(){
          System.out.println("hello world");
      }
  
  	//使用示例
      public static void main(String[] args) {
          Singleton_07.INSTANCE.test();
      }
  }
  ```

#### Spring boot自定义starter

- 编写@Configuration的配置类，在配置类中进行bean装配，可使用@Condition进行条件装配

- 新建spring.factories，关联写的配置类

  ```
  org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  io.sign.www.autoconfiguration.MyAutoConfiguration
  ```

## Spring framework源码学习

#### 依赖注入源码

- 源码重点看的地方：
  * 入口：DefaultListableBeanFactory#resolveDependency
  * 依赖描述符：DependencyDescriptor
  * 自动绑定候选对象处理器：AutowireCandidateResolver
- 源码分析：

![图片](https://uploader.shimo.im/f/OziQrVOzfWg758H2.png!thumbnail?fileGuid=9YYVrgQV9Y9H8pd3)

#### @Autowired字段注入原理源码分析

- 源码入口：AutowiredAnnotationBeanPostProcessor#postProcessProperties
- 源码分析：

![图片](https://uploader.shimo.im/f/1XZ8Hwjj8mMCRSU1.png!thumbnail?fileGuid=9YYVrgQV9Y9H8pd3)

#### Spring Bean生命周期---注册阶段

* 注册阶段主要是调用DefaultListableBeanFactory的registerBeanDefinition方法，对Bean进行是否已经注册过然后是否允许覆盖等校验，把Bean名称、BeanDefinition放到beanDefinitionNames(ArrayList)，beanDefinitionMap(ConcurrentHashMap)中
* 源码分析：

![图片](https://uploader.shimo.im/f/xP3GbfHVrkRE5Hpy.png!thumbnail?fileGuid=9YYVrgQV9Y9H8pd3)

#### Spring Bean生命周期---合并阶段

* 合并阶段主要是递归调用AbstractBeanFactory的getMergedBeanDinition，合并阶段的任务是把可能含有双亲Bean的GenericBeanDefinition，把双亲和当前Bean的属性合并后，返回一个代表没有双亲的RootBeanDefinition，针对当前Bean含有双亲(extends)的情况，获取到双亲BeanDefinition然后构造成RootBeanDefinition，然后结合当前Bean的BeanDefinition进行属性覆盖或添加。
* 源码分析：

![图片](https://uploader.shimo.im/f/PsqcUeaEYqIZeB6C.png!thumbnail?fileGuid=9YYVrgQV9Y9H8pd3)

#### Spring Bean生命周期---实例化前阶段

* 实例化前阶段主要是通过添加BeanPostProcessor的子类InstantiationAwareBeanPostProcessor并实现postProcessBeforeInstantiation方法，来进行代理Bean

  ```
  //编写Spring Bean实例化前处理
  class MyInstantiationAwareBeanPostProcessor implements InstantiationAwareBeanPostProcessor{
    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass,String beanName) throws BeanException{
      if(ObjectUtils.nullSafeEquals("user",beanName) && User.class.equals(beanClass)){
        //如果beanName等于user,而且时User.class类型
        //覆盖掉之前的Bean
        return new User();
      }else{
        //返回空则不做任何处理
        return null;
      }
    }
  }
  //创建Ioc容器代码省略
  beanFactory.addBeanPostProcessor(new MyInstantiationAwareBeanPostProcessor());
  ```

* 源码分析：

![图片](https://uploader.shimo.im/f/U9squTzgfttY0KaJ.png!thumbnail?fileGuid=9YYVrgQV9Y9H8pd3)

#### Spring Bean生命周期---Bean实例化阶段

- 实例化阶段主要是调用AbstractAutowireCapableBeanFactory的createBean->doCreateBean->createBeanInstance方法，其核心就是调用构造方法或工厂方法等获得一个Bean实例Object，如果没有使用函数式接口或工厂方法来创建Bean，则涉及到构造器的选择，如果是无参构造器来进行实例化的话，其本质就是Constructor.newInstance来创建对象，有参的话则涉及依赖注入

- 源码分析：

  ![图片](https://uploader.shimo.im/f/GZEIBp2LvZkwsDn5.png!thumbnail?fileGuid=9YYVrgQV9Y9H8pd3)

- 无参构造器+没有方法重写

  ![图片](https://uploader.shimo.im/f/0u1BOaA1pkubq3h2.png!thumbnail?fileGuid=9YYVrgQV9Y9H8pd3)

  ![图片](https://uploader.shimo.im/f/Fogb7e78Q2ikGdLE.png!thumbnail?fileGuid=9YYVrgQV9Y9H8pd3)

* 构造器参数需要处理情况

  ![图片](https://uploader.shimo.im/f/ol7GT3B5qyWcWWt1.png!thumbnail?fileGuid=9YYVrgQV9Y9H8pd3)

#### Spring Bean生命周期---实例化后阶段

- 实例化后回调主要是实现InstantiationAwareBeanPostProcessor并实现postProcessAfterInstantiation方法，如果return true则不做任何处理，如果return false使用方法内修改后的Bean

- 示例代码：

  ```
  //编写Spring Bean实例化后处理
  class MyInstantiationAwareBeanPostProcessor implements InstantiationAwareBeanPostProcessor{
    @Override
    public boolean postProcessAfterInstantiation(Class<?> beanClass,String beanName) throws BeanException{
      if(ObjectUtils.nullSafeEquals("user",beanName) && User.class.equals(beanClass)){
        //如果beanName等于user,而且时User.class类型
        //return false代表对象不允许属性赋值即xml元信息配置的那些
        //还可以使用下面的代码进行替换
        User user = (User)bean;
        user.name = "sign";
        return false;
      }else{
        //返回true则不做任何处理
        return true;
      }
    }
  }
  //创建Ioc容器代码省略
  beanFactory.addBeanPostProcessor(new MyInstantiationAwareBeanPostProcessor());
  ```

- 源码分析：

![图片](https://uploader.shimo.im/f/6SRmHKZk8DlBaXnj.png!thumbnail?fileGuid=9YYVrgQV9Y9H8pd3)

#### Spring Bean生命周期---属性赋值前阶段

- 通过实现InstantiationAwareBeanPostProcessor接口的postProcessProperties方法(spring5.1)或者postProcessPropertyValues方法，如果return null则不做任何处理

- 示例代码：

  ```
  class MyInstantiationAwareBeanPostProcessor implements InstantiationAwareBeanPostProcessor{
    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs,Object bean,String beanName){
      if(ObjectUtils.nullSafeEquals("user",beanName) && User.class.equals(bean.getClass())){
        //如果beanName等于user,而且时User.class类型
        final MutablePropertyValues propertyValues;
        if(pvs instanceof MutablePropertyValues){
          propertyValues = (MutablePropertyValues)pvs;
        }else{
          propertyValues = new MutablePropertyValues();
        }
        //演示进行属性替换
        if(propertyValues.contains("name")){
          propertyValues.removePropertyValue("name");
          propertyValues.addPropertyValue("name","sign");
        }
        return propertyValues;
      } 
      //return null则正常属性赋值
      return null;
    }
  }
  //创建Ioc容器代码省略
  beanFactory.addBeanPostProcessor(new MyInstantiationAwareBeanPostProcessor());
  ```

#### Spring Bean生命周期---属性赋值阶段

- 源码入口：AbstractAutowireCapableBeanFactory#populateBean
- 源码分析：

![图片](https://uploader.shimo.im/f/WMia862eh2sCvGD1.png!thumbnail?fileGuid=9YYVrgQV9Y9H8pd3)

#### Spring Bean生命周期---Aware接口回调阶段

- 这里的Aware接口是指BeanNameAware、BeanClassLoaderAware、BeanFactoryAware，其实Aware接口还有很多，但是它们不属于Spring Bean生命周期，像EnvironmentAware是属于ApplicationContext生命周期

- Aware接口回调顺序是BeanNameAware->BeanClassLoaderAware->BeanFactoryAware，Aware依赖注入示例：

  ```
  public cliass User implements BeanNameAware{
    private int id;
    private String name;
    //getter、setter
    //Aware依赖注入
    private String beanName;
    @Override
    public void setBeanName(String beanName){
      this.beanName = beanName;
    }
  }
  ```

- 源码分析：AbstractAutowireCapableBeanFactory#invokeAwareMethods

![图片](https://uploader.shimo.im/f/zkhWVmN9aaaoWPI4.png!thumbnail?fileGuid=9YYVrgQV9Y9H8pd3)

#### Spring Bean生命周期---初始化前阶段

- 通过实现BeanPostProcessor的postProcessBeforeInitialization方法

- 代码示例：

  ```
  class MyInstantiationAwareBeanPostProcessor implements InstantiationAwareBeanPostProcessor{
    @Override
    public PropertyValues postProcessBeforeInitialization(Object bean, String beanName) throws BeansException{
      if(ObjectUtils.nullSafeEquals("user",beanName) && User.class.equals(bean.getClass())){
        //如果beanName等于user,而且时User.class类型
        //演示初始化前对Bean进行操作
        User user = (User)bean;
        user.setName("sign");
      } 
      return bean;
    }
  }
  //创建Ioc容器代码省略
  beanFactory.addBeanPostProcessor(new MyInstantiationAwareBeanPostProcessor())
  ```

- 源码分析：AbstractAutowireCapableBeanFactory#initializeBean

![图片](https://uploader.shimo.im/f/SKaz2DhVNtYH2Nou.png!thumbnail?fileGuid=9YYVrgQV9Y9H8pd3)

#### Spring Bean生命周期---初始化阶段

Bean初始化阶段主要涉及到3个回调方法：

* @PostConstruct
* InitializingBean接口的afterPropertiesSet()方法
* <bean init-method="xxx"/>的自定义初始化方法

```plain
public class User implements InitializingBean{
  privite int id;
  private String name;
  
  @PostConstruct
  public void init(){}
  
  @Override
  public void afterPropertiesSet() throws Exception{
    
  }
  
  public void xxx(){}
  //getter、setter省略
}
/**因为CommonAnnotationBeanPostProcessor在BeanFactory里面是非必须的，因此我们需要手动引入才能回调@PostConstruct，如果是ApplicationContext则不用**//
beanFactory.addBeanPostProcessor(new CommonAnnotationBeanPostProcessor());
```

* 它们的回调顺序是：@PostConstruct->afterPropertiesSet->init-method
* 源码分析：AbstractAutowireCapableBeanFactory#initializeBean

![图片](https://uploader.shimo.im/f/Ha6ZZwqDtIEdVk7I.png!thumbnail?fileGuid=9YYVrgQV9Y9H8pd3)

#### Spring Bean生命周期---初始化后阶段

- 通过实现BeanPostProcessor的postProcessAfterInitialization方法

- 代码示例：

  ```
  class MyInstantiationAwareBeanPostProcessor implements InstantiationAwareBeanPostProcessor{
    @Override
    public PropertyValues postProcessAfterInitialization(Object bean, String beanName) throws BeansException{
      if(ObjectUtils.nullSafeEquals("user",beanName) && User.class.equals(bean.getClass())){
        //如果beanName等于user,而且时User.class类型
        //演示初始化前对Bean进行操作
        User user = (User)bean;
        user.setName("sign");
      } 
      return bean;
    }
  }
  //创建Ioc容器代码省略
  beanFactory.addBeanPostProcessor(new MyInstantiationAwareBeanPostProcessor()
  ```

#### Spring Bean生命周期---初始化完成阶段

- 初始化完成阶段主要涉及回调SmartInitializingSingleton接口的afterSingletonsInstantiated方法，需要spring4.1及以上版本，如果在BeanFactory里面需要自己手动执行回调方法，在ApplicationContext则不需要

- 代码示例：

  ```
  public class User implements SmartInitializingSingleton{
    private int id;
    private String name;
    
    @Override
    public void afterSingletonsInstantiated(){
      //演示进行属性替换
      this.name = "sign";
    }
  }
  //创建IoC容器代码忽略
  //preInstantiateSingletons方法里面回调了afterSingletonsInstantiated
  //在BeanFactory里面需要自己手动调用
  //而在ApplicationContext则不需要，因为在applicationContext的refresh方法->
  //finishRefresh方法里面调用了preInstantiateSingletons
  beanFactory.preInstantiateSingletons();
  ```

#### Spring Bean生命周期---销毁前阶段

- 销毁前阶段主要是回调@PreDestroy标注的方法，@PreDestroy的实现是DestructionAwareBeanPostProcessor接口的postProcessBeforeDestruction方法

- 代码示例：

  ```
  //第一种方式
  public class User{
    @PreDestory
    public void preDestory(){}
  }
  //第二种方式
  public class MyDestructionAwareBeanPostProcessor implements DestructionAwareBeanPostProcessor{
    @Override
    public void postProcessBeforeDestruction(Object bean,String beanName){
      
    }
  }
  beanFactory.addBeanPostProcessor(new MyDestructionAwareBeanPostProcessor());
  ```

#### Spring Bean生命周期---销毁阶段

销毁阶段主要是回调两个方法：

* DisposableBean接口的destroy方法

* <bean destory-method="xxx"/>的自定义销毁方法

* 代码示例：

  ```
  public class User implements DisPosableBean{
    privite int id;
    private String name;
    
    @Override
    public void destory() throws Exception{
      
    }
    
    public void xxx(){}
    //getter、setter省略
  }
  ```

- 源码分析：DisposableBeanAdapter#destroy

![图片](https://uploader.shimo.im/f/TaNHhn7yPoipT4fH.png!thumbnail?fileGuid=9YYVrgQV9Y9H8pd3)