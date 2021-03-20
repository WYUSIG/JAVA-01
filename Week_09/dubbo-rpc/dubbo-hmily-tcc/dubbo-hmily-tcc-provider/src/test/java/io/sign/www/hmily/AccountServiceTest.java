package io.sign.www.hmily;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    @Test
    public void payment() {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setUserId("10000");
        accountDTO.setAmount(new BigDecimal("100"));
        accountService.payment(accountDTO);
    }
}
