package dev.pocketprint.station;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import dev.pocketprint.contract.IPrintCallback;
import dev.pocketprint.contract.IPrintStation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Binder threads validate and enqueue; the worker owns the simulated print job.
 */
public final class PrintService extends Service {
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final AtomicBoolean busy = new AtomicBoolean();
    private final IPrintStation.Stub binder = new IPrintStation.Stub() {
        @Override public int getProtocolVersion() {return 1;}

        @Override public int submit(String id, String text, IPrintCallback callback) {
            if (id == null || id.isEmpty() || id.length() > 80 || text == null ||
                text.trim().isEmpty() || text.length() > 2000 || callback == null) return 1;
            if (!busy.compareAndSet(false, true)) return 2;
            try {
                worker.execute(() -> {
                    try {
                        for (int progress : new int[]{0, 25, 50, 75, 100}) {
                            callback.onProgress(id, progress);
                            Thread.sleep(400);
                        }
                        String preview = "POCKET PRINT\n--------------------\n" + text +
                            "\n--------------------\nJob: " + id;
                        getSharedPreferences("station", MODE_PRIVATE).edit()
                            .putString("preview", preview).apply();
                        callback.onCompleted(id, preview);
                    } catch (RemoteException ignored) {
                        // The client died: stop work and release the slot.
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                    } finally {busy.set(false);}
                });
            } catch (RejectedExecutionException stopped) {
                busy.set(false);
                return 2;
            }
            return 0;
        }
    };

    @Override public IBinder onBind(Intent intent) {return binder;}

    @Override public void onDestroy() {
        worker.shutdownNow();
        super.onDestroy();
    }
}
