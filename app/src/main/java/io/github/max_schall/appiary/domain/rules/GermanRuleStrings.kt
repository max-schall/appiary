package io.github.max_schall.appiary.domain.rules

import io.github.max_schall.appiary.domain.model.MiteResult
import io.github.max_schall.appiary.domain.season.SeasonPhase

/** German text for the rules engine. */
object GermanRuleStrings : RuleStrings {

    /** Dative day phrase ("seit/in/innerhalb von N Tagen"). */
    private fun dat(n: Long) = if (n == 1L) "1 Tag" else "$n Tagen"

    override fun daysAgo(days: Long) = when {
        days <= 0 -> "heute"
        days == 1L -> "gestern"
        else -> "vor $days Tagen"
    }

    override fun inDays(days: Long) = when {
        days < 0 -> "vor ${-days} Tagen"
        days == 0L -> "heute"
        days == 1L -> "morgen"
        else -> "in $days Tagen"
    }

    override fun dayCount(days: Long) = if (days == 1L) "1 Tag" else "$days Tage"

    override fun inspectTitle(hiveName: String) = "$hiveName kontrollieren"
    override fun inspectShort(overdue: Boolean, days: Long) =
        if (overdue) "Überfällig seit ${dat(days)}" else "Fällig ${inDays(days)}"
    override fun inspectExplanation(neverInspected: Boolean, daysSince: Long, intervalDays: Int, queenUnsure: Boolean): String {
        val base = if (neverInspected) {
            "Dieses Volk wurde seit der Einrichtung ${daysAgo(daysSince)} nicht kontrolliert"
        } else {
            "Dieses Volk wurde zuletzt ${daysAgo(daysSince)} kontrolliert"
        }
        return buildString {
            append(base); append("; ")
            append("das empfohlene Intervall beträgt ${dayCount(intervalDays.toLong())}.")
            if (queenUnsure) append(" Außerdem ist der Königinnenstatus unklar – ein Blick lohnt sich.")
        }
    }

    override fun queenTitle(hiveName: String, queenless: Boolean) =
        if (queenless) "Königin von $hiveName prüfen" else "Königin in $hiveName bestätigen"
    override fun queenShort(queenless: Boolean) = if (queenless) "Evtl. weisellos" else "Königin unbestätigt"
    override fun queenExplanation(queenless: Boolean, daysSince: Long, followUpDays: Int) = if (queenless) {
        "Bei der letzten Kontrolle (${daysAgo(daysSince)}) wurden weder Königin noch Eier gesehen, " +
            "daher ist das Volk möglicherweise weisellos. Bitte prüfen und ggf. umweiseln."
    } else {
        "Die Königin wurde bei der letzten Kontrolle (${daysAgo(daysSince)}) nicht bestätigt. " +
            "Innerhalb von ${dat(followUpDays.toLong())} erneut prüfen, um eine legende Königin zu bestätigen, " +
            "bevor das Volk zurückfällt."
    }

    override fun repeatedQueenTitle(hiveName: String) = "Königinnenproblem in $hiveName lösen"
    override fun repeatedQueenShort(streak: Int) = "$streak Kontrollen ohne bestätigte Königin"
    override fun repeatedQueenExplanation(streak: Int, spanDays: Long) =
        "Bei den letzten $streak Kontrollen (über ${dat(spanDays)}) wurde die Königin nicht bestätigt. " +
            "Das deutet meist auf eine fehlende oder versagende Königin hin – umweiseln, eine Wabe mit Eiern geben oder vereinigen."

    override fun swarmTitle(hiveName: String) = "Schwarmgefahr in $hiveName"
    override fun swarmShort(cells: Boolean) = if (cells) "Weiselzellen gesehen" else "Schwarmanzeichen gesehen"
    override fun swarmExplanation(cells: Boolean, bothCellsAndSigns: Boolean, daysSince: Long, followUpDays: Int): String {
        val cue = when {
            bothCellsAndSigns -> "Weiselzellen und weitere Schwarmanzeichen"
            cells -> "Weiselzellen"
            else -> "Schwarmvorbereitungen"
        }
        return buildString {
            append("Bei der letzten Kontrolle (${daysAgo(daysSince)}) wurden $cue festgestellt. ")
            if (cells) {
                append("Ein Volk mit Weiselzellen kann innerhalb weniger Tage schwärmen – zeitnah durchsehen ")
                append("und über Ableger, Entfernen der Zellen oder mehr Platz entscheiden.")
            } else {
                append("Innerhalb von ${dat(followUpDays.toLong())} erneut prüfen und für Platz sorgen, um das Schwärmen zu verhindern.")
            }
        }
    }

