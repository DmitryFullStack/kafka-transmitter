package com.luxoft.kirilin.messagetransmitter.config.troutbox;

import com.gruelbox.transactionoutbox.TransactionManager;

import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ExtendedMigrationManager {
    private static final String OUTBOX_TABLE_PREPARED_STATEMENT = "create table if not exists %s " +
            "(id varchar(36) not null primary key,"
            + "invocation      text,"
            + "nextattempttime timestamp(6),"
            + "attempts        integer,"
            + "blocked         boolean,"
            + "version         integer,"
            + "uniquerequestid varchar(250) unique,"
            + "processed       boolean,"
            + "lastattempttime timestamp(6));";
    private static final String OUTBOX_TABLE_INDEX_PREPARED_STATEMENT = "create index if not exists %s " +
            "on %s (processed, blocked, nextattempttime);";

    static void migrate(TransactionManager transactionManager, @NotNull String tableName) {
        transactionManager.inTransaction(
                transaction -> {
                    Connection connection = transaction.connection();
                    try (PreparedStatement createTable = connection.prepareStatement(String.format(OUTBOX_TABLE_PREPARED_STATEMENT, tableName));
                         PreparedStatement createIndex = connection.prepareStatement(String.format(OUTBOX_TABLE_INDEX_PREPARED_STATEMENT, "index_" + tableName, tableName))) {
                        connection.setAutoCommit(false);
                        createTable.executeUpdate();
                        createIndex.executeUpdate();
                        connection.commit();
                    } catch (Exception e) {
                        throw new RuntimeException("Migrations failed", e);
                    }
                });
    }
}