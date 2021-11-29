package com.pgr301.exam;

import com.pgr301.exam.model.Account;
import com.pgr301.exam.model.Transaction;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.util.logging.Logger;

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
    public ResponseEntity<HttpStatus> transfer(@RequestBody Transaction tx, @PathVariable String fromAccount, @PathVariable String toAccount) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            bankService.transfer(tx, fromAccount, toAccount);
        } catch (BackEndException exception){
            meterRegistry.counter("backendException", "method", "bankService.transfer").increment();
            Logger.getLogger(this.getClass().getName()).info("Backend exception thrown in method: bankService.transfer");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            timer.stop(Timer.builder("responseTime").tags("method", "bankService.transfer").register(meterRegistry));
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(path = "/account", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Account> updateAccount(@RequestBody Account a) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            bankService.updateAccount(a);
        } catch (BackEndException exception){
            meterRegistry.counter("backendException", "method", "bankService.updateAccount").increment();
            Logger.getLogger(this.getClass().getName()).info("Backend exception thrown in method: bankService.updateAccount");
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            timer.stop(Timer.builder("responseTime").tags("method", "bankService.updateAccount").register(meterRegistry));
        }

        return new ResponseEntity<>(a, HttpStatus.OK);
    }

    // Has to have header set: Content-Type: application/json
    @GetMapping(path = "/account/{accountId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Account> balance(@PathVariable String accountId) {
        Account account;

        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            account = ofNullable(bankService.getAccount(accountId)).orElseThrow(AccountNotFoundException::new);
        } catch (BackEndException exception){
            meterRegistry.counter("backendException", "method", "bankService.getAccount").increment();
            Logger.getLogger(this.getClass().getName()).info("Backend exception thrown in method: bankService.getAccount");
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            timer.stop(Timer.builder("responseTime").tags("method", "bankService.getAccount").register(meterRegistry));
        }

        return new ResponseEntity<>(account, HttpStatus.OK);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
    }

    @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "video not found")
    public static class AccountNotFoundException extends RuntimeException {
    }
}

