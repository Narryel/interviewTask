package ru.t1.interviews.concurrency;

import java.util.UUID;

public interface TaskExecutor {
    UUID execute(UUID taskId, Boolean fast);
}
