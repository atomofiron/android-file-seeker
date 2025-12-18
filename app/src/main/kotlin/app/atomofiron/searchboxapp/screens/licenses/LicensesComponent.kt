package app.atomofiron.searchboxapp.screens.licenses

import android.content.res.AssetManager
import androidx.fragment.app.Fragment
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.android.WebClient
import app.atomofiron.searchboxapp.di.dependencies.channel.CurtainChannel
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import kotlinx.coroutines.CoroutineScope
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention
annotation class LicensesScope

@LicensesScope
@Component(dependencies = [LicensesDependencies::class], modules = [LicensesModule::class])
interface LicensesComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun bind(scope: CoroutineScope): Builder
        @BindsInstance
        fun bind(view: WeakProperty<out Fragment>): Builder
        fun dependencies(dependencies: LicensesDependencies): Builder
        fun build(): LicensesComponent
    }

    fun inject(target: LicensesViewModel)
}

@Module
interface LicensesModule

interface LicensesDependencies {
    fun assets(): AssetManager
    fun webClient(): WebClient
    fun curtainChannel(): CurtainChannel
}
