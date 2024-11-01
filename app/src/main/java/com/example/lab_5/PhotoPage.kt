package com.example.lab_5

data class PhotoPage(
    val page: Int,
    val pages: Int,
    val perPage: Int,
    val total: Int,
    val photo: List<Photo>
)
