package app.atomofiron.fileseeker.android

import app.atomofiron.fileseeker.service.AppUpdateServiceGoogleImpl
import app.atomofiron.searchboxapp.android.AbstractApp
import app.atomofiron.searchboxapp.model.AppSource

class App : AbstractApp() {
    override val appSource = AppSource.GooglePlay
    override val updateServiceFactory = AppUpdateServiceGoogleImpl
}
