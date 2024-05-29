package com.fdev.spendsmart.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import com.fdev.spendsmart.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText

class CreateCategoryBottomSheet(
    private val onCreateClicked: (String, Int) -> Unit
) : BottomSheetDialogFragment() {

    private val DEFAULT_ICON = R.drawable.ic_bus

    private var selectedIcon: Int = DEFAULT_ICON
    private var isIconSelected: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.create_category_bottom_sheet, container, false)

        val btnCreate = view.findViewById<Button>(R.id.btn_category_create)
        val tieCategoryName = view.findViewById<TextInputEditText>(R.id.tie_category_name)
        val categoryIcon = view.findViewById<ImageView>(R.id.category_icon_new)


        val iconButtons = listOf(
            view.findViewById<ImageButton>(R.id.im_btn_home),
            view.findViewById<ImageButton>(R.id.im_btn_credit_card),
            view.findViewById<ImageButton>(R.id.im_btn_electric),
            view.findViewById<ImageButton>(R.id.im_btn_car),
            view.findViewById<ImageButton>(R.id.im_btn_checkroom),
            view.findViewById<ImageButton>(R.id.im_btn_travel),
            view.findViewById<ImageButton>(R.id.im_btn_wifi),
            view.findViewById<ImageButton>(R.id.im_btn_buy),
            view.findViewById<ImageButton>(R.id.im_btn_gas_station),
            view.findViewById<ImageButton>(R.id.im_btn_water)
        )

        val icons = listOf(
            R.drawable.home,
            R.drawable.ic_credit_card,
            R.drawable.electric,
            R.drawable.ic_car,
            R.drawable.ic_checkroom,
            R.drawable.travel,
            R.drawable.wifi,
            R.drawable.ic_shopping_cart,
            R.drawable.ic_gas_station,
            R.drawable.ic_water
        )

        for ((index, button) in iconButtons.withIndex()) {
            button.setOnClickListener {
                selectedIcon = icons[index]
                isIconSelected = true
                selectedCategory(button, iconButtons)
                categoryIcon.setImageResource(selectedIcon)
            }
        }

        btnCreate?.setOnClickListener {
            val name = tieCategoryName?.text?.toString() ?: ""
            when {
                name.isEmpty() -> {
                    Toast.makeText(requireContext(), "Please enter a category name", Toast.LENGTH_SHORT).show()
                }
                !isIconSelected -> {
                    Toast.makeText(requireContext(), "Please select an icon", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    onCreateClicked.invoke(name,  selectedIcon)
                    dismiss()
                }
            }
        }

        return view
    }

    private fun selectedCategory(selectedButton: ImageButton, allButtons: List<ImageButton>) {
        for (button in allButtons) {
            button.setBackgroundResource(android.R.color.transparent)
        }
        selectedButton.setBackgroundResource(R.drawable.filter_background_expense)
    }
}
