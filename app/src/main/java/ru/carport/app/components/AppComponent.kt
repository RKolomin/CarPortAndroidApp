package ru.carport.app.components

import dagger.Component
import ru.carport.app.MainActivity
import ru.carport.app.presenters.WebViewPresenter
import javax.inject.Singleton

@Component(modules = arrayOf(CommonModule::class, WebViewModule::class))
@Singleton
interface AppComponent {
    fun injectActivity(activity: MainActivity)

    fun injectPresenter(webViewPresenter: WebViewPresenter)
}