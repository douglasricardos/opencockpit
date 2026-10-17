package org.prowl.torque.remote;

interface ITorqueService {
    int getVersion();
    float getValueForPid(long pid, boolean readFromCache);
    String[] getPIDInformation(String[] pids);
    long[] getPIDList();
    String[] listActivePIDs();
    String[] listAllPIDs();
    boolean sendPIDData(String data);
    float[] getPIDValues(String[] pids);
}
