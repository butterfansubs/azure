import myaa.subkt.ass.*
import myaa.subkt.tasks.*
import myaa.subkt.tasks.Mux.*
import myaa.subkt.tasks.Nyaa.*
import java.awt.Color
import java.time.*

plugins {
    id("myaa.subkt")
}

fun getGitHash(): String {
    val proc = ProcessBuilder("git", "rev-parse", "HEAD")
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .start()

    return proc.inputStream.reader().readText().trim() ?: ""
}

subs {
    readProperties("sub.properties", "private.properties")
    episodes(getList("episodes"))

    val script by task<ASS> {
        from(get("script"))

        val git_hash = getGitHash()

        ass {
            for (line in events.lines) {
                if (line.comment) {
                    line.text = line.text.replace("%GIT_HASH%", git_hash)
                }
            }
        }
    }

    merge {
        from(script.item())

        includeProjectGarbage(false)
        includeExtraData(false)

        out(get("mergefile"))
    }

    mux {
        // uncomment this line to disable font validation if necessary
        // verifyFonts(false)
        skipUnusedFonts(true)

        title(get("title"))

        from(get("premux")) {
            video {
                lang("jpn")
                default(true)
            }
            audio {
                lang("jpn")
                default(true)
            }
            includeChapters(false)
            attachments { include(false) }
        }

        from(merge.item()) {
            tracks {
                name(get("group"))
                lang("eng")
                default(true)
            }
        }

        attach(get("fonts")) {
            includeExtensions("ttf", "otf")
        }

        out(get("muxfile"))
    }
}
