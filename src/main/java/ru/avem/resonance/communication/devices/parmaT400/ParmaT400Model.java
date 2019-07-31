package ru.avem.resonance.communication.devices.parmaT400;

import java.util.Observable;
import java.util.Observer;

public class ParmaT400Model extends Observable {
    public static final int RESPONDING_PARAM = 0;
    public static final int F_PARAM = 1;
    public static final int P_PARAM = 2;
    public static final int Q_PARAM = 3;
    public static final int S_PARAM = 4;
    public static final int UAB_PARAM = 5;
    public static final int UBC_PARAM = 6;
    public static final int UCA_PARAM = 7;
    public static final int IA_PARAM = 8;
    public static final int IB_PARAM = 9;
    public static final int IC_PARAM = 10;
    public static final int I0_PARAM = 11;
    public static final int UA_PARAM = 12;
    public static final int UB_PARAM = 13;
    public static final int UC_PARAM = 14;
    public static final int U0_PARAM = 15;
    public static final int PA_PARAM = 16;
    public static final int PB_PARAM = 17;
    public static final int PC_PARAM = 18;
    public static final int QA_PARAM = 19;
    public static final int QB_PARAM = 20;
    public static final int QC_PARAM = 21;
    public static final int SA_PARAM = 22;
    public static final int SB_PARAM = 23;
    public static final int SC_PARAM = 24;
    public static final int COSA_PARAM = 25;
    public static final int COSB_PARAM = 26;
    public static final int COSC_PARAM = 27;
    public static final int TIMELOW_PARAM = 28;
    public static final int TIMEHIGH_PARAM = 29;
    public static final int COS_PARAM = 30;
    private int deviceID;
    private boolean readResponding;
    private boolean writeResponding;

    ParmaT400Model(Observer observer, int deviceID) {
        addObserver(observer);
        this.deviceID = deviceID;
    }

 void resetResponding() {
        readResponding = true;
        writeResponding = true;
    }

    void setReadResponding(boolean readResponding) {
        this.readResponding = readResponding;
        setResponding();
    }

    void setWriteResponding(boolean writeResponding) {
        this.writeResponding = writeResponding;
        setResponding();
    }

    private void setResponding() {
        notice(RESPONDING_PARAM, readResponding && writeResponding);
    }

    public void setF(short f) {
            notice(F_PARAM, (f & 0xFFFF) / 1000.0);
    }

    public void setP(short p) {
        int i = p & 0xFFFF;
        if (i > 0 && i < 65535) {
            notice(P_PARAM, p / 5.0);
        }
    }

    public void setQ(short q) {
        int i = q & 0xFFFF;
        if (i > 0 && i < 65535) {
            notice(Q_PARAM, q / 5.0);
        }
    }

    public void setS(short s) {
        notice(S_PARAM, (s & 0xFFFF) / 5.0);
    }

    public void setUab(short uab) {
        int i = (uab & 0xFFFF);
        if (i > 0 && i < 30000) {
            notice(UAB_PARAM, (uab & 0xFFFF) / 50.0);
        }
    }

    public void setUbc(short ubc) {
        int i = (ubc & 0xFFFF);
        if (i > 0 && i < 30000) {
            notice(UBC_PARAM, (ubc & 0xFFFF) / 50.0);
        }
    }

    public void setUca(short uca) {
        int i = (uca & 0xFFFF);
        if (i > 0 && i < 30000) {
            notice(UCA_PARAM, (uca & 0xFFFF) / 50.0);
        }
    }

    public void setIa(short ia) {
        int i = ia & 0xFFFF;
        if (i > 0 && i < 65535) {
            notice(IA_PARAM, (ia & 0xFFFF) / 5000.0);
        }
    }

    public void setIb(short ib) {
        int i = ib & 0xFFFF;
        if (i > 0 && i < 65535) {
            notice(IB_PARAM, (ib & 0xFFFF) / 5000.0);
        }
    }

    public void setIc(short ic) {
        int i = ic & 0xFFFF;
        if (i > 0 && i < 65535) {
            notice(IC_PARAM, (ic & 0xFFFF) / 5000.0);
        }
    }

    public void setI0(short i0) {
        notice(I0_PARAM, (i0 & 0xFFFF) / 5000.0);
    }

    public void setUa(short ua) {
            notice(UA_PARAM, (ua & 0xFFFF) / 100.0);
    }

    public void setUb(short ub) {
        notice(UB_PARAM, (ub & 0xFFFF) / 100.0);
    }

    public void setUc(short uc) {
        notice(UC_PARAM, (uc & 0xFFFF) / 100.0);
    }

    public void setU0(short u0) {
        notice(U0_PARAM, (u0 & 0xFFFF) / 100.0);
    }

    public void setPa(short pa) {
        notice(PA_PARAM, (pa & 0xFFFF) / 10.0);
    }

    public void setPb(short pb) {
        notice(PB_PARAM, (pb & 0xFFFF) / 10.0);
    }

    public void setPc(short pc) {
        notice(PC_PARAM, (pc & 0xFFFF) / 10.0);
    }

    public void setQa(short qa) {
        notice(QA_PARAM, (qa & 0xFFFF) / 10.0);
    }

    public void setQb(short qb) {
        notice(QB_PARAM, (qb & 0xFFFF) / 10.0);
    }

    public void setQc(short qc) {
        notice(QC_PARAM, (qc & 0xFFFF) / 10.0);
    }

    public void setSa(short sa) {
        notice(SA_PARAM, (sa & 0xFFFF) / 10.0);
    }

    public void setSb(short sb) {
        notice(SB_PARAM, (sb & 0xFFFF) / 10.0);
    }

    public void setSc(short sc) {
        notice(SC_PARAM, (sc & 0xFFFF) / 10.0);
    }

    public void setCosa(short cosa) {
        if (cosa > 0 && cosa < 32767) {
            notice(COSA_PARAM, cosa / 10000.0);
        }
    }

    public void setCosb(short cosb) {
        if (cosb > 0 && cosb < 32767) {
            notice(COSB_PARAM, cosb / 10000.0);
        }
    }

    public void setCosc(short cosc) {
        if (cosc > 0 && cosc < 32767) {
            notice(COSC_PARAM, cosc / 10000.0);
        }
    }

    public void setTimeLow(short timeLow) {
        notice(TIMELOW_PARAM, timeLow);
    }

    public void setTimeHigh(short timeHigh) {
        notice(TIMEHIGH_PARAM, timeHigh);
    }

    public void setCos(short cos) {
        if (cos > 0 && cos < 32767) {
            notice(COS_PARAM, cos / 10000.0);
        }
    }


    private void notice(int param, Object value) {
        setChanged();
        notifyObservers(new Object[]{deviceID, param, value});
    }
}