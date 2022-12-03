package app.atomofiron.searchboxapp.screens.explorer.fragment.list.util

import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.model.explorer.NodeContent


fun NodeContent.File.getIcon(): Int = when (this) {
    is NodeContent.File.Music -> R.drawable.ic_explorer_music
    is NodeContent.File.Picture -> R.drawable.ic_explorer_picture
    is NodeContent.File.Movie -> R.drawable.ic_explorer_movie
    is NodeContent.File.Apk -> R.drawable.ic_explorer_apk
    is NodeContent.File.Archive -> R.drawable.ic_explorer_archive_file
    is NodeContent.File.Text.Script -> R.drawable.ic_explorer_script
    is NodeContent.File.Text -> R.drawable.ic_explorer_text
    is NodeContent.File.Pdf -> R.drawable.ic_explorer_pdf
    is NodeContent.File.DataImage -> R.drawable.ic_explorer_dt
    is NodeContent.File.DB -> R.drawable.ic_explorer_db
    is NodeContent.File.Other -> R.drawable.ic_file_circle
    is NodeContent.File.Unknown -> R.drawable.ic_file_circle
}

fun NodeContent.Directory.getIcon(isEmpty: Boolean): Int = when (type) {
    NodeContent.Directory.Type.Android -> when {
        isEmpty -> R.drawable.ic_explorer_folder_android_empty
        else -> R.drawable.ic_explorer_folder_android
    }
    NodeContent.Directory.Type.Camera -> when {
        isEmpty -> R.drawable.ic_explorer_folder_camera_empty
        else -> R.drawable.ic_explorer_folder_camera
    }
    NodeContent.Directory.Type.Download -> when {
        isEmpty -> R.drawable.ic_explorer_folder_download_empty
        else -> R.drawable.ic_explorer_folder_download
    }
    NodeContent.Directory.Type.Movies -> when {
        isEmpty -> R.drawable.ic_explorer_folder_movies_empty
        else -> R.drawable.ic_explorer_folder_movies
    }
    NodeContent.Directory.Type.Music -> when {
        isEmpty -> R.drawable.ic_explorer_folder_music_empty
        else -> R.drawable.ic_explorer_folder_music
    }
    NodeContent.Directory.Type.Pictures -> when {
        isEmpty -> R.drawable.ic_explorer_folder_pictures_empty
        else -> R.drawable.ic_explorer_folder_pictures
    }
    else -> when {
        isEmpty -> R.drawable.ic_explorer_folder_empty
        else -> R.drawable.ic_explorer_folder
    }
}
