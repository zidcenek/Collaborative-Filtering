package cz.cvut.fit.vwm.collaborativefiltering.component

import cz.cvut.fit.vwm.collaborativefiltering.async
import cz.cvut.fit.vwm.collaborativefiltering.launch
import cz.cvut.fit.vwm.collaborativefiltering.model.ReviewedSong
import cz.cvut.fit.vwm.collaborativefiltering.request.ReviewRpc
import cz.cvut.fit.vwm.collaborativefiltering.request.ReviewedSongRpc
import cz.cvut.fit.vwm.collaborativefiltering.request.SongRecommendationRpc
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLSelectElement
import react.*
import react.dom.*


fun RBuilder.songListComponent(handler: RHandler<SongListComponent.Props>) = child(SongListComponent::class, handler)

class SongListComponent : RComponent<SongListComponent.Props, SongListComponent.State>() {

    init {
        state = State()
    }

    override fun componentDidMount() {
        getSongs()
    }

    private fun getSongs() {
        launch {
            val list = if (props.recommended) {
                console.log("Showing SongRecommendations")
                val recommendedSongsList = SongRecommendationRpc.getList()
                recommendedSongsList.map {
                    ReviewedSong(it.song, null)
                }
            } else {
                console.log("Showing Songs")
                ReviewedSongRpc.getList()
            }
            setState {
                songs = list
            }
            console.log(list)
        }
    }

    private fun RBuilder.song(reviewedSong: ReviewedSong) {
        p(classes = "post-subtitle") {
            +"#${reviewedSong.song.lastFmRank} "
            a {
                +reviewedSong.song.title
                +" by "
                +reviewedSong.song.artist
                attrs.href = reviewedSong.song.url ?: ""
                attrs.target = "_blank"
            }
            select {
                // produces warning (bug) - https://github.com/JetBrains/kotlin-wrappers/issues/92
                attrs.value = reviewedSong.review?.value?.toString() ?: "0"
                (0..5).forEach {
                    option {
                        +(if (it == 0) "Review!" else it).toString()
                    }
                }
                attrs.name = reviewedSong.song.id.toString()
                attrs.onChangeFunction = {
                    val target = it.target as HTMLSelectElement
                    val songId = target.name.toIntOrNull()
                    val rating = target.selectedIndex
                    if (songId != null) {
                        doReview(songId, rating)
                    }
                }
            }
        }
    }

    private fun doReview(songId: Int, rating: Int) {
        val reviewedSong = state.songs?.find { it.song.id == songId } ?: return
        async {
            val newList: List<ReviewedSong>? = when {
                reviewedSong.review == null -> {    // not rated yet
                    val rev = ReviewRpc.create(songId, rating)
                    state.songs?.map {
                        if (it.song.id == rev.songId)
                            ReviewedSong(it.song, rev)
                        else
                            it
                    }
                }
                rating == 0 -> {    // delete review
                    ReviewRpc.delete(reviewedSong.review.id)
                    state.songs?.map {
                        if (it.song.id == songId)
                            ReviewedSong(it.song, null)
                        else
                            it
                    }
                }
                rating != 0 -> {    // update rating
                    ReviewRpc.update(reviewedSong.review.id, rating)
                    reviewedSong.review.value = rating // keep state consistent
                    state.songs
                }
                else -> {
                    state.songs
                }
            }

            setState {
                songs = newList
            }

        }.catch { err ->
            updateFailed(err)
        }
        return
    }

    private fun updateFailed(err: Throwable) {
        console.log(err)
    }

    override fun RBuilder.render() {
        div {
            h1 { +props.title }
            if (state.songs != null) {
                ul {
                    state.songs?.map { t ->
                        li {
                            key = t.song.id.toString()
                            song(t)
                        }
                    }
                }
            } else {
                p { +"Loading..." }
            }
        }
    }

    class Props(var title: String = "List of songs", var recommended: Boolean) : RProps
    class State(var songs: List<ReviewedSong>? = null) : RState
}