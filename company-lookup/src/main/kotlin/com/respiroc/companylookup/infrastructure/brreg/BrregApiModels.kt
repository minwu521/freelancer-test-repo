package com.respiroc.companylookup.infrastructure.brreg

import com.fasterxml.jackson.annotation.JsonProperty

data class BrregSearchResponse(
    @JsonProperty("_embedded") val embedded: BrregEmbedded? = null,
    val page: PageInfo? = null
)

data class BrregEmbedded(
    @JsonProperty("enheter") val entities: List<BrregEntityBasic>? = null,
    @JsonProperty("underenheter") val subEntities: List<BrregEntityBasic>? = null
)

data class PageInfo(
    val size: Int,
    val totalElements: Int,
    val totalPages: Int,
    val number: Int
)

data class BrregEntityBasic(
    @JsonProperty("organisasjonsnummer") val organizationNumber: String,
    @JsonProperty("navn") val name: String,
    @JsonProperty("forretningsadresse") val businessAddress: BrregAddress? = null,
    @JsonProperty("beliggenhetsadresse") val locationAddress: BrregAddress? = null
)

data class BrregEntityResponse(
    @JsonProperty("organisasjonsnummer") val organizationNumber: String,
    @JsonProperty("navn") val name: String,
    @JsonProperty("forretningsadresse") val businessAddress: BrregAddress?,
    @JsonProperty("stiftelsesdato") val establishmentDate: String?,
    @JsonProperty("konkurs") val bankrupt: Boolean?,
    @JsonProperty("underAvvikling") val underLiquidation: Boolean?,
    @JsonProperty("underTvangsavviklingEllerTvangsopplosning") val compulsoryLiquidation: Boolean?,
    @JsonProperty("naeringskode1") val industryCode1: BrregIndustry?,
    @JsonProperty("epostadresse") val email: String?,
    @JsonProperty("telefon") val phone: String?,
    @JsonProperty("hjemmeside") val website: String?,
    @JsonProperty("sisteInnsendteAarsregnskap") val lastSubmittedAnnualReport: String?
)

data class BrregSubEntityResponse(
    @JsonProperty("organisasjonsnummer") val organizationNumber: String,
    @JsonProperty("navn") val name: String,
    @JsonProperty("beliggenhetsadresse") val locationAddress: BrregAddress?,
    @JsonProperty("oppstartsdato") val startupDate: String?,
    @JsonProperty("naeringskode1") val industryCode1: BrregIndustry?,
    @JsonProperty("epostadresse") val email: String?,
    @JsonProperty("telefon") val phone: String?,
    @JsonProperty("hjemmeside") val website: String?
)

data class BrregAddress(
    @JsonProperty("adresse") val addressLines: List<String>?,
    @JsonProperty("landkode") val countryCode: String?,
    @JsonProperty("poststed") val city: String?,
    @JsonProperty("postnummer") val postalCode: String?,
    @JsonProperty("kommune") val municipality: String?
)

data class BrregIndustry(
    @JsonProperty("beskrivelse") val description: String?
)