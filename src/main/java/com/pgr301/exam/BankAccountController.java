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
    public void transfer(@RequestBody Transaction tx, @PathVariable String fromAccount, @PathVariable String toAccount) {
        // Should return HTTP status code
        Timer timer = meterRegistry.timer("responseTime",
                "method", "transfer");
        timer.record(() -> {
            try {
                bankService.transfer(tx, fromAccount, toAccount);
            } catch (BackEndException exception){
                meterRegistry.counter("backendException", "method", "bankService.transfer").increment();
                Logger.getLogger(this.getClass().getName()).info("Backend exception thrown in method: bankService.transfer");
            }
        });
    }

    @PostMapping(path = "/account", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Account> updateAccount(@RequestBody Account a) {
        Timer timer = meterRegistry.timer("responseTime",
                "method", "updateAccount");
        timer.record(() -> {
            try {
                bankService.updateAccount(a);
            } catch (BackEndException exception){
                meterRegistry.counter("backendException", "method", "bankService.updateAccount").increment();
                Logger.getLogger(this.getClass().getName()).info("Backend exception thrown in method: bankService.updateAccount");
            }
        });
        return new ResponseEntity<>(a, HttpStatus.OK);
    }

    // Has to have header set: Content-Type: application/json
    @GetMapping(path = "/account/{accountId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Account> balance(@PathVariable String accountId) {
        /*Timer timer = meterRegistry.timer("responseTime",
                "method", "balance");
        timer.record(() -> {
            try {
                Account account = ofNullable(bankService.getAccount(accountId)).orElseThrow(AccountNotFoundException::new);
            } catch (BackEndException exception){
                meterRegistry.counter("backendException", "method", "bankService.getAccount").increment();
                Logger.getLogger(this.getClass().getName()).info("Backend exception thrown in method: bankService.getAccount");
            }
        });*/

        Account account = null;

        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            account = ofNullable(bankService.getAccount(accountId)).orElseThrow(AccountNotFoundException::new);
        } catch (BackEndException exception){
            meterRegistry.counter("backendException", "method", "bankService.getAccount").increment();
            Logger.getLogger(this.getClass().getName()).info("Backend exception thrown in method: bankService.getAccount");
        } finally {
            timer.stop(Timer.builder("responseTime").tags("method", "balance").register(meterRegistry));
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

