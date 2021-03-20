package io.sign.www.hmily;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.hmily.annotation.HmilyTCC;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@DubboService(version = "1.0.0", tag = "red", weight = 100)
public class AccountServiceImpl implements AccountService{

    @Autowired
    private AccountDao accountDao;
    @Autowired
    private FreezeDao freezeDao;


    @Override
    @Transactional(rollbackFor = Exception.class)
    @HmilyTCC(confirmMethod = "confirm", cancelMethod = "cancel")
    public boolean payment(AccountDTO accountDTO) {
        //减金额
        if (accountDao.reduce(accountDTO) == 0) {
            throw new HmilyRuntimeException("账户扣减异常！");
        }
        //加冻结
        Freeze freeze = freezeDao.select(accountDTO.getUserId());
        if (freeze == null) {
            if (freezeDao.insert(accountDTO) == 0) {
                throw new HmilyRuntimeException("账户冻结异常！");
            }
        } else {
            if (freezeDao.increase(accountDTO) == 0) {
                throw new HmilyRuntimeException("账户冻结异常！");
            }
        }
        return true;
    }


    @Transactional(rollbackFor = Exception.class)
    public boolean confirm(AccountDTO accountDTO) {
        log.info("============dubbo tcc 执行确认付款接口===============");
        freezeDao.reduce(accountDTO);
        return Boolean.TRUE;
    }



    @Transactional(rollbackFor = Exception.class)
    public boolean cancel(AccountDTO accountDTO) {
        log.info("============ dubbo tcc 执行取消付款接口===============");
        accountDao.increase(accountDTO);
        freezeDao.reduce(accountDTO);
        return Boolean.TRUE;
    }
}
