package app.atomofiron.searchboxapp.di.dependencies

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppScope @Inject constructor() : CoroutineScope by CoroutineScope(Dispatchers.Default)