    override fun feedTitle(hiveName: String) = "$hiveName füttern"
    override fun feedShort() = "Futtervorrat niedrig"
    override fun lowStoresExplanation(daysSince: Long?, weak: Boolean, offSeason: Boolean): String {
        val seen = daysSince?.let { " (gesehen ${daysAgo(it)})" } ?: ""
        val extra = when {
            weak -> " Das Volk ist zudem schwach und hat kaum Reserven."
            offSeason -> " Außerhalb der Saison gibt es kaum Tracht zur Erholung."
            else -> ""
        }
        return "Bei der letzten Kontrolle war der Futtervorrat niedrig$seen.$extra " +
            "Sirup oder Futterteig geben und den Vorrat beim nächsten Besuch erneut prüfen."
    }

    override fun miteCheckTitle(hiveName: String) = "Varroakontrolle $hiveName"
    override fun miteOverdueShort(never: Boolean, overdueDays: Long) =
        if (never) "Noch nie geprüft" else "Überfällig seit ${dat(overdueDays)}"
    override fun miteOverdueExplanation(never: Boolean, daysSince: Long, intervalDays: Int, priorHighResult: MiteResult?): String {
        val history = if (never) "Für dieses Volk wurde noch keine Varroakontrolle erfasst"
        else "Die letzte Varroakontrolle war ${daysAgo(daysSince)}"
        val prior = priorHighResult?.let { " Der letzte Wert war ${miteResultLower(it)}, also nicht schleifen lassen." } ?: ""
        return "$history; das Kontrollintervall beträgt ${dayCount(intervalDays.toLong())}.$prior " +
            "Eine Auswaschprobe oder Puderzuckermethode durchführen."
    }

    override fun postTreatmentTitle(hiveName: String) = "Varroakontrolle nach Behandlung · $hiveName"
    override fun postTreatmentShort(daysUntil: Long) = if (daysUntil <= 0) "Kontrolle fällig" else "Kontrolle ${inDays(daysUntil)}"
    override fun postTreatmentExplanation(daysUntil: Long, endedDays: Long?): String {
        val ended = endedDays?.let { " Behandlung endete ${daysAgo(it)}." } ?: ""
        return "Eine Varroakontrolle ist ${inDays(daysUntil)} fällig, um den Behandlungserfolg zu bestätigen.$ended " +
            "Bei weiterhin hohem Befall erneut behandeln."
    }

    override fun weakTitle(hiveName: String) = "$hiveName beobachten"
    override fun weakShort() = "Schwaches Volk"
    override fun weakExplanation(daysSince: Long?): String {
        val seen = daysSince?.let { " (zuletzt gesehen ${daysAgo(it)})" } ?: ""
        return "Dieses Volk wurde als schwach eingestuft$seen. Entwicklung im Auge behalten; " +
            "ggf. Flugloch verengen, Brutnest einengen oder vereinigen, falls es sich nicht erholt."
    }

    override fun harvestTitle(hiveName: String) = "Erntevorbereitung · $hiveName"
    override fun harvestShort(inWindow: Boolean) = if (inWindow) "Erntereif" else "Ernte vorbereiten"
    override fun harvestExplanation(inWindow: Boolean): String {
        val phase = if (inWindow) "Die Erntezeit hat begonnen" else "Die Erntezeit naht"
        return "$phase, und dieses Volk ist stark mit vollen Vorräten. Verdeckelten Honig prüfen und Aufsetzen oder Schleudern planen."
    }

    override fun manualShort(overdue: Boolean, daysUntil: Long) =
        if (overdue) "Überfällige Aufgabe" else "Aufgabe fällig ${inDays(daysUntil)}"
    override fun manualExplanation(overdue: Boolean, daysUntil: Long, details: String?) = buildString {
        append("Du hast diese Aufgabe angelegt")
        append(if (overdue) ", sie wurde ${inDays(daysUntil)} fällig. " else ", fällig ${inDays(daysUntil)}. ")
        details?.takeIf { it.isNotBlank() }?.let { append(it) }
    }.trim()

    override fun miteResultLower(result: MiteResult) = when (result) {
        MiteResult.LOW -> "niedrig"
        MiteResult.MODERATE -> "moderat"
        MiteResult.HIGH -> "hoch"
        MiteResult.CRITICAL -> "kritisch"
    }

    override fun seasonalTitle(phase: SeasonPhase) = when (phase) {
        SeasonPhase.WINTER -> "Winterpflege"
        SeasonPhase.SPRING_BUILDUP -> "Frühjahrsentwicklung"
        SeasonPhase.SWARM_AND_FLOW -> "Schwarmzeit & Haupttracht"
        SeasonPhase.SUMMER_HARVEST -> "Ernte & erste Varroabehandlung"
        SeasonPhase.AUTUMN_PREP -> "Volk einwintern"
    }

    override fun seasonalShort(phase: SeasonPhase) = when (phase) {
        SeasonPhase.WINTER -> "In Ruhe lassen"
        SeasonPhase.SPRING_BUILDUP -> "Erste Durchsichten"
        SeasonPhase.SWARM_AND_FLOW -> "Wöchentlich kontrollieren, Platz geben"
        SeasonPhase.SUMMER_HARVEST -> "Ernten, dann behandeln"
        SeasonPhase.AUTUMN_PREP -> "Füttern, behandeln, einwintern"
    }

