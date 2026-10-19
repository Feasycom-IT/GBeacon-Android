package com.glowbird.gbeacon.ui.ad

import androidx.lifecycle.ViewModel
import com.glowbird.gbeacon.logic.BeaconRepository

class AdViewModel: ViewModel() {
    val mAdLiveData = BeaconRepository.getAdData()
}