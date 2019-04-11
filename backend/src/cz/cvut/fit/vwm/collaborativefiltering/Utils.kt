package cz.cvut.fit.vwm.collaborativefiltering

import cz.cvut.fit.vwm.collaborativefiltering.data.json.MockDataJsonParser
import cz.cvut.fit.vwm.collaborativefiltering.db.DatabaseInteractor
import io.ktor.util.hex
import java.net.URL
import java.net.UnknownHostException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

val hashKey = hex("c11cae575f8e4058662a5ef3c0c2bc")
val hmacKey = SecretKeySpec(hashKey, "HmacSHA1")
fun hash(password: String): String {
    val hmac = Mac.getInstance("HmacSHA1")
    hmac.init(hmacKey)
    return hex(hmac.doFinal(password.toByteArray(Charsets.UTF_8)))
}


fun fillDbWithMockData(storage: DatabaseInteractor) {
    if (storage.getSongsCount() == 0) {
        try {
            val url = "https://ws.audioscrobbler.com/2.0/?method=user.gettoptracks&user=rj&api_key=$LAST_FM_API_KEY&format=json&limit=100"
            MockDataJsonParser.parseSongs(URL(url).readText()).forEach { storage.createSong(it) }
        } catch (e: UnknownHostException) {
            MockDataJsonParser.parseSongs(MockDataJsonParser.getFileContent("mock/songs.json")).forEach { storage.createSong(it) }
        }
    }
    if (storage.getUsersCount() == 0) {
        MockDataJsonParser.parseUsers("mock/users.json").forEach { storage.createUser(it) }
    }
    if (storage.getReviewsCount() == 0) {
        MockDataJsonParser.praseReview("mock/reviewUser1.json").take(100).forEach { storage.createReview(it) }
        MockDataJsonParser.praseReview("mock/reviewUser2.json").take(100).forEach { storage.createReview(it) }
        MockDataJsonParser.praseReview("mock/reviewUser3.json").take(100).forEach { storage.createReview(it) }
        MockDataJsonParser.praseReview("mock/reviewUser4.json").take(100).forEach { storage.createReview(it) }
        MockDataJsonParser.praseReview("mock/reviewUser5.json").take(100).forEach { storage.createReview(it) }
        MockDataJsonParser.praseReview("mock/reviewUser6.json").take(100).forEach { storage.createReview(it) }
        MockDataJsonParser.praseReview("mock/reviewUser7.json").take(100).forEach { storage.createReview(it) }
    }
}