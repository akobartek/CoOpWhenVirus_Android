package pl.marta.view.ui

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.google.android.material.textfield.TextInputEditText

class MyTextInputEditText : TextInputEditText {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    override fun getAutofillType(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) View.AUTOFILL_TYPE_NONE
        else super.getAutofillType()
}