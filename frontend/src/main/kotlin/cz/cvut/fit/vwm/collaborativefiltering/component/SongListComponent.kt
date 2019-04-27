package cz.cvut.fit.vwm.collaborativefiltering.component

import cz.cvut.fit.vwm.collaborativefiltering.launch
import cz.cvut.fit.vwm.collaborativefiltering.model.Song
import cz.cvut.fit.vwm.collaborativefiltering.model.ReviewedSong
import cz.cvut.fit.vwm.collaborativefiltering.request.SongRpc
import cz.cvut.fit.vwm.collaborativefiltering.request.ReviewedSongRpc
import react.*
import react.dom.*
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.events.Event
//import khttp.post



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
            val list = SongRpc.getList()
            val reviewedSongsList = ReviewedSongRpc.getList()


            console.log("songs", list)
            console.log("reviewed songs", reviewedSongsList )
            setState {
                songs = reviewedSongsList
            }
        }
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
            select{
                option{
                    attrs.selected = true
                    attrs.value = "0"
                    + "Review!"
                }
                option{
                    attrs.selected = ( reviewedSong.rating == 1 )
                    attrs.value = "1"
                    + "1"
                }
                option{
                    attrs.selected = ( reviewedSong.rating == 2 )
                    attrs.value = "2"
                    + "2"
                }
                option{
                    attrs.selected = ( reviewedSong.rating == 3 )
                    attrs.value = "3"
                    + "3"
                }
                option{
                    attrs.selected = ( reviewedSong.rating == 4 )
                    attrs.value = "4"
                    + "4"
                }
                option{
                    attrs.selected = ( reviewedSong.rating == 5 )
                    attrs.value = "5"
                    + "5"
                }
                attrs.name = reviewedSong.song.id.toString()
                attrs.onChangeFunction = {
                    console.log("changing")
                    val target = it.target as HTMLSelectElement
                    val songId = target.getAttribute("name")
                    val rating = target.value
                    val userId = 1
                    val reviewId = 1
                    console.log(songId)
                    /*if ( rating == 0.toString() )
                        khttp.put(
                                url = "http://localhost:8080/reviews/" + reviewId
                        )

                    khttp.post(
                            url = "http://localhost:8080/reviews",
                            json = mapOf("userId" to "1", "songId" to songId, "rating" to rating)
                    )*/
                    console.log(rating)
                }
            }
        }
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