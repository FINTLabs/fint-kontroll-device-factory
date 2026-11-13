package no.novari.fintkontrolldevicefactory

class LinkUtils {
    companion object {
        fun getSystemIdFromPath(path: String): String = path.substring(path.lastIndexOf('/') + 1)

        fun getSystemIdFromMessageKey(path: String): String = path.substringAfterLast('/')
    }
}
