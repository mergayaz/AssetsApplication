package kz.kuz.assets

import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.io.IOException

class MainFragment : Fragment(), OnSeekBarChangeListener {
    private lateinit var mSoundPool: SoundPool // SoundPool - класс для хранения звуков
    private lateinit var seekBar : SeekBar // ползунок
    var soundRate = 0f
    var soundRate2 = 0f
    private lateinit var textView: TextView
    var currentStream = 0

    // методы фрагмента должны быть открытыми
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        // по умолчанию setRetainInstance стоит false
        // это означает, что фрагмент уничтожается вместе с активностью (например при поворотах)
        // когда мы ставим true, то фрагмент не уничтожается, а передаётся новой активности
        // это необходимо, чтобы звук не прерывался при повороте экрана
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.setTitle(R.string.toolbar_title)
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        val mAssets = context?.assets
        // класс AssetManager используется для обращения к активам
        // экземпляр этого класса можно получить для любой разновидности Context
        // при этом все разновидности Context работают с одним набором активов
        seekBar = view.findViewById(R.id.seekBar)
        seekBar.setOnSeekBarChangeListener(this)
        soundRate = seekBar.progress.toFloat() / 10
        soundRate2 = soundRate * 100
        textView = view.findViewById(R.id.textView)
        textView.text = "Playback Speed: ${soundRate2.toInt()}%"
        try {
            val fileNames = mAssets?.list("sounds")
            // mAssets.list возвращает список имён файлов в папке "sounds"
            val attributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            mSoundPool = SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .setMaxStreams(5)
                    .build()
            // SoundPool - класс для хранения звуков, AudioAttributes - его атрибуты
            var id: Int
            var afd: AssetFileDescriptor // дескриптор файлов для AssetManager
            for (i in 0..5) {
                // получаю идентификатор для кнопок
                id = resources.getIdentifier("btn" + (i + 1), "id",
                        context!!.packageName)
                val button = view.findViewById<Button>(id)
                button.text = fileNames!![i].replace(".wav", "")
                afd = mAssets.openFd("sounds/" + fileNames[i])
                val mSound = mSoundPool.load(afd, 1)
                // mSound должен инициализироваться внутри цикла, чтобы быть финальным
                button.setOnClickListener { // проигрыш звука, одновременно сохраняем стрим, чтобы управлять им
                    // ползунком
                    currentStream = mSoundPool.play(mSound, 1.0f, 1.0f,
                            1, 0, soundRate)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        mSoundPool.release()
        // необходимо освободить ресурсы SoundPool
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        soundRate = progress.toFloat() / 10
        soundRate2 = soundRate * 100
        // вместе с изменением значения ползунка одновременно изменяем текст и скорость звука
        textView.text = "Playback Speed: ${soundRate2.toInt()}%"
        mSoundPool.setRate(currentStream, soundRate)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}
    override fun onStopTrackingTouch(seekBar: SeekBar) {}
}