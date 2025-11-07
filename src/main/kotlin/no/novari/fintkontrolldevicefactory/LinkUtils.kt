package no.novari.fintkontrolldevicefactory

class LinkUtils {

    companion object {
        fun getSystemIdFromPath(path: String): String {

            return path.substring(path.lastIndexOf('/') + 1)
        }
    }
}