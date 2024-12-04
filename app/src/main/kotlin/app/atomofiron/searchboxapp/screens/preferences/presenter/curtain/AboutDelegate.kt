package app.atomofiron.searchboxapp.screens.preferences.presenter.curtain

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.MaterialAttr
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.databinding.CurtainAboutBinding
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.ExtType
import app.atomofiron.searchboxapp.utils.resolve
import lib.atomofiron.insets.insetsPadding

class AboutDelegate : CurtainApi.Adapter<CurtainApi.ViewHolder>() {
    private val githubIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Const.GITHUB_URL))
    private val forpdaIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Const.FORPDA_URL))

    override fun getHolder(inflater: LayoutInflater, layoutId: Int): CurtainApi.ViewHolder {
        val binding = CurtainAboutBinding.inflate(inflater, null, false)
        binding.init()
        binding.root.insetsPadding(ExtType.curtain, vertical = true)
        return CurtainApi.ViewHolder(binding.root)
    }

    private fun CurtainAboutBinding.init() {
        val context = root.context
        var available = context.resolve(forpdaIntent)
        aboutTvGithub.isEnabled = available
        aboutTvGithub.alpha = Alpha.enabled(available)
        val tint = context.findColorByAttr(MaterialAttr.colorOnSurface)
        aboutTvGithub.compoundDrawablesRelative[0].setTint(tint)
        aboutTvForpda.compoundDrawablesRelative[0].setTint(tint)

        available = context.resolve(forpdaIntent)
        aboutTvForpda.isEnabled = available
        aboutTvForpda.alpha = Alpha.enabled(available)

        val listener = ::onClick
        aboutTvGithub.setOnClickListener(listener)
        aboutTvForpda.setOnClickListener(listener)
    }

    private fun onClick(view: View) {
        when (view.id) {
            R.id.about_tv_github -> view.context.startActivity(githubIntent)
            R.id.about_tv_forpda -> view.context.startActivity(forpdaIntent)
        }
    }
}