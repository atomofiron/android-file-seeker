package app.atomofiron.searchboxapp.screens.main

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.injectable.delegate.InitialDelegate
import app.atomofiron.searchboxapp.injectable.service.WindowService
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.main.presenter.AppEventDelegate
import app.atomofiron.searchboxapp.screens.main.presenter.AppEventDelegateApi
import app.atomofiron.searchboxapp.screens.main.util.tasks.XTask
import kotlinx.coroutines.CoroutineScope

class MainPresenter(
    scope: CoroutineScope,
    private val viewState: MainViewState,
    router: MainRouter,
    appEventDelegate: AppEventDelegate,
    private val windowService: WindowService,
    preferenceStore: PreferenceStore,
    private val initialDelegate: InitialDelegate,
) : BasePresenter<MainViewModel, MainRouter>(scope, router),
    AppEventDelegateApi by appEventDelegate
{
    init {
        viewState.tasks.value = Array(16) { XTask() }.toList()
        preferenceStore.appTheme.collect(scope) {
            initialDelegate.updateTheme(it)
            viewState.setTheme.value = it
        }
    }

    override fun onSubscribeData() = Unit

    fun onEscClick(): Boolean = router.onBack()

    fun onBackButtonClick() = when {
        router.onBack() -> Unit
        else -> router.minimize()
    }

    fun onThemeApplied(isDarkTheme: Boolean) {
        updateLightStatusBar(isDarkTheme)
        updateLightNavigationBar(isDarkTheme)
    }

    fun updateLightNavigationBar(isDarkTheme: Boolean) {
        windowService.setLightNavigationBar(!isDarkTheme)
    }

    fun updateLightStatusBar(isDarkTheme: Boolean) {
        val lightStatusBar = router.lastVisibleFragment?.isLightStatusBar ?: !isDarkTheme
        windowService.setLightStatusBar(lightStatusBar)
    }
}