package io.github.max_schall.appiary.domain.rules

import io.github.max_schall.appiary.domain.model.MiteResult
import io.github.max_schall.appiary.domain.season.SeasonPhase

/** English text for the rules engine (also the default used by unit tests). */
object DefaultRuleStrings : RuleStrings {

    override fun daysAgo(days: Long) = when {
        days <= 0 -> "today"
        days == 1L -> "yesterday"
        else -> "$days days ago"
    }

    override fun inDays(days: Long) = when {
        days < 0 -> "${-days} days ago"
        days == 0L -> "today"
        days == 1L -> "tomorrow"
        else -> "in $days days"
    }

    override fun dayCount(days: Long) = if (days == 1L) "1 day" else "$days days"

    override fun inspectTitle(hiveName: String) = "Inspect $hiveName"
    override fun inspectShort(overdue: Boolean, days: Long) =
        if (overdue) "Overdue by ${dayCount(days)}" else "Due ${inDays(days)}"
    override fun inspectExplanation(neverInspected: Boolean, daysSince: Long, intervalDays: Int, queenUnsure: Boolean): String {
        val base = if (neverInspected) {
            "This colony has not been inspected since it was set up ${daysAgo(daysSince)}"
        } else {
            "This hive was last inspected ${daysAgo(daysSince)}"
        }
        return buildString {
            append(base); append("; ")
            append("the recommended interval is ${dayCount(intervalDays.toLong())}.")
            if (queenUnsure) append(" The queen status is also unconfirmed, so a look is worthwhile.")
        }
    }

    override fun queenTitle(hiveName: String, queenless: Boolean) =
        if (queenless) "Check queen status of $hiveName" else "Confirm queen in $hiveName"
    override fun queenShort(queenless: Boolean) = if (queenless) "Possibly queenless" else "Queen unconfirmed"
    override fun queenExplanation(queenless: Boolean, daysSince: Long, followUpDays: Int) = if (queenless) {
        "No queen and no eggs were seen at the last inspection (${daysAgo(daysSince)}), " +
            "so this colony may be queenless. Confirm and consider requeening."
    } else {
        "The queen was not confirmed at the last inspection (${daysAgo(daysSince)}). " +
            "Re-check within ${dayCount(followUpDays.toLong())} to confirm a laying queen before the colony loses ground."
    }

    override fun repeatedQueenTitle(hiveName: String) = "Resolve queen issue in $hiveName"
    override fun repeatedQueenShort(streak: Int) = "$streak visits without a confirmed queen"
    override fun repeatedQueenExplanation(streak: Int, spanDays: Long) =
        "The queen has not been confirmed across the last $streak inspections " +
            "(spanning ${dayCount(spanDays)}). This usually means a failing or missing queen — " +
            "decide whether to requeen, add a frame of eggs, or combine."

    override fun swarmTitle(hiveName: String) = "Swarm risk in $hiveName"
    override fun swarmShort(cells: Boolean) = if (cells) "Queen cells seen" else "Swarm signs seen"
    override fun swarmExplanation(cells: Boolean, bothCellsAndSigns: Boolean, daysSince: Long, followUpDays: Int): String {
        val cue = when {
            bothCellsAndSigns -> "queen cells and other swarm signs"
            cells -> "queen cells"
            else -> "swarm preparations"
        }
        return buildString {
            append("The last inspection (${daysAgo(daysSince)}) recorded $cue. ")
            if (cells) {
                append("A colony with queen cells can swarm within days — inspect promptly and decide on ")
                append("splitting, removing cells, or giving space.")
            } else {
                append("Re-check within ${dayCount(followUpDays.toLong())} and manage for space to head off a swarm.")
            }
        }
    }

