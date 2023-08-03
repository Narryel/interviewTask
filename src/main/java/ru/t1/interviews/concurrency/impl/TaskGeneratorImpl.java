package ru.t1.interviews.concurrency.impl;

import ru.t1.interviews.concurrency.TaskGenerator;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class TaskGeneratorImpl implements TaskGenerator {

    @Override
    public UUID generate(UUID taskId, Boolean fast) {
        var delay = delay(fast);
        var id = UUID.randomUUID();
        log.info("id generation task {}, with delay {}, generated id result is {}", taskId, delay, id);
        return id;
    }

    private long delay(boolean fast) {
        long delay = 0;
        try {
            if (!fast) {
                delay = 1000 + ThreadLocalRandom.current().nextInt(3000);
                Thread.sleep(delay);
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return delay;
    }

}
