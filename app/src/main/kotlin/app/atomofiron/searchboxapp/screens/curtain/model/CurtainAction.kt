package app.atomofiron.searchboxapp.screens.curtain.model

import app.atomofiron.common.util.Alert

sealed class CurtainAction {
    class ShowNext(val id: CurtainId) : CurtainAction()
    data object ShowPrev : CurtainAction()
    data class Hide(val irrevocably: Boolean) : CurtainAction()
    class ShowSnackbar(val alert: Alert.Uni) : CurtainAction()
}