package com.permission.localmusic

import android.util.Log


class LocalMusicBean(//歌曲id
    var id: String?,//歌曲名称
    var song: String?,//歌手名称
    var singer: String?,//专辑名称
    var album: String?,//歌曲时长
    var duration: String?,//歌曲路径
    var path: String?
) {


    init {
        Log.d("数据源","完成")
    }
}