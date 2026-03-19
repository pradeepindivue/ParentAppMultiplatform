package com.edmik.parentapp.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.db.QueryResult
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings

actual fun createDriver(schema: SqlSchema<QueryResult.Value<Unit>>, name: String): SqlDriver {
    return NativeSqliteDriver(schema, name)
}

actual fun createSettings(): Settings {
    return KeychainSettings("ParentAppKeychain")
}
