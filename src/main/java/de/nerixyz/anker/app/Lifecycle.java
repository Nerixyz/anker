package de.nerixyz.anker.app;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.io.Closeable;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class Lifecycle {
    private static final @NonNull List<WeakReference<? super Closeable>> pendingRefs = new ArrayList<>(1);

    private Lifecycle() {
    }

    public static void trackObjectToClose(WeakReference<? super Closeable> ref) {
        pendingRefs.add(ref);
    }

    public static void untrackObjectToClose(Closeable ref) {
        pendingRefs.removeIf(weak -> weak.get() == ref);
    }

    public static void cleanupObjects() {
        for (var ref : pendingRefs) {
            try {
                if (ref.get() instanceof Closeable c) {
                    c.close();
                }
            } catch (IOException e) {
                log.warn("Couldn't close object", e);
            }
        }
        pendingRefs.clear();
    }
}
