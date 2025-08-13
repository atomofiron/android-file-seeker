package app.atomofiron.searchboxapp.screens.explorer.fragment.list.util

import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.explorer.DirectoryKind
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent


fun Node.getIcon(): Int {
    return when (val content = content) {
        is NodeContent.Undefined -> R.drawable.ic_explorer_unknown
        is NodeContent.Directory -> content.getIcon(isEmpty == true)
        is NodeContent.File -> content.getIcon()
        is NodeContent.Link -> R.drawable.ic_explorer_link
    }
}

fun NodeContent.File.getIcon(): Int = when (this) {
    is NodeContent.Music -> R.drawable.ic_explorer_music
    is NodeContent.Text.Svg -> R.drawable.ic_explorer_vector
    is NodeContent.Picture -> R.drawable.ic_explorer_picture
    is NodeContent.Movie -> R.drawable.ic_explorer_movie
    is NodeContent.AndroidApp -> R.drawable.ic_explorer_apk
    is NodeContent.Archive,
    is NodeContent.Dmg -> R.drawable.ic_explorer_archive_file
    is NodeContent.Text.Osu -> R.drawable.ic_file_osu
    is NodeContent.Text.ShellScript -> R.drawable.ic_explorer_script
    is NodeContent.Text.Ino -> R.drawable.ic_explorer_infinity
    is NodeContent.Text -> R.drawable.ic_explorer_text
    is NodeContent.Pdf -> R.drawable.ic_explorer_pdf
    is NodeContent.DataImage -> R.drawable.ic_explorer_dt
    is NodeContent.DB -> R.drawable.ic_explorer_db
    is NodeContent.Osu.Map,
    is NodeContent.Osu.Skin,
    is NodeContent.Osu.LazerMap,
    is NodeContent.Osu.Storyboard,
    is NodeContent.Osu.Replay -> R.drawable.ic_explorer_osu_map
    is NodeContent.Fap -> R.drawable.ic_dolphin
    is NodeContent.Torrent -> R.drawable.ic_explorer_download
    is NodeContent.ExeApl -> R.drawable.ic_apple
    is NodeContent.ExeApls -> R.drawable.ic_apple_s
    is NodeContent.Elf,
    is NodeContent.ElfSo -> R.drawable.ic_tux
    is NodeContent.ExeMs -> R.drawable.ic_microsoft
    is NodeContent.Cert -> R.drawable.ic_certificate
    is NodeContent.Java -> R.drawable.ic_explorer_binary
    is NodeContent.Xz,
    is NodeContent.Other,
    is NodeContent.Flash,
    is NodeContent.Empty -> R.drawable.ic_explorer_empty
    is NodeContent.Unknown -> R.drawable.ic_explorer_unknown
}

fun NodeContent.Directory.getIcon(isEmpty: Boolean): Int = when (kind) {
    DirectoryKind.Alarms -> when {
        isEmpty -> R.drawable.ic_explorer_folder_alarms_empty
        else -> R.drawable.ic_explorer_folder_alarms
    }
    DirectoryKind.Android -> when {
        isEmpty -> R.drawable.ic_explorer_folder_android_empty
        else -> R.drawable.ic_explorer_folder_android
    }
    DirectoryKind.Camera -> when {
        isEmpty -> R.drawable.ic_explorer_folder_camera_empty
        else -> R.drawable.ic_explorer_folder_camera
    }
    DirectoryKind.Download -> when {
        isEmpty -> R.drawable.ic_explorer_folder_download_empty
        else -> R.drawable.ic_explorer_folder_download
    }
    DirectoryKind.Movies -> when {
        isEmpty -> R.drawable.ic_explorer_folder_movies_empty
        else -> R.drawable.ic_explorer_folder_movies
    }
    DirectoryKind.Music -> when {
        isEmpty -> R.drawable.ic_explorer_folder_music_empty
        else -> R.drawable.ic_explorer_folder_music
    }
    DirectoryKind.Pictures -> when {
        isEmpty -> R.drawable.ic_explorer_folder_pictures_empty
        else -> R.drawable.ic_explorer_folder_pictures
    }
    DirectoryKind.Ringtones -> when {
        isEmpty -> R.drawable.ic_explorer_folder_bell_empty
        else -> R.drawable.ic_explorer_folder_bell
    }
    DirectoryKind.Ordinary -> when {
        isEmpty -> R.drawable.ic_explorer_folder_empty
        else -> R.drawable.ic_explorer_folder
    }
}
