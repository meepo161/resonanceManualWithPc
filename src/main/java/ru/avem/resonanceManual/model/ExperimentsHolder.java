package ru.avem.resonanceManual.model;

import java.util.ArrayList;
import java.util.List;

public class ExperimentsHolder {

    private static final List<Experiment> experiments = new ArrayList<>();

    static {
//        experiments.add(new Experiment("layouts/experiment1View.fxml",
//                Constants.Experiments.EXPERIMENT1_NAME));
//
//        experiments.add(new Experiment("layouts/experiment2View.fxml",
//                Constants.Experiments.EXPERIMENT2_NAME));
    }

    public static Experiment getExperimentByName(String name) {
        for (Experiment experiment : experiments) {
            if (experiment.getTitle().equals(name)) {
                return experiment;
            }
        }
        throw new NullPointerException("Проверьте правильность названия испытания");
    }

    public static List<String> getNamesOfExperiments() {
        List<String> names = new ArrayList<>();

        for (Experiment experiment : experiments) {
            names.add(experiment.getTitle());
        }

        return names;
    }
}
