package com.edmik.parentapp.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.AfterVersion
import org.koin.dsl.module

import app.cash.sqldelight.db.SqlSchema

expect fun createDriver(schema: SqlSchema<QueryResult.Value<Unit>>, name: String): SqlDriver

val DatabaseModule = module {
    single {
        createDriver(com.edmik.parentapp.db.ParentAppDatabase.Schema, "ParentApp.db")
    }
    single {
        com.edmik.parentapp.db.ParentAppDatabase(get())
    }
}
