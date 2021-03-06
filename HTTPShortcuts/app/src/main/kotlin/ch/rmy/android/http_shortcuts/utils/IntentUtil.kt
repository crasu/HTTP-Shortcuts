package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.models.Shortcut

object IntentUtil {

    private const val ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT"
    private const val ACTION_UNINSTALL_SHORTCUT = "com.android.launcher.action.UNINSTALL_SHORTCUT"
    private const val EXTRA_SHORTCUT_DUPLICATE = "duplicate"

    fun getShortcutId(intent: Intent): String =
        intent.getStringExtra(ExecuteActivity.EXTRA_SHORTCUT_ID)
            ?: intent.data?.lastPathSegment
            ?: ""

    fun getVariableValues(intent: Intent): Map<String, String> {
        val serializable = intent.getSerializableExtra(ExecuteActivity.EXTRA_VARIABLE_VALUES)
        if (serializable is Map<*, *>) {
            @Suppress("UNCHECKED_CAST")
            return serializable as Map<String, String>
        }
        return emptyMap()
    }

    @Suppress("DEPRECATION")
    fun getLegacyShortcutPlacementIntent(context: Context, shortcut: Shortcut, install: Boolean): Intent {
        val shortcutIntent = ExecuteActivity.IntentBuilder(context, shortcut.id)
            .build()
        val addIntent = Intent()
            .putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            .putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcut.name)
            .putExtra(EXTRA_SHORTCUT_DUPLICATE, true)
        if (shortcut.iconName != null) {
            val iconUri = IconUtil.getIconURI(context, shortcut.iconName, external = true)
            try {
                val scaledIcon = MediaStore.Images.Media.getBitmap(context.contentResolver, iconUri)
                val size = IconUtil.getIconSize(context, scaled = false)
                val unscaledIcon = Bitmap.createScaledBitmap(scaledIcon, size, size, false)
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, unscaledIcon)
            } catch (e: Exception) {
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context.applicationContext, IconUtil.DEFAULT_ICON))
            }
        } else {
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context.applicationContext, IconUtil.DEFAULT_ICON))
        }

        addIntent.action = if (install) ACTION_INSTALL_SHORTCUT else ACTION_UNINSTALL_SHORTCUT

        return addIntent
    }

}
