package no.novari.fintkontrolldevicefactory.producer

import no.novari.fintkontrolldevicefactory.service.DeviceService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Component
class DevicePublishingComponent(deviceService: DeviceService,
    ) {


    @Scheduled
    fun publishDeviceGroups() {
        eventTopicService.createOrModifyTopic(eventTopicNameParameters, eventTopicConfiguration)

    }

}