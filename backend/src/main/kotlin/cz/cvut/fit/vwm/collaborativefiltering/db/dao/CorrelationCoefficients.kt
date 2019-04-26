package cz.cvut.fit.vwm.collaborativefiltering.db.dao

import org.jetbrains.squash.definition.*

object CorrelationCoefficients : TableDefinition() {
    val id = integer("id").autoIncrement().primaryKey()
    val userId1 = integer("user_id_1").index()
    val userId2 = integer("user_id_2").index()
    val distance = decimal("distance", 38, 20)
    val spearmanCoeficient = decimal("spearman_coef", 38, 20)
}
