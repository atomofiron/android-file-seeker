package app.atomofiron.searchboxapp.screens.explorer.fragment.list.util

import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent


fun Node.getIcon(): Int {
    return when (val content = content) {
        is NodeContent.Unknown -> R.drawable.ic_explorer_unknown
        is NodeContent.Link -> R.drawable.ic_explorer_link
        is NodeContent.File -> content.getIcon()
        is NodeContent.Directory -> content.getIcon(isEmpty)
    }
}

fun NodeContent.File.getIcon(): Int = when (this) {
    is NodeContent.File.Music -> R.drawable.ic_explorer_music
    is NodeContent.File.Text.Svg -> R.drawable.ic_explorer_vector
    is NodeContent.File.Picture -> R.drawable.ic_explorer_picture
    is NodeContent.File.Movie -> R.drawable.ic_explorer_movie
    is NodeContent.File.AndroidApp -> R.drawable.ic_explorer_apk
    is NodeContent.File.Archive,
    is NodeContent.File.Dmg -> R.drawable.ic_explorer_archive_file
    is NodeContent.File.Text.Osu -> R.drawable.ic_file_osu
    is NodeContent.File.Text.ShellScript -> R.drawable.ic_explorer_script
    is NodeContent.File.Text.Ino -> R.drawable.ic_explorer_infinity
    is NodeContent.File.Text -> R.drawable.ic_explorer_text
    is NodeContent.File.Pdf -> R.drawable.ic_explorer_pdf
    is NodeContent.File.DataImage -> R.drawable.ic_explorer_dt
    is NodeContent.File.DB -> R.drawable.ic_explorer_db
    is NodeContent.File.Osu.Map,
    is NodeContent.File.Osu.Skin,
    is NodeContent.File.Osu.LazerMap,
    is NodeContent.File.Osu.Storyboard,
    is NodeContent.File.Osu.Replay -> R.drawable.ic_explorer_osu_map
    is NodeContent.File.Fap -> R.drawable.ic_dolphin
    is NodeContent.File.Torrent -> R.drawable.ic_explorer_download
    is NodeContent.File.ExeApl -> R.drawable.ic_apple
    is NodeContent.File.ExeApls -> R.drawable.ic_apple_s
    is NodeContent.File.Elf,
    is NodeContent.File.ElfSo -> R.drawable.ic_tux
    is NodeContent.File.ExeMs -> R.drawable.ic_microsoft
    is NodeContent.File.Cert -> R.drawable.ic_certificate
    is NodeContent.File.Xz,
    is NodeContent.File.Other,
    is NodeContent.File.Flash,
    is NodeContent.File.Empty,
    is NodeContent.File.Unknown -> R.drawable.ic_explorer_unknown
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
