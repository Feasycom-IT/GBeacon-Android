package com.glowbird.gbeacon.ui.setting;

public class ScanCallback {


    private static volatile ScanCallback singleton = null;

    public static ScanCallback getInstance() {
        if (singleton == null) {
            synchronized (ScanCallback.class) {
                if (singleton == null) {
                    singleton = new ScanCallback();
                }
            }
        }
        return singleton;
    }

    private Scall scall;

    public Scall getScall() {
        return scall;
    }

    public void setScall(Scall scall) {
        this.scall = scall;


    }

    interface Scall{
        void result(String str);
    }


}
