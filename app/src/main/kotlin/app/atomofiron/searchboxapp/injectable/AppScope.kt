package app.atomofiron.searchboxapp.injectable

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class AppScope : CoroutineScope by CoroutineScope(Dispatchers.Default)