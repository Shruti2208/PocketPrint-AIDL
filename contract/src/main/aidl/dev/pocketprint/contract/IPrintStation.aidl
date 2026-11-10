package dev.pocketprint.contract;
import dev.pocketprint.contract.IPrintCallback;
interface IPrintStation {
    int getProtocolVersion();
    // 0 = accepted, 1 = invalid input, 2 = station busy.
    int submit(String jobId, String text, IPrintCallback callback);
}