    override fun seasonalExplanation(phase: SeasonPhase) = when (phase) {
        SeasonPhase.WINTER ->
            "Das Volk ungestört lassen und den Futtervorrat durch Anheben prüfen; im Notfall füttern. Wenn " +
                "das Volk brutfrei ist (oft Dez–Jän, idealerweise um den Gefrierpunkt), die Oxalsäure-Winterbehandlung durchführen."
        SeasonPhase.SPRING_BUILDUP ->
            "Am ersten warmen Tag die erste vollständige Durchsicht: Königin und Futter prüfen, Boden reinigen, " +
                "Brutnest einengen und eine Drohnenwabe geben. Mit der Schwarmkontrolle beginnen."
        SeasonPhase.SWARM_AND_FLOW ->
            "Während der Haupttracht alle 7 Tage auf Weiselzellen kontrollieren, rechtzeitig Honigräume aufsetzen " +
                "und Ableger oder Königinnenzucht erwägen. Das Ausschneiden von Drohnenbrut senkt den Varroadruck."
        SeasonPhase.SUMMER_HARVEST ->
            "Sommerernte einbringen und Honigräume abnehmen, dann direkt nach der letzten Ernte gegen Varroa behandeln. " +
                "Mit der Fütterung auf ~15–18 kg Wintervorrat beginnen; bei Hitze Wasser geben und Fluglöcher gegen Räuberei einengen."
        SeasonPhase.AUTUMN_PREP ->
            "Fütterung bis Ende September abschließen und den Behandlungserfolg prüfen. Schwache Völker vereinigen " +
                "oder auflösen, bei Bedarf umweiseln, Mäusegitter anbringen und die Bienen nicht mehr stören."
    }

    override fun flowImminentTitle() = "Tracht steht bevor"
    override fun flowImminentShort() = "Aufsetzen vorbereiten"
    override fun flowImminentExplanation(monthsToNext: Int?): String {
        val whenText = when (monthsToNext) {
            null -> ""
            1 -> " (in etwa einem Monat)"
            else -> " (in etwa $monthsToNext Monaten)"
        }
        return "Eine Haupttracht steht bevor$whenText. Sorge für starke, aufgesetzte Völker und verstärke die " +
            "Schwarmkontrolle, damit sie während der Tracht nicht schwärmen."
    }
    override fun dearthTitle() = "Trachtlücke"
    override fun dearthShort() = "Lücke zwischen Trachten"
    override fun dearthExplanation() =
        "Aktuell blüht wenig. Fluglöcher gegen Räuberei einengen und den Futtervorrat prüfen, bei Bedarf füttern. " +
            "Ohne Honigräume ist es zudem ein guter Zeitpunkt für Varroakontrolle und -behandlung."

    override fun inspectionWeatherTitle() = "Gutes Durchsichtswetter"
    override fun inspectionWeatherShort() = "Warm, windstill, trocken"
    override fun inspectionWeatherExplanation(dayOffset: Int) =
        "Die Bedingungen sind ${inDays(dayOffset.toLong())} gut für eine Durchsicht – warm, windstill und trocken. " +
            "Ein gutes Zeitfenster, um die Völker schonend zu bearbeiten."
    override fun treatmentWeatherTitle() = "Hitzewelle steht bevor"
    override fun treatmentWeatherShort() = "Varroabehandlung beachten"
    override fun treatmentWeatherExplanation(maxTempC: Double) =
        "Es werden Höchstwerte um ${"%.0f".format(maxTempC)} °C erwartet. Ameisensäure kann bei großer Hitze dem " +
            "Volk schaden – auf kühlere Tage warten oder eine hitzetolerante Methode wählen (z. B. Oxalsäure bei Brutfreiheit)."
    override fun coldSnapTitle() = "Kälteeinbruch steht bevor"
    override fun coldSnapShort() = "Futter & Schutz prüfen"
    override fun coldSnapExplanation(minTempC: Double) =
        "Für mehrere Tage werden Tiefstwerte um ${"%.0f".format(minTempC)} °C erwartet. Sorge für ausreichend Futter " +
            "und Windschutz und öffne die Völker vorerst nicht."

    override fun bestandsbuchTitle() = "Bestandsbuch: Belege ergänzen"
    override fun bestandsbuchShort(missingCount: Int) =
        if (missingCount == 1) "1 Behandlung ohne Beleg" else "$missingCount Behandlungen ohne Beleg"
    override fun bestandsbuchExplanation(missingCount: Int) =
        "Das Bestandsbuch (EU 2019/6 Art. 108) verlangt für jedes angewendete Tierarzneimittel einen Erwerbsbeleg – " +
            "auch für Ameisen- und Oxalsäure. $missingCount Behandlung(en) hier ist/sind noch kein Beleg zugeordnet. " +
            "Lieferant und ein Foto des Belegs erfassen und das Bestandsbuch bei Bedarf als PDF exportieren."
}
