package ru.tusur.data.usecase

import android.content.Context
import ru.tusur.core.util.FileHelper
import ru.tusur.data.local.DatabaseProvider



class CreateDatabaseUseCaseImpl(
    private val context: Context,
    private val provider: DatabaseProvider
) {
    operator fun invoke() {
        // Close current DB
        provider.resetDatabase()

        // Create new empty DB file
        //FileHelper.createNewDatabaseFile(context)

        // Reinitialize Room with the new DB
        provider.initializeDatabase()
    }
}
