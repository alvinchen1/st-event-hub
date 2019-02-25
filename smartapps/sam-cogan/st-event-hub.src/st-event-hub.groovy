/**
 *  Azure Event Hub
 *
 *  Copyright 2017 Sam Cogan
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "ST-Event-Hub",
    namespace: "sam-cogan",
    author: "Sam Cogan",
    description: "Smartthings Azure Event Hub Integration",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png") {
    appSetting "EventHubSecret"
    appSetting "EventHubURL"
}


preferences {
    section("Power Meters") {
        input "powers", "capability.powerMeter", title: "Power Sensor", multiple: true, required: false
    }
    section("Temperature Sensors") {
        input "temperatures", "capability.temperatureMeasurement", title: "Temperature Sensors", multiple: true, required: false
    }
    section("Motion Sensors") {
        input "motions", "capability.motionSensor", title: "Motion Sensors", multiple: true, required: false
        }
    section("Switches") {
        input "switches", "capability.switch", title: "Switches", multiple: true, required: false
		}
    section("Contact Sensors") {
        input "contacts", "capability.contactSensor", title: "Contact Sensors", multiple: true, required: false
		}
    section("Buttons") {
        input "buttons", "capability.button", title: "Buttons", multiple: true, required: false
		}
	section("Humidity Sensors") {
        input "humiditysensor", "capability.relativeHumidityMeasurement", title: "Humidity Sensor", multiple: true, required: false
		}
    section("Light Sensors") {
        input "lightMeters", "capability.illuminanceMeasurement", title: "Light Sensors", multiple: true,  required: false
		}
   }

def installed() {
	log.debug "Installed with settings: ${settings}"
	
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
    subscribe(powers, "power", powerHandler)
    subscribe(temperatures, "temperature", temperatureHandler)
    subscribe(motions, "motion", motionHandler)
    subscribe(switches, "switch", switchHandler)
    subscribe(contacts, "contact", contactHandler)
 	subscribe(buttons, "button", buttonHandler)
 	subscribe(humiditysensor, "humidity", humidityHandler)
	subscribe(lightMeters, "illuminance", illuminanceHandler)
}

def sendEvent(sensorId, sensorName, sensorType, value) {
    log.debug "sending ${sensorName} ${sensorType} at ${value}"
    def cleanedSensorId = sensorId.replace(" ", "")
    def params = [
        uri: "${appSettings.EventHubURL}",
        body: "{ sensorId : \"${cleanedSensorId}\", sensorName : \"${sensorName}\", sensorType : \"${sensorType}\", value : \"${value}\" }",
        contentType: "application/xml; charset=utf-8",
        requestContentType: "application/atom+xml;type=entry;charset=utf-8",
        headers: ["Authorization": "${appSettings.EventHubSecret}"],
    ]

	try {
        httpPost(params) { resp -> 
            log.debug "response message ${resp}"
        }
    } catch (e) {
        // For some reason SmartThings treats 200 as an error response, so we need to comment this out to avoid errors. Uncomment the line below to debug errors 
        // log.error "something went wrong: $e"
    }
}

def powerHandler(evt) {
    sendEvent('powerMeter', evt.displayName, 'power', evt.value)
}

def temperatureHandler(evt) {    
   log.debug "Hey got to ${evt.displayName} handler at least"
   sendEvent(evt.displayName + 'temp', evt.displayName, 'temperature', evt.value)
}

def motionHandler(evt) {
    if (evt.value == 'active') {
        sendEvent(evt.displayName + 'motion', evt.displayName, 'motion', 'motion detected')
    }
    if (evt.value == 'inactive') {
        sendEvent(evt.displayName + 'motion', evt.displayName, 'motion', 'no motion detected')
    }
}

def switchHandler(evt) {
    if (evt.value == "on") {
        sendEvent(evt.displayName + 'switch', evt.displayName, 'switch', 'on')
    } else if (evt.value == "off") {
        sendEvent(evt.displayName + 'switch', evt.displayName, 'switch', 'off')
    }
}

def contactHandler(evt) {
    log.debug "Hey got to ${evt.displayName} handler at least"
	if (evt.value == 'open') {
        sendEvent(evt.displayName + 'contact', evt.displayName, 'contactsensor', 'open')
    }
    if (evt.value == 'closed') {
        sendEvent(evt.displayName + 'contact', evt.displayName, 'contactsensor', 'closed')
    }
}

def buttonHandler(evt) {
     log.debug "Hey got to ${evt.displayName} handler at least"
	 sendEvent(evt.displayName + 'button', evt.displayName, 'button', evt.value)
}

def humidityHandler(evt) {
    sendEvent(evt.displayName + 'humidity', evt.displayName, 'humidity', evt.value)
}

def illuminanceHandler(evt) {
    sendEvent(evt.displayName + 'light', evt.displayName, 'lumens', evt.value)
}
