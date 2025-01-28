package app.atomofiron.searchboxapp.screens.preferences.presenter.curtain

import android.view.LayoutInflater
import android.view.View
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.android.Intents
import app.atomofiron.fileseeker.databinding.CurtainAboutBinding
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.preferences.PreferenceRouter
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.ExtType
import app.atomofiron.searchboxapp.utils.resolve
import lib.atomofiron.insets.insetsPadding

class AboutDelegate(
    private val router: PreferenceRouter,
) : CurtainApi.Adapter<CurtainApi.ViewHolder>() {

    override fun getHolder(inflater: LayoutInflater, layoutId: Int): CurtainApi.ViewHolder {
        val binding = CurtainAboutBinding.inflate(inflater, null, false)
        binding.init()
        binding.root.insetsPadding(ExtType.curtain, vertical = true)
        return CurtainApi.ViewHolder(binding.root)
    }

    private fun CurtainAboutBinding.init() {
        val context = root.context
        var available = context.resolve(Intents.github)
        aboutTvGithub.isEnabled = available
        aboutTvGithub.alpha = Alpha.enabled(available)
        val tint = context.findColorByAttr(MaterialAttr.colorOnSurface)
        aboutTvGithub.compoundDrawablesRelative[0].setTint(tint)
        aboutTvForpda.compoundDrawablesRelative[0].setTint(tint)

        available = context.resolve(Intents.forPda)
        aboutTvForpda.isEnabled = available
        aboutTvForpda.alpha = Alpha.enabled(available)

        val listener = ::onClick
        aboutTvGithub.setOnClickListener(listener)
        aboutTvForpda.setOnClickListener(listener)
    }

    private fun onClick(view: View) {
        when (view.id) {
            R.id.about_tv_github -> router.goToGithub()
            R.id.about_tv_forpda -> router.goToForPda()
        }
    }
}