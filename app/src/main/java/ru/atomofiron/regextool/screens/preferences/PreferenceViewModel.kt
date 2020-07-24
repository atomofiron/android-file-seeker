package ru.atomofiron.regextool.screens.preferences

import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.SingleLiveEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import ru.atomofiron.regextool.di.DaggerInjector
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import ru.atomofiron.regextool.model.preference.ExplorerItemComposition
import ru.atomofiron.regextool.model.preference.JoystickComposition
import ru.atomofiron.regextool.model.preference.ToyboxVariant
import ru.atomofiron.regextool.utils.Shell
import javax.inject.Inject

class PreferenceViewModel : BaseViewModel<PreferenceComponent, PreferenceFragment>() {

    @Inject
    lateinit var preferenceStore: PreferenceStore

    val alert = SingleLiveEvent<String>()
    val alertOutputSuccess = SingleLiveEvent<Int>()
    val alertOutputError = SingleLiveEvent<Shell.Output>()
    val isExportImportAvailable: Boolean get() = context.getExternalFilesDir(null) != null
    val explorerItemComposition: ExplorerItemComposition get() = preferenceStore.explorerItemComposition.entity
    val joystickComposition: JoystickComposition get() = preferenceStore.joystickComposition.entity
    val toyboxVariant: ToyboxVariant get() = preferenceStore.toyboxVariant.entity

    override val component = DaggerPreferenceComponent
            .builder()
            .bind(this)
            .bind(viewProperty)
            .dependencies(DaggerInjector.appComponent)
            .build()

    override fun inject(view: PreferenceFragment) {
        super.inject(view)
        component.inject(this)
        component.inject(view)
    }

    fun getCurrentValue(key: String): Any? = preferenceStore.getCurrentValue(key)
}