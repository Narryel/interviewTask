package ru.t1.interviews.concurrency;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Slf4j
@TestInstance(Lifecycle.PER_CLASS)
class ConcurrencyTest {
    private final static int COUNT = 40;
    private TaskGenerator taskGenerator;
    private TaskExecutor taskExecutor;
    private final IdGenerator idGenerator = new IdGenerator((int) (COUNT * 0.25));

    @BeforeAll
    public void beforeAll() {
        taskGenerator = Mockito.spy(new ru.t1.interviews.concurrency.impl.TaskGeneratorImpl());
        taskExecutor = Mockito.spy(new ru.t1.interviews.concurrency.impl.TaskExecutorImpl(taskGenerator));

        log.info("Common ForkJoinPool thread count: {}", ForkJoinPool.commonPool().getParallelism());
    }

    @Test
    public void generate() {
        CompletableFuture.allOf(IntStream.range(1, COUNT + 1)
                .boxed()
                .map(this::run)
                .toArray((count) -> new CompletableFuture<?>[count])).join();
    }

    private CompletableFuture<UUID> run(final int index) {
        var fast = (index % 2) == 0;
        log.info("Running concurrently {}, fast mode {}", index, fast);
        return CompletableFuture.supplyAsync(() -> taskExecutor.execute(idGenerator.get(), fast));
    }

    @AfterAll
    public void afterAll() {
        ArgumentCaptor<UUID> acExecutorIds = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<UUID> acGeneratorIds = ArgumentCaptor.forClass(UUID.class);

        verify(taskExecutor, times(COUNT)).execute(acExecutorIds.capture(), anyBoolean());
        verify(taskGenerator, atMost(idGenerator.size())).generate(acGeneratorIds.capture(), anyBoolean());

        var executorIdUniqueCount = computeAndLogInvocations(acExecutorIds, "executor");
        var generatorIdUniqueCount = computeAndLogInvocations(acGeneratorIds, "generator");

        Assertions.assertEquals(executorIdUniqueCount, generatorIdUniqueCount, "ids used by executor must match");
    }

    private int computeAndLogInvocations(ArgumentCaptor<UUID> ag, String title) {
        var invocations = ag.getAllValues();
        log.info("Recorded {} invocations count {}", title, invocations.size());

        var statistics = new HashMap<UUID, Integer>();
        invocations.forEach(id -> statistics.compute(id, (k, v) -> v == null ? 1 : v + 1));
        statistics.forEach((k, v) -> log.info("id {} used {}", k, v));

        return statistics.size();
    }

    private static class IdGenerator {
        private final UUID[] ids;

        public IdGenerator(int size) {
            ids = new UUID[size];

            Arrays.setAll(ids, (i) -> UUID.randomUUID());
        }

        public int size() {
            return ids.length;
        }

        public UUID get() {
            return UUID.fromString(ids[ThreadLocalRandom.current().nextInt(ids.length)].toString());
        }
    }
}
