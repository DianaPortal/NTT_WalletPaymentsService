package com.nttdata.walletpaymentsservice.service;

import io.reactivex.Completable;
import io.reactivex.subjects.CompletableSubject;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class OperationAwaiter {
    private final ConcurrentHashMap<String, CompletableSubject> waits = new ConcurrentHashMap<>();

    public Completable await(String operationId) {
        CompletableSubject subj = CompletableSubject.create();
        waits.put(operationId, subj);
        // opcional: timeout
        return subj.timeout(10, TimeUnit.SECONDS)
                .doFinally(() -> waits.remove(operationId));
    }

    public void complete(String operationId, boolean success) {
        CompletableSubject subj = waits.remove(operationId);
        if (subj == null) return;
        if (success) subj.onComplete();
        else subj.onError(new RuntimeException("OPERATION_FAILED"));
    }
}
