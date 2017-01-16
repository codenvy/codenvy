/*
 *  [2012] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.im.event;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Dmytro Nochevnov
 * @see {org.eclipse.che.api.analytics.logger.EventLogger}
 */
@Singleton
public class EventLogger {
    private static final int QUEUE_MAX_CAPACITY = 10000;

    private final Thread       logThread;
    private final Queue<Event> queue;

    static Logger LOG = LoggerFactory.getLogger(EventLogger.class);   // is not final for testing propose

    /**
     * Stores the number of ignored events due to maximum queue capacity
     */
    private long ignoredEvents;

    public EventLogger() {
        this.queue = new LinkedBlockingQueue<>(QUEUE_MAX_CAPACITY);
        this.ignoredEvents = 0;

        logThread = new LogThread();
        logThread.setDaemon(true);
    }

    @PostConstruct
    public void init() {
        logThread.start();
    }

    @PreDestroy
    public void destroy() {
        logThread.interrupt();
    }

    public void log(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Empty event is not accepted");
        }

        if (!offerEvent(event)) {
            if (ignoredEvents++ % 1000 == 0) {
                LOG.warn("Ignored " + ignoredEvents + " events due to maximum queue capacity");
            }
        }
    }

    boolean offerEvent(Event event) {
        return queue.offer(event);
    }

    /**
     * Is responsible for logging events.
     * Rate-limit is 50 messages per second.
     */
    class LogThread extends Thread {

        static final int LOG_MESSAGE_TIMEOUT_MILLIS          = 20;
        static final int SLEEP_ON_EMPTY_QUEUE_TIMEOUT_MILLIS = 1000;

        private LogThread() {
            super("Installation Manager Event Logger");
        }

        @Override
        public void run() {
            LOG.info(getName() + " thread is started, queue is initialized for " + QUEUE_MAX_CAPACITY + " messages");
            while (!isInterrupted()) {
                Event event = queue.poll();

                try {
                    if (event != null) {
                        LOG.info(event.toString());
                        sleep(LOG_MESSAGE_TIMEOUT_MILLIS);
                    } else {
                        sleep(SLEEP_ON_EMPTY_QUEUE_TIMEOUT_MILLIS);
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }

            LOG.info(getName() + " thread is stopped");
        }
    }
}
