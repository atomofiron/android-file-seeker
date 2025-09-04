package app.atomofiron.searchboxapp.screens.preferences.presenter.curtain

import android.view.LayoutInflater
import android.view.View
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.android.Intents
import app.atomofiron.fileseeker.databinding.CurtainAboutBinding
import app.atomofiron.searchboxapp.model.AppSource
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.preferences.PreferenceRouter
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.ExtType
import app.atomofiron.searchboxapp.utils.resolve
import lib.atomofiron.insets.insetsPadding

class AboutDelegate(
    private val router: PreferenceRouter,
    private val appSource: AppSource,
) : CurtainApi.Adapter<CurtainApi.ViewHolder>() {

    override fun getHolder(inflater: LayoutInflater, layoutId: Int): CurtainApi.ViewHolder {
        val binding = CurtainAboutBinding.inflate(inflater, null, false)
        binding.init()
        binding.root.insetsPadding(ExtType.curtain, vertical = true)
        return CurtainApi.ViewHolder(binding.root)
    }

    private fun CurtainAboutBinding.init() {
        val versionIcon = when (appSource) {
            AppSource.GitHub -> R.drawable.ic_github
            AppSource.GooglePlay -> R.drawable.ic_google_play
        }
        version.setCompoundDrawablesRelativeWithIntrinsicBounds(versionIcon, 0, 0, 0)
        val context = root.context
        var available = context.resolve(Intents.github)
        github.isEnabled = available
        github.alpha = Alpha.enabled(available)
        val tint = context.findColorByAttr(MaterialAttr.colorOnSurface)
        github.compoundDrawablesRelative[0].setTint(tint)
        forpda.compoundDrawablesRelative[0].setTint(tint)

        available = context.resolve(Intents.forPda)
        forpda.isEnabled = available
        forpda.alpha = Alpha.enabled(available)

        val listener = ::onClick
        github.setOnClickListener(listener)
        forpda.setOnClickListener(listener)
    }

    private fun onClick(view: View) {
        when (view.id) {
            R.id.github -> router.goToGithub()
            R.id.forpda -> router.goToForPda()
        }
    }
}