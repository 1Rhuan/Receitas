package cc.rhuan.receitas

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(ReceitasPreviewTest::class, CreationNavigationTest::class,
    RegistrationCopyTest::class, DetailAndFormDesignTest::class)
class DesignMigrationSuite
