package ru.tusur.presentation.about

import androidx.lifecycle.ViewModel

class AboutViewModel : ViewModel() {
    val version: String = "v1.0.0 (Build 20251219)"
    val copyright: String = "© 2025, TUSSUR Student"
    val license: String = "MIT License — For non-commercial use"
}