    override fun feedTitle(hiveName: String) = "Feed $hiveName"
    override fun feedShort() = "Food stores low"
    override fun lowStoresExplanation(daysSince: Long?, weak: Boolean, offSeason: Boolean): String {
        val seen = daysSince?.let { " (seen ${daysAgo(it)})" } ?: ""
        val extra = when {
            weak -> " The colony is also weak, so it has little reserve."
            offSeason -> " Outside the active season there is little forage to recover from."
            else -> ""
        }
        return "Food stores were low at the last inspection$seen.$extra " +
            "Feed syrup or fondant and re-check stores on the next visit."
    }

    override fun miteCheckTitle(hiveName: String) = "Mite check $hiveName"
    override fun miteOverdueShort(never: Boolean, overdueDays: Long) =
        if (never) "Never checked" else "Overdue by ${dayCount(overdueDays)}"
    override fun miteOverdueExplanation(never: Boolean, daysSince: Long, intervalDays: Int, priorHighResult: MiteResult?): String {
        val history = if (never) "No varroa check has been recorded for this colony"
        else "The last varroa check was ${daysAgo(daysSince)}"
        val prior = priorHighResult?.let { " The previous reading was ${miteResultLower(it)}, so don't let it drift." } ?: ""
        return "$history; the monitoring interval is ${dayCount(intervalDays.toLong())}.$prior Run an alcohol wash or sugar roll."
    }

    override fun postTreatmentTitle(hiveName: String) = "Post-treatment mite check · $hiveName"
    override fun postTreatmentShort(daysUntil: Long) = if (daysUntil <= 0) "Check due" else "Check ${inDays(daysUntil)}"
    override fun postTreatmentExplanation(daysUntil: Long, endedDays: Long?): String {
        val ended = endedDays?.let { " Treatment ended ${daysAgo(it)}." } ?: ""
        return "A follow-up varroa check is ${inDays(daysUntil)} to confirm the last treatment worked.$ended " +
            "Re-treat if mite levels are still high."
    }

    override fun weakTitle(hiveName: String) = "Watch $hiveName"
    override fun weakShort() = "Weak colony"
    override fun weakExplanation(daysSince: Long?): String {
        val seen = daysSince?.let { " (last seen ${daysAgo(it)})" } ?: ""
        return "This colony was assessed as weak$seen. Keep an eye on build-up; " +
            "consider reducing the entrance, tightening the brood nest, or uniting if it doesn't recover."
    }

    override fun harvestTitle(hiveName: String) = "Harvest prep · $hiveName"
    override fun harvestShort(inWindow: Boolean) = if (inWindow) "Ready to harvest" else "Prep for harvest"
    override fun harvestExplanation(inWindow: Boolean): String {
        val phase = if (inWindow) "The harvest window is open" else "Harvest season is approaching"
        return "$phase, and this colony is strong with full stores. Check capped honey and plan supering or extraction."
    }

    override fun manualShort(overdue: Boolean, daysUntil: Long) =
        if (overdue) "Overdue task" else "Task due ${inDays(daysUntil)}"
    override fun manualExplanation(overdue: Boolean, daysUntil: Long, details: String?) = buildString {
        append("You set this task")
        append(if (overdue) ", and it became due ${inDays(daysUntil)}. " else ", due ${inDays(daysUntil)}. ")
        details?.takeIf { it.isNotBlank() }?.let { append(it) }
    }.trim()

    override fun miteResultLower(result: MiteResult) = result.label.lowercase()

    override fun seasonalTitle(phase: SeasonPhase) = when (phase) {
        SeasonPhase.WINTER -> "Winter care"
        SeasonPhase.SPRING_BUILDUP -> "Spring build-up"
        SeasonPhase.SWARM_AND_FLOW -> "Swarm season & main flow"
        SeasonPhase.SUMMER_HARVEST -> "Harvest & first varroa treatment"
        SeasonPhase.AUTUMN_PREP -> "Winterize the colony"
    }

    override fun seasonalShort(phase: SeasonPhase) = when (phase) {
        SeasonPhase.WINTER -> "Leave them in peace"
        SeasonPhase.SPRING_BUILDUP -> "First inspections"
        SeasonPhase.SWARM_AND_FLOW -> "Check weekly, give room"
        SeasonPhase.SUMMER_HARVEST -> "Harvest, then treat"
        SeasonPhase.AUTUMN_PREP -> "Feed, treat, make winter-ready"
    }

