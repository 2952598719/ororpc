package top.orosirian.consumer;


import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ConsumerTest {

    private static final int THREAD_POOL_SIZE = 20;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public static void main(String[] args) throws InterruptedException {

    }


}
