package ru.t1.interviews.concurrency;

import java.util.UUID;
public interface TaskGenerator {
    UUID generate(UUID taskId, Boolean fast);
}