    override fun seasonalExplanation(phase: SeasonPhase) = when (phase) {
        SeasonPhase.WINTER ->
            "Keep the hive undisturbed and check stores by hefting; feed in an emergency. When the " +
                "colony is broodless (often Dec–Jan, ideally near freezing), do the oxalic-acid winter treatment."
        SeasonPhase.SPRING_BUILDUP ->
            "On the first warm day, do the first full inspection: confirm the queen and stores, clean the " +
                "floor, tighten the brood nest, and add a drone comb. Start watching for swarm signs."
        SeasonPhase.SWARM_AND_FLOW ->
            "During the main flow, inspect every 7 days for swarm cells, add supers in time, and consider " +
                "splits or queen rearing. Cutting drone brood knocks back varroa."
        SeasonPhase.SUMMER_HARVEST ->
            "Take the summer harvest and remove the supers, then treat varroa right after the last harvest. " +
                "Begin feeding toward ~15–18 kg of winter stores; give water in heat and reduce entrances against robbing."
        SeasonPhase.AUTUMN_PREP ->
            "Finish feeding by late September and confirm the varroa treatment worked. Unite or remove weak " +
                "colonies, requeen if needed, fit mouse guards, and stop disturbing the bees."
    }

    override fun flowImminentTitle() = "Flow approaching"
    override fun flowImminentShort() = "Get ready to super"
    override fun flowImminentExplanation(monthsToNext: Int?): String {
        val whenText = when (monthsToNext) {
            null -> ""
            1 -> " (about a month out)"
            else -> " (about $monthsToNext months out)"
        }
        return "A main nectar flow is approaching$whenText. Make sure colonies are strong and supered, " +
            "and step up swarm checks so they don't swarm during the flow."
    }
    override fun dearthTitle() = "Forage dearth"
    override fun dearthShort() = "Gap between flows"
    override fun dearthExplanation() =
        "Little is blooming now. Reduce entrances against robbing and check stores, feeding if they're light. " +
            "With supers off, it's also a good window to monitor and treat varroa."

    override fun inspectionWeatherTitle() = "Good inspecting weather"
    override fun inspectionWeatherShort() = "Warm, calm, dry"
    override fun inspectionWeatherExplanation(dayOffset: Int) =
        "Conditions look good for an inspection ${inDays(dayOffset.toLong())} — warm, calm and dry. " +
            "A good window to work the colonies with minimal disruption."
    override fun treatmentWeatherTitle() = "Heat spell ahead"
    override fun treatmentWeatherShort() = "Mind varroa treatment"
    override fun treatmentWeatherExplanation(maxTempC: Double) =
        "Highs near ${"%.0f".format(maxTempC)} °C are forecast. Formic acid can harm the colony in high heat — " +
            "wait for cooler days or choose a heat-tolerant method (e.g. oxalic acid when broodless)."
    override fun coldSnapTitle() = "Cold snap coming"
    override fun coldSnapShort() = "Check stores & shelter"
    override fun coldSnapExplanation(minTempC: Double) =
        "Lows around ${"%.0f".format(minTempC)} °C are forecast for several days. Make sure colonies have ample " +
            "stores and are sheltered from wind, and hold off on opening them."

    override fun bestandsbuchTitle() = "Record book: add receipts"
    override fun bestandsbuchShort(missingCount: Int) =
        if (missingCount == 1) "1 treatment needs a receipt" else "$missingCount treatments need a receipt"
    override fun bestandsbuchExplanation(missingCount: Int) =
        "German law (Bestandsbuch, EU 2019/6 Art. 108) requires a proof-of-purchase for every medicine used — " +
            "including formic and oxalic acid. $missingCount treatment(s) here have no receipt linked yet. " +
            "Add the supplier and a photo of the receipt, then export the record book as a PDF when needed."
}
