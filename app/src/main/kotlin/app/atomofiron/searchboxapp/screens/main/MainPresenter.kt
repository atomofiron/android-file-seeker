package app.atomofiron.searchboxapp.screens.main

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.di.dependencies.delegate.ScreenDelegate
import app.atomofiron.searchboxapp.di.dependencies.service.WindowService
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.main.presenter.AppEventDelegate
import app.atomofiron.searchboxapp.screens.main.presenter.AppEventDelegateApi
import app.atomofiron.searchboxapp.screens.main.util.tasks.XTask
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@MainScope
class MainPresenter @Inject constructor(
    scope: CoroutineScope,
    private val viewState: MainViewState,
    router: MainRouter,
    appEventDelegate: AppEventDelegate,
    private val windowService: WindowService,
    preferences: PreferenceStore,
    private val screenDelegate: ScreenDelegate,
) : BasePresenter<MainViewModel, MainRouter>(scope, router)
    , AppEventDelegateApi by appEventDelegate
    , ScreenDelegate by screenDelegate
{
    init {
        viewState.tasks.value = Array(16) { XTask() }.toList()
        preferences.appTheme.collect(scope) {
            viewState.setTheme.value = it
        }
    }

    override fun onSubscribeData() = Unit

    fun onEscClick(): Boolean = when {
        router.onBack(soft = true) -> true
        viewState.activityMode.default -> false
        else -> false.also { router.finish() }
    }

    fun onBackButtonClick() = when {
        router.onBack(soft = false) -> Unit
        viewState.activityMode.default -> router.minimize()
        else -> router.finish()
    }.also {
        onUserInteraction()
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