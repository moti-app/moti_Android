package com.example.moti.ui.afterAction

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.moti.R

class AfterAction : AppCompatActivity() {
    private lateinit var silentDetailTextView: TextView
    private lateinit var silentActionSwitch: SwitchCompat
    private lateinit var openAppTextView: TextView
    private lateinit var openAppSwitch: SwitchCompat
    private lateinit var messageTextView: TextView
    private lateinit var messageSwitch: SwitchCompat
    private lateinit var messageEditText: EditText
    private var contactName: String? = null
    private var silentSwitchOn: Boolean = false
    private var contactPhone: String? = null
    private var messageSwitchOn: Boolean = false
    private var messageText: String? = null
    private var onAppSwitchOn: Boolean = false
    private var selectedAppPackageName: String? = null
    private var appName: String? = null

    companion object {
        private const val PERMISSION_REQUEST_READ_CONTACTS = 100
        private const val PICK_CONTACT_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_after_action)

        val btnBack: ImageButton = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            val resultIntent = Intent().apply {
                messageText = messageEditText.text.toString()
                putExtra("contactPhone", contactPhone)
                putExtra("messageSwitchOn", messageSwitchOn)
                putExtra("messageText", messageText)
                putExtra("onAppSwitchOn", onAppSwitchOn)
                putExtra("selectedAppPackageName", selectedAppPackageName)
                putExtra("silentSwitchOn", silentSwitchOn)
                putExtra("contactName", contactName)
                putExtra("appName", appName)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        // 스위치와 텍스트뷰 초기화
        messageTextView = findViewById(R.id.messageDetailTextView)
        messageSwitch = findViewById(R.id.messageAction)
        silentDetailTextView = findViewById(R.id.silentDetailTextView)
        silentActionSwitch = findViewById(R.id.silentAction)
        openAppTextView = findViewById(R.id.openAppDetailTextView)
        openAppSwitch = findViewById(R.id.openAppAction)
        messageEditText = findViewById(R.id.messageEditText)

        // Intent에서 데이터를 가져옴
        silentSwitchOn = intent.getBooleanExtra("silentSwitchOn", false)
        contactPhone = intent.getStringExtra("contactPhone")
        messageSwitchOn = intent.getBooleanExtra("messageSwitchOn", false)
        messageText = intent.getStringExtra("messageText")
        onAppSwitchOn = intent.getBooleanExtra("onAppSwitchOn", false)
        selectedAppPackageName = intent.getStringExtra("selectedAppPackageName")
        appName = intent.getStringExtra("appName")
        contactName = intent.getStringExtra("contactName")

        // 초기 UI 상태 설정
        silentDetailTextView.text = if (silentSwitchOn) "켜짐" else "꺼짐"
        silentActionSwitch.isChecked = silentSwitchOn

        messageSwitch.isChecked = messageSwitchOn
        messageTextView.text = if (messageSwitchOn) "$contactName, $contactPhone" else "없음"
        messageEditText.setText(messageText)
        messageEditText.visibility = if (messageSwitchOn) View.VISIBLE else View.GONE

        openAppSwitch.isChecked = onAppSwitchOn
        openAppTextView.text = if (onAppSwitchOn) appName else "없음"

        // 스위치 상태 변화 감지
        silentActionSwitch.setOnCheckedChangeListener { _, isChecked ->
            silentDetailTextView.text = if (isChecked) "켜짐" else "꺼짐"
            silentSwitchOn = isChecked
        }

        openAppSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showAppList()
                onAppSwitchOn = true
            } else {
                openAppTextView.text = "없음"
                onAppSwitchOn = false
            }
        }

        messageSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                messageTextView.text = "켜짐"
                sendSMS()
                messageEditText.visibility = EditText.VISIBLE
                messageSwitchOn = true
            } else {
                messageTextView.text = "없음"
                messageEditText.visibility = EditText.GONE
                messageEditText.text.clear()
                messageSwitchOn = false
                messageText = null
            }
        }
    }

    private fun showAppList() {
        val pm = packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }

        val appNames = apps.map { pm.getApplicationLabel(it).toString() }
        val appIcons = apps.map { pm.getApplicationIcon(it) }
        val appPackages = apps.map { it.packageName }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("앱 선택")

        val appList = appNames.mapIndexed { index, name ->
            name to appIcons[index]
        }

        val adapter = AppListAdapter(this, appList)
        builder.setAdapter(adapter) { _, which ->
            val selectedAppName = appNames[which]
            selectedAppPackageName = appPackages[which]
            appName = appNames[which]
            openAppTextView.text = selectedAppName
        }

        builder.setNegativeButton("취소", null)
        builder.show()
    }

    private fun sendSMS() {
        val status = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
        if (status == PackageManager.PERMISSION_GRANTED) {
            Log.d("test", "permission granted")
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            startActivityForResult(intent, PICK_CONTACT_REQUEST)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), PERMISSION_REQUEST_READ_CONTACTS)
            Log.d("test", "permission denied")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_READ_CONTACTS) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                sendSMS()
            } else {
                Log.d("Permission", "Permission denied to read contacts")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            data?.data?.let { contactUri ->
                val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER)
                contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        contactName = cursor.getString(nameIndex)
                        contactPhone = cursor.getString(numberIndex)
                        Log.d("Contact Info", "Name: $contactName, Phone: $contactPhone")
                        messageTextView.text = "$contactName, $contactPhone"
                    }
                }
            }
        }
    }
}

class AppListAdapter(context: Context, private val appList: List<Pair<String, Drawable>>) :
    ArrayAdapter<Pair<String, Drawable>>(context, R.layout.app_list_item, appList) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.app_list_item, parent, false)
        val appName = view.findViewById<TextView>(R.id.appName)
        val appIcon = view.findViewById<ImageView>(R.id.appIcon)

        val (name, icon) = appList[position]
        appName.text = name
        appIcon.setImageDrawable(icon)

        return view
    }
}
