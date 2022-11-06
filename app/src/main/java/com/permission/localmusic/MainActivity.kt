package com.permission.localmusic


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.permission.localmusic.LocalMusicAdapter.OnItemClickListener
import com.permission.localmusic.databinding.ActivityMainBinding
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity()  {

    private var nextIv: ImageView? =null
    private var playIv: ImageView? =null
    private var lastIv: ImageView? =null
    private var singerTv:TextView?=null
    private var songTv:TextView?=null
    private var mDatas=ArrayList<LocalMusicBean>()
    private var musicRv:RecyclerView?=null
    //适配器
    private var adapter:LocalMusicAdapter?=null
    //记录当前正在播放音乐的位置
    var currentPlayPosition=-1
    //记录暂停音乐时候进度条的位置
    private var currentPausePositionInSong=0
    //记录当前正在播放音乐的位置
    //停止音乐
    private val mediaPlayer=MediaPlayer()
    //需要获取的权限,Manifest.permission.READ_EXTERNAL_STORAGE
    private val permissions= arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var isAllGrant:Boolean=false

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //需要的权限

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initView()
        Log.d("初始化","完成")

        //创建适配器对象
        adapter=LocalMusicAdapter(mDatas)
        Log.d("获得适配器","成功")
        musicRv?.adapter=adapter
        //设置布局管理器
        val layoutManager=LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        musicRv?.layoutManager=layoutManager
        //申请动态权限
        // handlePermission()
        //加载本地数据源
        loadLocalMusicData()
        Log.d("加载数据","完成")
        //设置点击事件
        setEventListener()
        Log.d("点击事件","完成")
    }



    private fun setEventListener() {
        //设置每一项点击事件
        Log.d("点击","完成")
        adapter!!.setOnItemClickListener( object : OnItemClickListener {
            override fun onItemClick(view: View?, position: Int) {

                    currentPlayPosition = position
                    val musicBean = mDatas[position]
                    playMusicInMusicBean(musicBean)


                }
            })

        }

    /**
     * 根据传入对象播放音乐
     */
    fun playMusicInMusicBean(musicBean: LocalMusicBean){
        //设置底部显示歌手和歌曲名
        Log.d("设置歌手歌曲","完成")

        singerTv?.text = musicBean.singer
        songTv?.text = musicBean.song
        //停止音乐
        stopMusic()
        Log.d("停止音乐", "完成")
        //重置多媒体播放器
        mediaPlayer.reset()
        //重置新的路径
        try {
            mediaPlayer.setDataSource(musicBean.path)
            playMusic()
            Log.d("重置新的路径", "完成")

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 点击播放按钮播放音乐，或者暂停再播放
     * 两种情况
     * 1、从暂停到停止
     * 2、从停止到播放
     */
    private fun playMusic() {
        //播放音乐的函数
        if (!mediaPlayer.isPlaying){
            if (currentPausePositionInSong==0){
                try {
                    with(mediaPlayer) {
                        prepare()
                        start()
                    }
                }catch (e:IOException){
                    e.printStackTrace()
                }
            }else{
                //从暂停到播放
                mediaPlayer.seekTo(currentPausePositionInSong)
                mediaPlayer.start()
            }

            playIv?.setImageResource(R.mipmap.icon_pause)//一点击就变成这个图标
        }
    }
    private fun pauseMusic() {
        //暂停音乐
        if (mediaPlayer.isPlaying){
            currentPausePositionInSong=mediaPlayer.currentPosition
            mediaPlayer.pause()
            playIv?.setImageResource(R.mipmap.icon_play)
        }
    }


    private fun stopMusic() {
        //停止音乐的函数
        currentPausePositionInSong=0
        mediaPlayer.pause()
        mediaPlayer.seekTo(0)
        mediaPlayer.stop()
        playIv?.setImageResource(R.mipmap.icon_play)//使播放按钮回到初始位置
    }
    override fun onDestroy() {
        super.onDestroy()
        //销毁
        stopMusic()
    }


    @SuppressLint("Range", "Recycle", "NotifyDataSetChanged", "SimpleDateFormat")
    private fun loadLocalMusicData() {
        /**
         * 加载本地存储当中的音乐MP3文件到集合当中
         * 1、获取ContentResolver
         * 2、获取本地音乐存储uri地址
         * 3、开始查找地址
         * 4、遍历Cursor
         */

        val resolver=contentResolver
        val uri=MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor=resolver.query(uri,null,null,null,null)
        var id=0
        while (cursor?.moveToNext() == true){
            val song=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
            val singer=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
            val album=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
            id++
            val sid= id.toString()
            val path=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
            val duration=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
            val sdf= SimpleDateFormat("mm:ss")
            val time=sdf.format(Date(duration))
            if (time.equals("00:00")){
                id--
            }else{
                //将一行当中的数据封装到对象当中
                val bean=LocalMusicBean(sid,song,singer,album,time,path)
                Log.d("获取音乐","成功")

                mDatas.add(bean)
            }

        }
        //数据源变化，提示适配器更新
        adapter?.notifyDataSetChanged()
        //


    }
    private fun normal(){
        val isAllGrant=checkAllPermissionGranted(permissions,this)
        if (isAllGrant){
            //已有权限->做事情
            Toast.makeText(this,"检测到已有权限！！",Toast.LENGTH_SHORT).show()
            loadLocalMusicData()
        }else{
            //没权限->申请权限
            ActivityCompat.requestPermissions(this, permissions,1)
        }
    }


    private fun checkAllPermissionGranted(permissions: Array<String>, context: Context): Boolean {
        //遍历检测权限
        for (p in permissions){
            if (ContextCompat.checkSelfPermission(context, permissions.toString())!=PackageManager.PERMISSION_GRANTED){
                return false
            }
        }
                return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==1){
            isAllGrant=true
            for (g in grantResults){
                if (grantResults[0]==PackageManager.PERMISSION_DENIED){
                    isAllGrant=false
                    break
                }
            }
        }
        if (isAllGrant){//同意授权->做事
            Toast.makeText(this,"授权成功",Toast.LENGTH_SHORT).show()
            loadLocalMusicData()
        }else{
            //拒绝授权->开弹窗跳询问是否跳设置-权限管理界面
            AlertDialog.Builder(this).apply {
                setMessage("应用需要您的通讯录和存储权限，请到设置-权限管理中授权。")
                setPositiveButton("确定"){ _, _ ->
                    intent= Intent()
                    intent.apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        addCategory(Intent.CATEGORY_DEFAULT)
                        data = Uri.parse("package:$packageName")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    }
                    startActivity(intent)
                }
                setCancelable(false)
                setNegativeButton("取消"){_,_->
                    Toast.makeText(context,"您没有获得权限，此功能不能正常使用",Toast.LENGTH_SHORT).show()
                }
             show()
            }
        }
    }

    /**
    //申请动态权限
    handlePermission()
    private fun handlePermission() {
    //动态申请权限
    Log.d("动态申请权限","完成")
    val permission=Manifest.permission.READ_EXTERNAL_STORAGE //这个是需要申请的权限信息
    val checkPerm=this.let { ActivityCompat.checkSelfPermission(this,permission) }
    if (checkPerm==PackageManager.PERMISSION_GRANTED){
    // 执行到这里说明用户已经申请了权限直接加载数据就可以
    loadLocalMusicData()
    }else{
    //执行到这里说明没有权限了
    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
    }

    }

    override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
    ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
    loadLocalMusicData()

    }
    }


     */






    private fun initView(){
        //申请权限函数放这
        normal()
        /**
         * 初始化控件函数
         */
        nextIv=findViewById(R.id.local_music_bottom_iv_next)
        playIv=findViewById(R.id.local_music_bottom_iv_play)
        lastIv=findViewById(R.id.local_music_bottom_iv_last)
        singerTv=findViewById(R.id.local_music_bottom_tv_singer)
        songTv=findViewById(R.id.local_music_bottom_tv_song)
        musicRv=findViewById(R.id.local_music_rv)
        Log.d("初始化进行","完成")
        /**
         * 上去，下去，播放
         */
        binding.localMusicBottomIvNext.setOnClickListener{
            Log.d("下一首歌","完成")

            if (currentPlayPosition==mDatas.size-1){
                Toast.makeText(this,"已经是最后一首了，没有下一首了，嘤嘤嘤！",Toast.LENGTH_SHORT).show()
            }
            currentPlayPosition += 1
            val nextBean=mDatas[currentPlayPosition]
            playMusicInMusicBean(nextBean)    //？？？有问？？？
            Log.d("下一首歌资源已加载","完成")
        }
        /*******************************/
        binding.localMusicBottomIvPlay.setOnClickListener{
            Log.d("播放","完成")

            if (currentPlayPosition==-1){
                //没有选中音乐
                Toast.makeText(this,"请选择需要播放的音乐",Toast.LENGTH_SHORT).show()
            }
            if (mediaPlayer.isPlaying){
                //此时处于播放状态，需要暂停音乐
                pauseMusic()
            }else{
                //此时处于暂停音乐，需要播放音乐
                playMusic()
            }
        }
        /*****************************/
        binding.localMusicBottomIvLast.setOnClickListener {
            Log.d("上一首歌","完成")

            if (currentPlayPosition==0){
                Toast.makeText(this,"已经是第一首了，没有上一首了，嘤嘤嘤！",Toast.LENGTH_SHORT).show()

            }
            currentPlayPosition -= 1
            val lastBean=mDatas[currentPlayPosition]
            playMusicInMusicBean(lastBean)
        }

    }



}

