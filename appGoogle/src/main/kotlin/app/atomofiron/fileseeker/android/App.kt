package app.atomofiron.fileseeker.android

import app.atomofiron.fileseeker.service.AppUpdateServiceGoogleImpl
import app.atomofiron.searchboxapp.android.AbstractApp

class App : AbstractApp() {
    override val updateServiceFactory = AppUpdateServiceGoogleImpl
}
