package io.github.max_schall.appiary.util

import java.util.UUID

/** Stable, app-generated primary keys. String UUIDs keep export/import and a
 *  future sync layer straightforward (no autoincrement collisions across devices). */
fun newId(): String = UUID.randomUUID().toString()
