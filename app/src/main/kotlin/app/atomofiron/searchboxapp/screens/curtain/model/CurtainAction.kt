package app.atomofiron.searchboxapp.screens.curtain.model

import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi

sealed class CurtainAction {
    class ShowNext(val layoutId: Int) : CurtainAction()
    data object ShowPrev : CurtainAction()
    data object Hide : CurtainAction()
    class ShowSnackbar(val provider: CurtainApi.SnackbarProvider) : CurtainAction()
}