package cen.xiaoyuan.epub.ui.preference

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import cen.xiaoyuan.epub.R
import com.google.android.material.card.MaterialCardView

class PreviewPreference@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : Preference(context, attrs, defStyleAttr, defStyleRes) {

    private val observer = Observer<Any> { updateSummary() }

    private lateinit var previewText:TextView
    private lateinit var previewBackgroundColor:MaterialCardView

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        (holder.findViewById(R.id.preview_title) as TextView).text = title.toString()
        previewText = holder.findViewById(R.id.preview_text) as TextView
        previewBackgroundColor = holder.findViewById(R.id.preview_bg) as MaterialCardView
        updateSummary()

    }

    private fun updateSummary() {
        if(!this::previewText.isInitialized) return
        previewBackgroundColor.setCardBackgroundColor(Settings.READER_BACKGROUND_COLOR.valueCompat)
        previewText.setTextColor(Settings.READER_TEXT_COLOR.valueCompat)
        previewText.textSize = Settings.READER_TEXT_SIZE.valueCompat*0.26f
    }

    override fun onAttached() {
        super.onAttached()
        Settings.READER_TEXT_COLOR.observeForever(observer)
        Settings.READER_BACKGROUND_COLOR.observeForever(observer)
        Settings.READER_TEXT_SIZE.observeForever(observer)
    }

    override fun onDetached() {
        super.onDetached()
        Settings.READER_TEXT_COLOR.removeObserver(observer)
        Settings.READER_BACKGROUND_COLOR.removeObserver(observer)
        Settings.READER_TEXT_SIZE.removeObserver(observer)
    }
}