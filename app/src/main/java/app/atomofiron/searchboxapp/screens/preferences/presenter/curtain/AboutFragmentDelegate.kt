package app.atomofiron.searchboxapp.screens.preferences.presenter.curtain

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.databinding.CurtainAboutBinding
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.utils.Const
import lib.atomofiron.android_window_insets_compat.ViewInsetsController

class AboutFragmentDelegate : CurtainApi.Adapter<CurtainApi.ViewHolder>() {
    companion object {
        private const val ALPHA_ENABLED = 1f
        private const val ALPHA_DISABLED = 0.5f
    }
    private val githubIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Const.GITHUB_URL))
    private val forpdaIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Const.FORPDA_URL))

    override fun getHolder(inflater: LayoutInflater, container: ViewGroup, layoutId: Int): CurtainApi.ViewHolder {
        val binding = CurtainAboutBinding.inflate(inflater, container, false)
        binding.init()
        ViewInsetsController.bindPadding(binding.root, top = true, bottom = true)
        return CurtainApi.ViewHolder(binding.root)
    }

    private fun CurtainAboutBinding.init() {
        val context = root.context
        // todo requires
        var componentName = githubIntent.resolveActivity(context.packageManager)
        aboutTvGithub.isEnabled = componentName != null
        aboutTvGithub.alpha = if (componentName == null) ALPHA_DISABLED else ALPHA_ENABLED
        val tint = context.findColorByAttr(R.attr.colorPositive)
        aboutTvGithub.compoundDrawablesRelative[0].setTint(tint)
        aboutTvForpda.compoundDrawablesRelative[0].setTint(tint)

        componentName = forpdaIntent.resolveActivity(context.packageManager)
        aboutTvForpda.isEnabled = componentName != null
        aboutTvForpda.alpha = if (componentName == null) ALPHA_DISABLED else ALPHA_ENABLED

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