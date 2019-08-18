package ru.avem.resonance.model;

import ru.avem.resonance.db.model.Account;
import ru.avem.resonance.db.model.Protocol;
import ru.avem.resonance.db.model.TestItem;

public class ExperimentValuesModel {

    private static ExperimentValuesModel instance = new ExperimentValuesModel();

    private Account firstTester = new Account("ADMIN", "ADMIN", "ADMIN", "ADMIN", "ADMIN");
    private Account secondTester = new Account("ADMIN", "ADMIN", "ADMIN", "ADMIN", "ADMIN");

    private boolean isNeedRefresh = true;
    private Protocol currentProtocol;

    private Protocol intermediateProtocol;

    private Experiment1Model experiment1Model = new Experiment1Model();
//    private Experiment2Model experiment2Model = new Experiment2Model();
//    private Experiment3Model experiment3Model = new Experiment3Model();


    private ExperimentValuesModel() {
    }

    public static ExperimentValuesModel getInstance() {
        return instance;
    }

    public void setTesters(Account tester1, Account tester2) {
        this.firstTester = tester1;
        this.secondTester = tester2;
    }

    public boolean isNeedRefresh() {
        return isNeedRefresh;
    }

    public void setNeedRefresh(boolean needRefresh) {
        isNeedRefresh = needRefresh;
    }

    public void createNewProtocol(String serialNumber, TestItem selectedTestItem) {
        currentProtocol = new Protocol(serialNumber, selectedTestItem, firstTester, secondTester, System.currentTimeMillis());
    }

    public Protocol getCurrentProtocol() {
        return currentProtocol;
    }

    public void setCurrentProtocol(Protocol currentProtocol) {
        this.currentProtocol = currentProtocol;
    }

    public void setIntermediateProtocol(Protocol intermediateProtocol) {
        this.intermediateProtocol = intermediateProtocol;
    }

    public void applyIntermediateProtocol() {
        currentProtocol = intermediateProtocol;
        intermediateProtocol = null;
    }

    public Experiment1Model getExperiment1Model() {
        return experiment1Model;
    }

    public void setExperiment1Model(Experiment1Model experiment1Model) {
        this.experiment1Model = experiment1Model;
    }
}
