package com.permission.localmusic


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView



class LocalMusicAdapter ( val mDatas:ArrayList<LocalMusicBean>):
            RecyclerView.Adapter<LocalMusicAdapter.LocalMusicViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener=onItemClickListener
        Log.d("适配器的点击", "完成")

    }

    interface OnItemClickListener {

        fun onItemClick(view: View?, position: Int)
    }
    inner class LocalMusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val  idTv: TextView =itemView.findViewById(R.id.item_local_music_num)
        val songTv: TextView?=itemView.findViewById(R.id.item_local_music_song)
        val singerTv: TextView?=itemView.findViewById(R.id.item_local_music_singer )
        val albumTv: TextView=itemView.findViewById(R.id.item_local_music_album)
        val timeTv: TextView=itemView.findViewById(R.id.item_local_music_duration)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalMusicViewHolder {
        Log.d("适配器", "完成")
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_local_music, parent, false)
        return LocalMusicViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocalMusicViewHolder, position: Int) {
        val musicBean= mDatas[position]
        holder.idTv.text=musicBean.id
        holder.songTv?.text=musicBean.song
        holder.singerTv?.text=musicBean.singer
        holder.albumTv.text=musicBean.album
        holder.timeTv.text=musicBean.duration
        /**
         *        holder.itemView.setOnClickListener(View.OnClickListener{
        fun onClick(v:View){
        setOnItemClickListener(v,position)
        }
        })
         */
        holder.itemView.setOnClickListener {
                v -> onItemClickListener!!.onItemClick(v, position)
        }
    }

    override fun getItemCount(): Int {
        return mDatas.size
    }


}



