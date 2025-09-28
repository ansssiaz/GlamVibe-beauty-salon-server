package com.glamvibe.recommendations

import com.glamvibe.domain.model.Form
import com.glamvibe.domain.model.FormServiceRelation
import com.glamvibe.domain.model.ServiceRelation
import com.glamvibe.domain.repository.RecommendationsRepository
import smile.nlp.bag
import smile.nlp.tfidf
import smile.nlp.vectorize
import smile.math.MathEx.cosine
import com.glamvibe.utils.StemmerPorter.stem

import com.glamvibe.domain.repository.ServiceRepository

class RecommendationsService(
    private val serviceRepository: ServiceRepository,
    private val recommendationsRepository: RecommendationsRepository
) {

    private lateinit var serviceIds: List<Int>
    private lateinit var bagOfWordsList: List<Map<String, Int>>
    private lateinit var tfidfMatrix: List<DoubleArray>
    private lateinit var terms: Array<String>
    private val stopWords =
        setOf(
            "и", "в", "на", "с", "по", "к", "что", "как", "а",
            "но", "за", "при", "от", "или", "из", "это", "этот", "эта"
        )

    suspend fun initialize() {
        val services = serviceRepository.getAllServices()
        serviceIds = services.map { it.id }

        bagOfWordsList = services.map { service ->
            val text = listOf(service.category, service.description).joinToString(" ")
            val filteredWords = text.lowercase()
                .replace(Regex("\\p{Punct}"), "")
                .replace(Regex("\\d+"), "")
                .split("\\s+".toRegex())
                .filter { it.isNotBlank() && it !in stopWords }
                .joinToString(" ") { word -> stem(word) }

            filteredWords.bag()
        }

        terms = bagOfWordsList.flatMap { it.keys }.distinct().toTypedArray()

        val freqVectors = bagOfWordsList.map { bag ->
            val freqVector = vectorize(terms, bag)
            val totalWords = bag.values.sum()
            if (totalWords > 0)
                freqVector.map { it / totalWords.toDouble() }.toDoubleArray()
            else
                DoubleArray(terms.size) { 0.0 }
        }

        tfidfMatrix = tfidf(freqVectors)

        val relations = mutableListOf<ServiceRelation>()

        for (i in tfidfMatrix.indices) {
            for (j in i + 1 until tfidfMatrix.size) {
                val cosineSimilarity = cosine(tfidfMatrix[i], tfidfMatrix[j])

                if (cosineSimilarity > 0.0) {
                    relations.add(
                        ServiceRelation(
                            serviceId1 = services[i].id,
                            serviceId2 = services[j].id,
                            cosineSimilarity = cosineSimilarity
                        )
                    )
                }
            }
        }
        recommendationsRepository.saveServiceRelations(relations)
    }

    suspend fun createRecommendationsByClientFormData(form: Form) {
        val preparedClientFormData = form.clientData.lowercase()
            .replace(Regex("\\p{Punct}"), "")
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() && it !in stopWords }
            .joinToString(" ") { word -> stem(word) }

        val clientBag = preparedClientFormData.bag()

        val freqVector = vectorize(terms, clientBag)
        val totalWords = clientBag.values.sum()

        val clientVector = if (totalWords > 0) {
            freqVector.map { it / totalWords.toDouble() }.toDoubleArray()
        } else {
            DoubleArray(terms.size) { 0.0 }
        }

        val fullMatrix = tfidfMatrix + listOf(clientVector)
        val tfidfFull = tfidf(fullMatrix)
        val clientTfidfVector = tfidfFull.last()

        val similarities = tfidfFull.dropLast(1).mapIndexed { index, vector ->
            val sim = cosine(clientTfidfVector, vector)
            index to sim
        }

        val recommendations = similarities.sortedByDescending { it.second }
            .take(5)
            .map { (index, sim) ->
                FormServiceRelation(
                    formId = form.id,
                    serviceId = serviceIds[index],
                    cosineSimilarity = sim
                )
            }

        recommendationsRepository.saveFormServiceRelations(recommendations)
    }
}