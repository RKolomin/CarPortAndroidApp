package ru.carport.app.components

import dagger.Module
import dagger.Provides
import ru.carport.app.MainActivity
import ru.carport.app.presenters.ManagePermissionsPresenter
import ru.carport.app.presenters.WebViewPresenter

@Module
class CommonModule() {
    @Provides
    fun providesMainActivity(): MainActivity = MainActivity()

    @Provides
    fun providesManagePermissionsPresenter(mainActivity: MainActivity): ManagePermissionsPresenter =
        ManagePermissionsPresenter(
            mainActivity,
            WebViewPresenter.getPermissionList(),
            WebViewPresenter.PermissionsRequestCode
        )
}
