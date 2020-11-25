package no.fdk.dataset_catalog.utils

import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory

import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets

class TestResponseReader {

    private fun resourceAsReader(resourceName: String): Reader {
        return InputStreamReader(javaClass.classLoader.getResourceAsStream(resourceName)!!, StandardCharsets.UTF_8)
    }

    fun parseFile(filename: String, lang: String): Model {
        val expected = ModelFactory.createDefaultModel()
        expected.read(resourceAsReader(filename), "", lang)
        return expected
    }
}
