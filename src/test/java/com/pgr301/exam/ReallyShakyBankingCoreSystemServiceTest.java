package com.pgr301.exam;

import com.pgr301.exam.model.Account;
import com.pgr301.exam.model.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ReallyShakyBankingCoreSystemServiceTest {

   /* @Autowired
    ReallyShakyBankingCoreSystemService bankingCoreSystemService;

    @Test
    public void transactionTest() {

        Transaction tx = new Transaction();
        tx.setAmount(500);

        bankingCoreSystemService.transfer(tx, "test1", "test2");

        BigDecimal balance = bankingCoreSystemService.balance("test2");

        assertEquals(new BigDecimal(500), balance);
    }*/

    @Test
    public void test() {

        Transaction tx = new Transaction();
        tx.setAmount(500);

        Account a = new Account();
        a.setId("newAccount1");

        a.setBalance(BigDecimal.valueOf(tx.getAmount()));

        assertEquals(BigDecimal.valueOf(tx.getAmount() - 1), a.getBalance());

    }
}
