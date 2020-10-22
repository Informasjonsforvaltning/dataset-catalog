package no.fdk.dataset_catalog.webutils.exceptions

abstract class FDKException : Exception {
    internal constructor() : super() {}
    internal constructor(message: String?) : super(message) {}
    internal constructor(message: String?, cause: Throwable?) : super(message, cause) {}
    internal constructor(cause: Throwable?) : super(cause) {}
}