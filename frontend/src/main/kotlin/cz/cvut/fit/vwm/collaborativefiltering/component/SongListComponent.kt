package cz.cvut.fit.vwm.collaborativefiltering.component

import cz.cvut.fit.vwm.collaborativefiltering.launch
import cz.cvut.fit.vwm.collaborativefiltering.model.Song
import cz.cvut.fit.vwm.collaborativefiltering.model.ReviewedSong
import cz.cvut.fit.vwm.collaborativefiltering.request.SongRpc
import cz.cvut.fit.vwm.collaborativefiltering.request.ReviewRpc
import cz.cvut.fit.vwm.collaborativefiltering.request.UserRpc
import cz.cvut.fit.vwm.collaborativefiltering.request.ReviewedSongRpc
import cz.cvut.fit.vwm.collaborativefiltering.model.Review
import react.*
import react.dom.*
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.events.Event
import cz.cvut.fit.vwm.collaborativefiltering.async


fun RBuilder.songListComponent(handler: RHandler<SongListComponent.Props> = {}) = child(SongListComponent::class, handler)

class SongListComponent : RComponent<SongListComponent.Props, SongListComponent.State>() {

    init {
        state = State()
    }

    override fun componentDidMount() {
        getSongs()
    }

    private fun getSongs() {
        launch {
            val reviewedSongsList = ReviewedSongRpc.getList()
            setState {
                songs = reviewedSongsList
            }
        }
    }

    private fun getNameAttribute(reviewedSong: ReviewedSong): String {
        val str = reviewedSong.song.id.toString()
        if (reviewedSong.review == null)
            return str
        else
            return str + "|" + reviewedSong.review.id.toString()
    }

    private fun RBuilder.song(reviewedSong: ReviewedSong) {
        h2 {
            +"#${reviewedSong.song.lastFmRank} "
            a {
                +reviewedSong.song.title
                +" by "
                +reviewedSong.song.artist
                attrs.href = reviewedSong.song.url ?: ""
                attrs.target = "_blank"
            }
            select {
                option {
                    attrs.selected = true
                    attrs.value = "0"
                    +"Review!"
                }
                option {
                    attrs.selected = (reviewedSong.review != null && reviewedSong.review.value == 1)
                    attrs.value = "1"
                    +"1"
                }
                option {
                    attrs.selected = (reviewedSong.review != null && reviewedSong.review.value == 2)
                    attrs.value = "2"
                    +"2"
                }
                option {
                    attrs.selected = (reviewedSong.review != null && reviewedSong.review.value == 3)
                    attrs.value = "3"
                    +"3"
                }
                option {
                    attrs.selected = (reviewedSong.review != null && reviewedSong.review.value == 4)
                    attrs.value = "4"
                    +"4"
                }
                option {
                    attrs.selected = (reviewedSong.review != null && reviewedSong.review.value == 5)
                    attrs.value = "5"
                    +"5"
                }
                attrs.name = getNameAttribute(reviewedSong)
                attrs.onChangeFunction = {
                    getNameAttribute(reviewedSong)
                    val target = it.target as HTMLSelectElement
                    val songReviewId = target.getAttribute("name")
                    val rating = target.value
                    doReview(songReviewId, rating)
                }
            }
        }
    }

    private fun doReview(songReviewId: String?, rating: String){
        val output = songReviewId?.split("|");
        if (output == null)
            return
        var songId = 0;
        var ratingValue = 0;
        try {
            songId = output.get(0).toInt()
            ratingValue = rating.toInt()

        } catch (e: NumberFormatException) {
            console.log(e)
        }
        async {
            if (output.size == 1) {
                val rev = ReviewRpc.create(songId, ratingValue)
                var list = state.songs
                if ( list != null ){
                    val newList = list.map{
                        if ( it.song.id == rev.songId )
                            ReviewedSong(it.song, rev)
                        else
                            it
                    }
                    setState {
                        songs = newList
                    }
                }
            }
            if ( output.size == 2 && ratingValue == 0) {
                var list = state.songs
                if ( list != null ){
                    val newList = list.map{
                        if ( it.review != null && it.review.id == output.get(1).toInt() )
                            ReviewedSong(it.song, null)
                        else
                            it
                    }
                    setState {
                        songs = newList
                    }
                }
                ReviewRpc.delete(output.get(1).toInt())
            }
            if ( output.size == 2 && ratingValue != 0)
                ReviewRpc.update(output.get(1).toInt(), ratingValue)
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
                            p(classes = "post-meta") {
                                song(t)
                            }
                        }
                    }
                }
            } else {
                p { +"Loading..." }
            }
        }
    }

    class Props(var title: String = "List of songs") : RProps
    class State(var songs: List<ReviewedSong>? = null) : RState
}