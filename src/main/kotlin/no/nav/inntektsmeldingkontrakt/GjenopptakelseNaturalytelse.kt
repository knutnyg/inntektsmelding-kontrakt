package no.nav.inntektsmeldingkontrakt

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.math.BigDecimal
import java.time.LocalDate

data class GjenopptakelseNaturalytelse(

        /** Hvis arbeidstaker igjen skulle motta naturalytelsen så skal det oppgis hvilken naturalytelse dette gjelder.  */
        val naturalytelse: Naturalytelse? = null,

        /** Må oppgis dersom naturalytelsestype angis. Fra og med dato arbeidstaker igjen mottar naturalytelsen, dvs. den
         * datoen NAV ikke lengre skal erstatte den bortfalte ytelsen.  */
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        val fom: LocalDate? = null,

        /** ???  */
        @field: JsonSerialize(using = PengeSerialiserer::class)
        val beloepPrMnd: BigDecimal? = null


)