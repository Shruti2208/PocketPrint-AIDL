package dev.pocketprint.contract;
// One-way callbacks never block the station waiting for client UI work.
oneway interface IPrintCallback {
    void onProgress(String jobId, int percent);
    void onCompleted(String jobId, String preview);
}
