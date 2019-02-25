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
    section("Electric") {
        input "lights", "capability.switch", title: "Lights", multiple: true, required: false
	    input "switches", "capability.switch", title: "Switches", multiple: true, required: false
	    input "powers", "capability.powerMeter", title: "Power Sensors", multiple: true, required: false
    }
    section("Environment Sensors") {
        input "temperatures", "capability.temperatureMeasurement", title: "Temperature Sensors", multiple: true, required: false
        input "motions", "capability.motionSensor", title: "Motion Sensors", multiple: true, required: false
        input "humiditysensor", "capability.relativeHumidityMeasurement", title: "Humidity Sensors", multiple: true, required: false
	    input "lightMeters", "capability.illuminanceMeasurement", title: "Illuminance Sensors", multiple: true,  required: false
	}
	section("Security Sensors") {
        input "contacts", "capability.contactSensor", title: "Contact Sensors", multiple: true, required: false
		input "locks", "capability.lock", title: "Locks", multiple: true, required: false
	}

    section("Buttons") {
        input "buttons", "capability.button", title: "Buttons", multiple: true, required: false
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
    subscribe(lights, "switch", lightHandler)
	subscribe(switches, "switch", switchHandler)
    subscribe(powers, "power", powerHandler)
    subscribe(temperatures, "temperature", temperatureHandler)
    subscribe(motions, "motion", motionHandler)
    subscribe(humiditysensor, "humidity", humidityHandler)
	subscribe(lightMeters, "illuminance", illuminanceHandler)
	subscribe(contacts, "contact", contactHandler)
 	subscribe(locks, "lock", lockHandler)
 	subscribe(buttons, "button", buttonHandler)
 	}

def sendEvent(sensorId, sensorName, sensorType, value) {
    // log.debug "sending ${sensorName} ${sensorType} at ${value}"
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

def lightHandler(evt) {
    if (evt.value == "on") {
        sendEvent(evt.displayName + 'light', evt.displayName, 'light', 'on')
		// log.debug "sending ${evt.displayName} light is on at ${evt.date}"
    } else if (evt.value == "off") {
        sendEvent(evt.displayName + 'light', evt.displayName, 'light', 'off')
		// log.debug "sending ${evt.displayName} light is off at ${evt.date}"
    }
}

def powerHandler(evt) {
    sendEvent('powerMeter', evt.displayName, 'power', evt.value, evt.date)
	// log.debug "sending ${evt.displayName} power at ${evt.value} at ${evt.date}"
}

def temperatureHandler(evt) {    
   // log.debug "Hey got to ${evt.displayName} handler at least"
   String temperature = evt.value 
   String temperature = temperature.minus(" F")
   sendEvent(evt.displayName + 'temp', evt.displayName, 'temperature', temperature, evt.date)
   // log.debug "sending ${evt.displayName} temp at ${temperature} at ${evt.date}"
}

def motionHandler(evt) {
    if (evt.value == 'active') {
        sendEvent(evt.displayName + 'motion', evt.displayName, 'motion', 'motion detected', evt.date)
		// log.debug "sending ${evt.displayName} motion detected at ${evt.date}"
    }
    if (evt.value == 'inactive') {
        sendEvent(evt.displayName + 'motion', evt.displayName, 'motion', 'no motion detected', evt.date)
		// log.debug "sending ${evt.displayName} no motion detected at ${evt.date}"
	}
}

def switchHandler(evt) {
    if (evt.value == "on") {
        sendEvent(evt.displayName + 'switch', evt.displayName, 'switch', 'on', evt.date)
		// log.debug "sending ${evt.displayName} switch on at ${evt.date}"
    } else if (evt.value == "off") {
        sendEvent(evt.displayName + 'switch', evt.displayName, 'switch', 'off', evt.date)
		// log.debug "sending ${evt.displayName} switch off at ${evt.date}"
    }
}

def contactHandler(evt) {
    // log.debug "Hey got to ${evt.displayName} handler at least"
	if (evt.value == 'open') {
        sendEvent(evt.displayName + 'contact', evt.displayName, 'contactsensor', 'open', evt.date)
		// log.debug "sending ${evt.displayName} contact open at ${evt.date}"
    }
    if (evt.value == 'closed') {
        sendEvent(evt.displayName + 'contact', evt.displayName, 'contactsensor', 'closed', evt.date)
		// log.debug "sending ${evt.displayName} contact closed at ${evt.date}"
    }
}

def lockHandler(evt) {
    // log.debug "Hey got to ${evt.displayName} handler at least"
	if (evt.value == 'locked') {
        sendEvent(evt.displayName + 'lock', evt.displayName, 'lock', 'locked', evt.date)
		// log.debug "sending ${evt.displayName} lock locked at ${evt.date}"
    }
    if (evt.value == 'unlocked') {
        sendEvent(evt.displayName + 'lock', evt.displayName, 'lock', 'unlocked', evt.date)
    	// log.debug "sending ${evt.displayName} lock unlocked at ${evt.date}"
    }
}

def buttonHandler(evt) {
     // log.debug "Hey got to ${evt.displayName} handler at least"
	 sendEvent(evt.displayName + 'button', evt.displayName, 'button', evt.value, evt.date)
	 // log.debug "sending ${evt.displayName} button ${evt.value} at ${evt.date}"
}

def humidityHandler(evt) {
    sendEvent(evt.displayName + 'humidity', evt.displayName, 'humidity', evt.value, evt.date)
	// log.debug "sending ${evt.displayName} humditiy at ${evt.value} at ${evt.date}"
}

def illuminanceHandler(evt) {
    sendEvent(evt.displayName + 'light', evt.displayName, 'lumens', evt.value, evt.date)
	// log.debug "sending ${evt.displayName} lumens at ${evt.value} at ${evt.date}"
}
