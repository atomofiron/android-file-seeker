package app.atomofiron.searchboxapp.di.dependencies

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class AppScope : CoroutineScope by CoroutineScope(Dispatchers.Default)