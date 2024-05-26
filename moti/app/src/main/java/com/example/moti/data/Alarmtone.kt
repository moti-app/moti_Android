package com.example.moti.data

import android.provider.Settings
import com.example.moti.data.Alarmtone.Companion.defaultAlarmAlertUri
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Creates a [String] in a format this is understood by [android.media.RingtoneManager]. */
fun Alarmtone.ringtoneManagerUri(): String? {
  return when (this) {
    is Alarmtone.Silent -> null
    is Alarmtone.Default -> defaultAlarmAlertUri
    is Alarmtone.SystemDefault -> defaultAlarmAlertUri
    is Alarmtone.Sound -> uriString
  }
}

@Serializable
sealed class Alarmtone {
  @SerialName("Silent")
  @Serializable
  object Silent : Alarmtone()

  @SerialName("Default")
  @Serializable
  object Default : Alarmtone()

  @SerialName("SystemDefault")
  @Serializable
  object SystemDefault : Alarmtone()

  @SerialName("Sound")
  @Serializable
  data class Sound(val uriString: String) : Alarmtone()

  fun asString(): String {
    return when (this) {
      is Silent -> "Silent"
      is Default -> "Default"
      is SystemDefault -> "SystemDefault"
      is Sound -> uriString
    }
  }

  companion object {
    val defaultAlarmAlertUri =
      Settings.System.DEFAULT_ALARM_ALERT_URI?.toString() ?: "DEFAULT_ALARM_ALERT_URI_IN_TEST"

    fun fromString(string: String): Alarmtone {
      return when (string) {
        "Silent" -> Silent
        "Default" -> Default
        "SystemDefault" -> SystemDefault
        else -> Sound(string)
      }
    }
  }
}
