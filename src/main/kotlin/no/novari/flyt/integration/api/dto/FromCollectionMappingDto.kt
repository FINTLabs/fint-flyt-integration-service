package no.novari.flyt.integration.api.dto

data class FromCollectionMappingDto<T>(
    var instanceCollectionReferencesOrdered: MutableList<String> = mutableListOf(),
    var elementMapping: T? = null,
)
