package app.atomofiron.searchboxapp.screens.licenses.delegate

import android.view.LayoutInflater
import androidx.core.view.isVisible
import app.atomofiron.common.arch.Recipient
import app.atomofiron.fileseeker.databinding.CurtainLicenseBinding
import app.atomofiron.searchboxapp.android.WebClient
import app.atomofiron.searchboxapp.custom.drawable.MuonsDrawable.Companion.setMuonsDrawable
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.licenses.state.License
import app.atomofiron.searchboxapp.screens.licenses.state.LicenseContent
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.ExtType
import lib.atomofiron.insets.insetsPadding

class LicenseCurtainDelegate(
    private val webClient: WebClient,
    private val license: License,
) : CurtainApi.Adapter<CurtainApi.ViewHolder>(), Recipient {

    override fun getHolder(inflater: LayoutInflater, layoutId: Int): CurtainApi.ViewHolder {
        val binding = CurtainLicenseBinding.inflate(inflater)
        binding.title.text = license.name
        when (license.content) {
            is LicenseContent.Text -> binding.showText(license.content.value)
            is LicenseContent.Url -> binding.showUrl(license.content.value)
        }
        binding.root.insetsPadding(ExtType.curtain, vertical = true)
        return CurtainApi.ViewHolder(binding.root, isCancelable = true, unsureScrollable = false)
    }

    private fun CurtainLicenseBinding.showText(text: String) {
        scroll.isVisible = true
        this.text.text = text
    }

    private fun CurtainLicenseBinding.showUrl(url: String) {
        progress.isVisible = true
        progress.setMuonsDrawable()
        web.webViewClient = webClient
        webClient.onFinished {
            web.postDelayed({
                webContainer.isVisible = true
                progress.isVisible = false
            }, Const.LONG_DELAY)
        }
        web.loadUrl(url)
    }
}