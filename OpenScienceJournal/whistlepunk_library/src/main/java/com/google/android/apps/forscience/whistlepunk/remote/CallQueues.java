package com.google.android.apps.forscience.whistlepunk.remote;

import java.util.HashMap;
import java.util.Map;

class CallQueues {

    private static final Map<String, CallQueue> mQueues = new HashMap<>();

    static CallController enqueue(final String queueId, final HttpCall<?, ?> call) {
        CallQueue queue;
        synchronized (mQueues) {
            queue = mQueues.get(queueId);
            if (queue == null) {
                queue = new CallQueue();
                mQueues.put(queueId, queue);
            }
        }
        return queue.enqueue(call);
    }

}
