package no.nav.inntektsmeldingkontrakt

import java.time.LocalDate

data class OpphoerAvNaturalytelse (

        /** Type Naturalytelse som faller bort i stønadsperioden og som ikke blir erstattet skal meldes inn.  */
    val naturalytelse: Naturalytelse? = null,

        /** Må oppgis dersom naturalytelsestype angis. Fra og med dato naturalytelsen bortfaller, dvs. den datoen NAV skal
     * erstatte den bortfalte ytelsen.  */
    val fom: LocalDate? = null,

        /** Må oppgis dersom naturalytelsestype angis. Beløpet for naturalytelsen som faller bort. Beløpet oppgis som
     * månedsbeløp.
     */
    val beloepPrMnd: Float? = null

)
