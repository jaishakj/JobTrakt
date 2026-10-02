package com.jobtrakt.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobWorker {
    private final JobProcessor processor;
    public JobWorker(JobProcessor processor) { this.processor = processor; }

    @Scheduled(fixedDelayString = "${JOB_POLL_DELAY_MS:5000}")
    public void pollQueue() {
        for (int processed = 0; processed < 20 && processor.processNextDue(); processed++) {
            // Keep one polling cycle bounded so application traffic remains responsive.
        }
    }
}
