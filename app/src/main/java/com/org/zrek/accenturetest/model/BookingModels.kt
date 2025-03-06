package com.org.zrek.accenturetest.model

data class BookingResponse(
    val shipReference: String,
    val shipToken: String,
    val canIssueTicketChecking: Boolean,
    val expiryTime: String,
    val duration: Int,
    val segments: List<Segment>
)

data class Segment(
    val id: Int,
    val originAndDestinationPair: OriginAndDestinationPair
)

data class OriginAndDestinationPair(
    val destination: Location,
    val destinationCity: String,
    val origin: Location,
    val originCity: String
)

data class Location(
    val code: String,
    val displayName: String,
    val url: String
) 