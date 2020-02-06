package com.aeropay_merchant.activity

import AP.model.MerchantLocationDevices
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import com.aeropay_merchant.Model.AeropayModelManager
import com.aeropay_merchant.R
import com.aeropay_merchant.Utilities.ConstantsStrings
import com.aeropay_merchant.Utilities.PrefKeeper
import com.aeropay_merchant.communication.AWSConnectionManager
import com.aeropay_merchant.communication.DefineID
import java.math.BigDecimal


class SettingsScreenActivity : BaseActivity() {

    lateinit var save : Button
    lateinit var deviceNameSpinner : Spinner
    lateinit var storeLocationSpinner : Spinner
    var storeLocation : String? = null
    var deviceName : String? = null
    lateinit var backButton : ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_screen)

        initialiseControls()
    }

    // inflating UI controls
    private fun initialiseControls() {
        save = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.back_button)
        storeLocationSpinner = findViewById(R.id.storeSpinner)
        deviceNameSpinner = findViewById(R.id.deviceSpinner)
        deviceNameSpinner.visibility = View.GONE

        createStoreSpinner(this)

        if(PrefKeeper.storeName.equals(ConstantsStrings().noValue)|| PrefKeeper.deviceName.equals(
                ConstantsStrings().noValue)){
            backButton.visibility = View.GONE
        }
        else{
            backButton.setOnClickListener(View.OnClickListener {
                finish()
            })
        }
    }

    // creating spinner to select store from dropdown list
    private fun createStoreSpinner(context: Context?) {

        var objModelManager = AeropayModelManager().getInstance()
        var modelOutPut = objModelManager.merchantLocationsModel

        var arraySize = modelOutPut.locations.size - 1
        var storeListName: MutableList<String> = ArrayList()

        storeListName.add(0,"Select store")
        var count = 1

        for(i in 0..arraySize){
            storeListName.add(count,modelOutPut.locations[i].name)
            count++
        }

        if (storeLocationSpinner != null) {
            val arrayAdapter = ArrayAdapter<String>(context!!,R.layout.spinner_layout, storeListName!!)
            storeLocationSpinner?.adapter = arrayAdapter

            storeLocationSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    if(position != 0)
                    {
                        storeLocation = storeListName[position]
                        var arrayPosition = position - 1
                        (context as SettingsScreenActivity).onStoreSelectedEvent(arrayPosition)
                       /* var objModelManager = AeropayModelManager().getInstance()
                        objModelManager.createSyncPayload.payloadList.clear()
                        objModelManager.subscriptionPayloadForList.payloadList.clear()*/
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            }
        }
    }

    // creating spinner to select devices from dropdown list
    fun createDeviceSpinner() {
        deviceNameSpinner.visibility = View.VISIBLE

        var objModelManager = AeropayModelManager().getInstance()
        var modelOutPut = objModelManager.merchantDevicesModel

        if(modelOutPut.devices != null){

            var arraySize = modelOutPut.devices.size - 1
            var storeListName: MutableList<String> = ArrayList()
            var count = 0

            for(i in 0..arraySize){
                storeListName.add(count,modelOutPut.devices[i].name as String)
                count++
            }

            if (deviceNameSpinner != null) {
                val devicesAdapter = ArrayAdapter<String>(this,R.layout.spinner_layout, storeListName!!)
                deviceNameSpinner?.adapter = devicesAdapter
                deviceNameSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        deviceName = storeListName[position]
                        PrefKeeper.merchantDeviceId = modelOutPut.devices[position].merchantLocationDeviceId.intValueExact()
                        PrefKeeper.merchantLocationId = modelOutPut.devices[position].merchantLocationId.intValueExact()
                        PrefKeeper.merchantLocationDeviceId = modelOutPut.devices[position].merchantLocationDeviceId.intValueExact()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {

                    }
                }
            }
        }
    }

    fun onSaveButtonClick(view: View) {
        if(storeLocation.isNullOrEmpty() || deviceName.isNullOrEmpty()){
            showMsgToast("Please select your store location and Device name")
        }
        else{
            PrefKeeper.storeName = storeLocation
            PrefKeeper.deviceName = deviceName
            PrefKeeper.minorId = -1
            launchActivity(HomeActivity::class.java)
        }
    }

    // saving store name and device name on save button click
    fun onStoreSelectedEvent(position : Int){
        var objModelManager = AeropayModelManager().getInstance()

        var merchantLocation = MerchantLocationDevices()
        var merchantLocationValue = objModelManager.merchantLocationsModel.locations[position].merchantLocationId as Double

        merchantLocation.locationId =  merchantLocationValue.toBigDecimal()

        var awsConnectionManager = AWSConnectionManager(this)
        awsConnectionManager.hitServer(DefineID().FETCH_MERCHANT_DEVICES,this,merchantLocation)
    }
}
