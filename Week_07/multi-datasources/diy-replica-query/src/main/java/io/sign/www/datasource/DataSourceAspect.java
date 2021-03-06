package io.sign.www.datasource;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Aspect
@Component
public class DataSourceAspect implements Ordered {

    private List<String> masterStartPredicate = new ArrayList<String>() {{
        add("save");
        add("add");
        add("insert");
        add("update");
        add("edit");
        add("delete");
        add("remove");
    }};

    @Pointcut("execution(* io.sign.www.service.*.*(..))")
    public void myServicePointCut() {

    }

    @Pointcut("execution(* com.baomidou.mybatisplus.extension.service.IService.*(..))")
    public void mybatisPlusService() {

    }


    @Around("myServicePointCut() || mybatisPlusService()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        log.info("service 方法名：" + method.getName());
        boolean isMasterDatasource = false;
        for (String predicate : masterStartPredicate) {
            if (method.getName().startsWith(predicate)) {
                isMasterDatasource = true;
                break;
            }
        }
        if (isMasterDatasource) {
            log.info("命中主库");
            DynamicDataSource.setDataSource(DynamicDataSourceConfig.masterName);
        } else {
            log.info("命中从库");
            DynamicDataSource.setDataSource(DynamicDataSourceConfig.slave1Name);
        }
        try {
            return point.proceed();
        } finally {
            DynamicDataSource.clearDataSource();
            log.debug("clean datasource");
        }
    }


    @Override
    public int getOrder() {
        return 1;
    }
}
