package ru.avem.resonance.db;

import ru.avem.resonance.db.model.Account;
import ru.avem.resonance.db.model.Protocol;
import ru.avem.resonance.db.model.TestItem;

import java.io.File;
import java.util.ArrayList;

public abstract class DataBaseRepository {
    public static String DATABASE_NAME = "resonanceDataBase.db";
    protected static String DATABASE_URL = "jdbc:" + "sqlite:" + DATABASE_NAME;

    public static void init(boolean forceInit) {
        if (!new File(DATABASE_NAME).exists() || forceInit) {
            AccountRepository.createTable(Account.class);

            Account ivanov = new Account("ivanov", "1234", "Исполнитель-1", "148", "Иванов И. И.");
            AccountRepository.insertAccount(ivanov);

            Account petrov = new Account("petrov", "1234", "Исполнитель-2", "841", "Петров П. П.");
            AccountRepository.insertAccount(petrov);

            TestItemRepository.createTable();

            ArrayList<Double> times = new ArrayList<>();
            times.add(0, 1.0);
            ArrayList<Double> torques = new ArrayList<>();
            torques.add(0, 1.0);
            ArrayList<Double> dots = new ArrayList<>();
            dots.add(0, 0.9);
            TestItem testItem1 = new TestItem("Удалить обязательно", times, torques, dots, 1.0, 1.0, 1.0,
                    1.0, 1.0, 1.0, 1.0, 1.0, "right");
            TestItemRepository.insertTestItem(testItem1);

            ProtocolRepository.createTable();

            Protocol protocol = new Protocol("SN1", testItem1, ivanov, petrov, System.currentTimeMillis());
            ProtocolRepository.insertProtocol(protocol);
        }
    }
}
