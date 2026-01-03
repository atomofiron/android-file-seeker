package app.atomofiron.searchboxapp.screens.preferences.presenter.curtain

import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.CurtainAboutBinding
import app.atomofiron.searchboxapp.android.Intents
import app.atomofiron.searchboxapp.custom.drawable.NoticeableDrawable
import app.atomofiron.searchboxapp.custom.drawable.NoticeableDrawable.Placement
import app.atomofiron.searchboxapp.model.AppSource
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainId
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.preferences.PreferenceRouter
import app.atomofiron.searchboxapp.screens.preferences.PreferenceScope
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.ExtType
import app.atomofiron.searchboxapp.utils.colorAttr
import app.atomofiron.searchboxapp.utils.drawableEnd
import app.atomofiron.searchboxapp.utils.drawableStart
import app.atomofiron.searchboxapp.utils.resolve
import app.atomofiron.searchboxapp.utils.resources
import app.atomofiron.searchboxapp.utils.updateDrawables
import lib.atomofiron.insets.insetsPadding
import javax.inject.Inject

@PreferenceScope
class AboutDelegate @Inject constructor(
    private val router: PreferenceRouter,
    private val appSource: AppSource,
    private val packageManager: PackageManager,
) : CurtainApi.Adapter<CurtainApi.ViewHolder>() {

    private var telegramInfo: ResolveInfo? = null

    override fun getHolder(inflater: LayoutInflater, id: CurtainId): CurtainApi.ViewHolder {
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
        val rusted = NoticeableDrawable(icon.drawable, Color.TRANSPARENT, Placement.BottomEnd)
        rusted.forceShowDot(true)
        icon.setImageDrawable(rusted)
        version.setCompoundDrawablesRelativeWithIntrinsicBounds(versionIcon, 0, 0, 0)
        val context = root.context
        var available = context.resolve(Intents.github)
        github.isEnabled = available
        github.alpha = Alpha.enabled(available)
        val tint = context.colorAttr(R.attr.colorContent)
        github.drawableStart?.setTint(tint)
        github.drawableEnd?.setTint(tint)
        discuss.drawableEnd?.setTint(tint)
        resolveTelegramInfo()?.also { icon ->
            val size = resources.getDimensionPixelSize(R.dimen.icon_size)
            icon.setBounds(0, 0, size, size)
            discuss.updateDrawables(start = icon)
        } ?: discuss.drawableStart?.setTint(tint)

        available = context.resolve(Intents.forPda)
        discuss.isEnabled = available
        discuss.alpha = Alpha.enabled(available)

        github.setOnClickListener { router.goToGithub() }
        discuss.setOnClickListener {
            telegramInfo
                ?.let { router.goTelegram(it) }
                ?: router.goToForPda()
        }
        licenses.setOnClickListener { router.goToLicenses() }
    }

    fun resolveTelegramInfo(): Drawable? {
        val list = packageManager.queryIntentActivities(Intents.telegram, 0)
        telegramInfo = list.find { it.activityInfo.packageName == Const.TELEGRAM_PACKAGE_NAME }
            ?: list.find { it.activityInfo.packageName.contains(Const.GRAM) }
        return telegramInfo?.loadIcon(packageManager)
    }
}