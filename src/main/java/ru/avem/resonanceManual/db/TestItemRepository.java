package ru.avem.resonanceManual.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import ru.avem.resonanceManual.db.model.TestItem;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class TestItemRepository extends DataBaseRepository {
    static void createTable() {
        try (ConnectionSource connectionSource = new JdbcConnectionSource(DATABASE_URL)) {
            TableUtils.dropTable(connectionSource, TestItem.class, true);
            TableUtils.createTableIfNotExists(connectionSource, TestItem.class);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertTestItem(TestItem testItem) {
        sendAction((testItemDao) -> testItemDao.create(testItem));
    }

    public static void updateTestItem(TestItem testItem) {
        sendAction((testItemDao) -> testItemDao.update(testItem));
    }

    public static void deleteTestItem(TestItem testItem) {
        sendAction((testItemDao) -> testItemDao.delete(testItem));
    }

    public static List<TestItem> getAllTestItems() {
        final List[] testItems = {null};
        sendAction((testItemDao) -> testItems[0] = testItemDao.queryForAll());
        return (List<TestItem>) testItems[0];
    }

    public static TestItem getTestItem(long id) {
        final TestItem[] testItem = {null};
        sendAction((testItemDao) -> testItem[0] = testItemDao.queryForId(id));
        return testItem[0];
    }

//    public static TestItem getTestItem(String type) {
//        final TestItem[] testItem = {null};
//        sendAction((testItemDao) -> testItem[0] = testItemDao.queryForEq("type", type).get(0));
//        return testItem[0];
//    }

    @FunctionalInterface
    private interface Actionable {
        void onAction(Dao<TestItem, Long> testItemDao) throws SQLException;
    }

    private static void sendAction(Actionable actionable) {
        try (ConnectionSource connectionSource = new JdbcConnectionSource(DATABASE_URL)) {
            Dao<TestItem, Long> testItemDao =
                    DaoManager.createDao(connectionSource, TestItem.class);

            actionable.onAction(testItemDao);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}
