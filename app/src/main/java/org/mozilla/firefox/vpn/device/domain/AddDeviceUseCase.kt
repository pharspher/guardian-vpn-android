package org.mozilla.firefox.vpn.device.domain

import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.report.doReport
import org.mozilla.firefox.vpn.service.DeviceInfo
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.findAvailableModelName
import org.mozilla.firefox.vpn.util.onSuccess
import org.mozilla.firefox.vpn.util.then

class AddDeviceUseCase(
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(): Result<DeviceInfo> {
        return userRepository
            .refreshUserInfo()
            .doReport(tag = TAG, successMsg = MSG_REFRESH)
            .then {
                val devices = it.user.devices
                val deviceName = findAvailableModelName(devices)
                deviceRepository
                    .registerDevice(deviceName)
                    .doReport(tag = TAG, successMsg = MSG_REGISTER)
            }
            .onSuccess {
                userRepository
                    .refreshUserInfo()
                    .doReport(tag = TAG, successMsg = MSG_REFRESH)
            }
    }

    companion object {
        private const val TAG = "AddDeviceUseCase"
        private const val MSG_REFRESH = "user info refreshed"
        private const val MSG_REGISTER = "device registered"
    }
}
