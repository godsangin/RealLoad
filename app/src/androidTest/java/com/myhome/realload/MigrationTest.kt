package com.myhome.realload
//
//import androidx.room.migration.Migration
//import androidx.room.testing.MigrationTestHelper
//import androidx.sqlite.db.SupportSQLiteDatabase
//import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.platform.app.InstrumentationRegistry
//import com.myhome.realload.db.AppDatabase
//import org.junit.BeforeClass
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//import java.io.IOException
//
//@RunWith(AndroidJUnit4::class)
//class MigrationTest {
//    companion object {
//        @BeforeClass fun setup() {
//            // Setting up
//        }
//    }
//
//    private var TEST_DB:String = "migration-test"
//    val MIGRATION_1_2 = object : Migration(1, 2) {
//        override fun migrate(database: SupportSQLiteDatabase) {
//            database.execSQL("CREATE TABLE `image` (`id` INTEGER, `pid` INTEGER, `imageRes` TEXT" +
//                    "PRIMARY KEY(`id`))")
//        }
//    }
//
//    @get:Rule
//    val helper: MigrationTestHelper = MigrationTestHelper(
//        InstrumentationRegistry.getInstrumentation(),
//        AppDatabase::class.java.canonicalName,
//        FrameworkSQLiteOpenHelperFactory()
//    )
//
//
//    @Test
//    @Throws(IOException::class)
//    fun migrate1To2() {
//        var db = helper.createDatabase(TEST_DB, 1).apply {
//            // db has schema version 1. insert some data using SQL queries.
//            // You cannot use DAO classes because they expect the latest schema.
//            execSQL("INSERT INTO myPlace VALUES (0,0.0,0.0,'', 'false')")
//
//            // Prepare for the next version.
//            close()
//        }
//
//        // Re-open the database with version 2 and provide
//        // MIGRATION_1_2 as the migration process.
//        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
//
//        // MigrationTestHelper automatically verifies the schema changes,
//        // but you need to validate that the data was migrated properly.
//    }
//}