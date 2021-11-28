package com.pgr301.exam;

import com.pgr301.exam.model.Account;
import com.pgr301.exam.model.Transaction;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.math.BigDecimal.*;
import static java.util.Optional.ofNullable;

@RestController
public class BankAccountController implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private BankingCoreSystmeService bankService;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    public BankAccountController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostMapping(path = "/account/{fromAccount}/transfer/{toAccount}", consumes = "application/json", produces = "application/json")
    public void transfer(@RequestBody Transaction tx, @PathVariable String fromAccount, @PathVariable String toAccount) {
        //meterRegistry.counter("transfer", "amount", String.valueOf(tx.getAmount())).increment();

        /*Timer timer = meterRegistry.timer("transfer",
                "amount", String.valueOf(tx.getAmount()),
                "fromAccount", fromAccount,
                "toAccount", toAccount);
        timer.record(() -> {
            try {
                //bankService.transfer(tx, fromAccount, toAccount);
                TimeUnit.MILLISECONDS.sleep(5000);
            } catch (BackEndException | InterruptedException exception){
                meterRegistry.counter("backendException").increment();
                meterRegistry.gauge("amount", tx.getAmount());
            }
        });*/

        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            bankService.transfer(tx, fromAccount, toAccount);
        } catch (BackEndException exception){
            meterRegistry.counter("backendException").increment();
            meterRegistry.gauge("amount", tx.getAmount());
        } finally {
            timer.stop(Timer.builder("transfer").tags("amount", String.valueOf(tx.getAmount()),
                    "fromAccount", fromAccount,
                    "toAccount", toAccount).register(meterRegistry));
        }
    }

    @PostMapping(path = "/account", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Account> updateAccount(@RequestBody Account a) {
        bankService.updateAccount(a);
        return new ResponseEntity<>(a, HttpStatus.OK);
    }

    // Has to have header set: Content-Type: application/json
    @GetMapping(path = "/account/{accountId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Account> balance(@PathVariable String accountId) {
        Account account = ofNullable(bankService.getAccount(accountId)).orElseThrow(AccountNotFoundException::new);
        return new ResponseEntity<>(account, HttpStatus.OK);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
    }

    @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "video not found")
    public static class AccountNotFoundException extends RuntimeException {
    }
}

