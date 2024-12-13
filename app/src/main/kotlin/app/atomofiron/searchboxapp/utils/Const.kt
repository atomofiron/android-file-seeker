package app.atomofiron.searchboxapp.utils

object Const {
    const val ROOT = "/"
    const val SLASH = "/"
    const val SPACE = " "
    const val EMPTY = ""
    const val QUOTE = "\""
    const val ANDROID_DIR = "/Android/"

    const val DEFAULT_TEXT_FORMATS = "txt java xml html htm smali log js css json kt md mkd markdown cm ad adoc"
    const val DEFAULT_SPECIAL_CHARACTERS = "\\ [ { ? + * ^ $"
    const val DEFAULT_MAX_SIZE = 10485760
    const val DEFAULT_MAX_DEPTH = 1024
    const val DEFAULT_EXPLORER_ITEM = 248
    const val DEFAULT_JOYSTICK = 16732754 // 0ff5252
    const val DEFAULT_TOYBOX_PATH = "/system/bin/toybox"

    const val SCHEME_FILE = "file"
    const val SCHEME_PACKAGE = "package"
    const val NULL = "null"
    const val MIME_TYPE_ANY = "*/*"

    const val UNDEFINED = -1
    const val UNDEFINEDL = -1L
    const val COMMON_DURATION = 512L
    const val COMMON_DELAY = 256L
    const val CONFIRM_DELAY = 2000L

    const val VALUE_TOYBOX_ARM_32 = "toybox_arm_32"
    const val VALUE_TOYBOX_ARM_64 = "toybox_arm_64"
    const val VALUE_TOYBOX_X86_64 = "toybox_x86_64"
    const val VALUE_TOYBOX_CUSTOM = "toybox_custom"
    const val VALUE_TOYBOX_IMPORTED = "toybox_imported"

    const val NOTIFICATION_CHANNEL_UPDATE_ID = "channel_update"
    const val FOREGROUND_NOTIFICATION_CHANNEL_ID = "foreground_channel_id"
    const val RESULT_NOTIFICATION_CHANNEL_ID = "result_channel_id"
    const val FOREGROUND_NOTIFICATION_ID = 101
    const val NOTIFICATION_ID_UPDATE = 9485

    const val DATE_PATTERN = "yyyy-MM-DD_HH-mm-ss"
    const val MIME_TYPE_TEXT = "text/plain"
    const val GITHUB_URL = "https://github.com/Atomofiron/android-search-box-app"
    const val FORPDA_URL = "https://4pda.ru/forum/index.php?showtopic=1000070&view=findpost&p=98557921"

    const val TOYBOX_32 = "/toybox32"
    const val TOYBOX_64 = "/toybox64"
    const val TOYBOX_86_64 = "/toybox86_64"
    const val TOYBOX_IMPORTED = "/toybox_imported"

    const val TEXT_FILE_PAGINATION_STEP = 128
    const val TEXT_FILE_PAGINATION_STEP_OFFSET = 16

    const val ERROR_UPDATE_AVAILABILITY = "Update is unavailable."
    const val ERROR_CHECK_UPDATE = "Failed to check updates."
}