package cz.cvut.fit.vwm.collaborativefiltering

import org.jetbrains.squash.definition.ColumnDefinition
import org.jetbrains.squash.definition.TableDefinition

/**
 * Column name exactly as stored in database.
 */
val ColumnDefinition<Any?>.colName
    get() = name.identifier.id

/**
 * Escaped column name colName
 */
val ColumnDefinition<Any?>.colNameEsc
    get() = "`$colName`"

val TableDefinition.tableName
    get() = compoundName.id

val TableDefinition.dropStatement
    get() = "DROP TABLE IF EXISTS $tableName;"