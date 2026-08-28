package no.novari.flyt.integration.api.dto

data class CollectionMappingDto<T>(
    var elementMappings: MutableCollection<T> = mutableListOf(),
    var fromCollectionMappings: MutableCollection<FromCollectionMappingDto<T>> = mutableListOf(),
)
