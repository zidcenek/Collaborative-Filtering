package cz.cvut.fit.vwm.collaborativefiltering.component

import cz.cvut.fit.vwm.collaborativefiltering.getSongList
import cz.cvut.fit.vwm.collaborativefiltering.launch
import cz.cvut.fit.vwm.collaborativefiltering.model.Song
import react.*
import react.dom.*

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
            val list = getSongList()

            console.log("songs", list)
            setState {
                songs = list
            }
        }
    }

    private fun RBuilder.song(song: Song) {
        h2 {
            +"#${song.lastFmRank} "
            a {
                +song.title
                +" by "
                +song.artist
                attrs.href = song.url ?: ""
                attrs.target = "_blank"
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
                            key = t.id.toString()
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
    class State(var songs: List<Song>? = null) : RState
}