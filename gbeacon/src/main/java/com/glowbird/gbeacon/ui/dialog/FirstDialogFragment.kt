package com.glowbird.gbeacon.ui.dialog

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.PrivacyDialogBinding // 自动生成的绑定类
import com.glowbird.gbeacon.ui.about.AgreementActivity

class FirstDialogFragment : DialogFragment(){

    // ViewBinding 实例
    private var _binding: PrivacyDialogBinding? = null
    private val binding get() = _binding!!

    var onAgree: (()-> Unit)? = null
    var onRefuse: (()-> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 初始化 ViewBinding
        _binding = PrivacyDialogBinding.inflate(inflater, container, false)

        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setOnKeyListener { _, keyCode, _ ->
            requireActivity().finish()
            keyCode == KeyEvent.KEYCODE_BACK
        }
        // 返回 binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 所有控件通过 binding.xxx 访问
        binding.scrollView.isVerticalScrollBarEnabled = true
        binding.scrollView.isScrollbarFadingEnabled = false

        val userSpan = SpannableString(getString(R.string.userAgreement)).apply {
            setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val intent = Intent(requireContext(), AgreementActivity::class.java)
                    intent.putExtra("type", 1)
                    requireContext().startActivity(intent)
                }
            }, 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            setSpan(
                ForegroundColorSpan(Color.parseColor("#D1894A")),
                0,
                length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        val privacySpan = SpannableString(getString(R.string.privacyAgreement)).apply {
            setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val intent = Intent(requireContext(), AgreementActivity::class.java)
                    intent.putExtra("type", 2)
                    requireContext().startActivity(intent)
                }
            }, 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            setSpan(
                ForegroundColorSpan(Color.parseColor("#D1894A")),
                0,
                length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        val stringBuilder = SpannableStringBuilder(getString(R.string.userAndPrivacyAgreementPrefix)).apply {
            append("《").append(userSpan).append("》")
            append(getString(R.string.and))
            append("《").append(privacySpan).append("》")
            append(getString(R.string.userAndPrivacyAgreementSuffix))
        }

        with(binding.contentTextView){
            movementMethod = LinkMovementMethod.getInstance()
            text = stringBuilder
            highlightColor = ContextCompat.getColor(requireContext(), R.color.transparent)
        }

        binding.confirmButton.setOnClickListener {
            onAgree?.invoke()
        }

        binding.disagreeButton.setOnClickListener {
            onRefuse?.invoke()
        }
    }

    override fun onResume() {
        dialog?.window?.attributes?.let { params ->
            params.width = WindowManager.LayoutParams.WRAP_CONTENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            dialog?.window?.attributes = params
        }
        super.onResume()
    }

    // 销毁时释放 binding，防止内存泄漏
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        const val TAG = "FirstDialogFragment"
    }
}