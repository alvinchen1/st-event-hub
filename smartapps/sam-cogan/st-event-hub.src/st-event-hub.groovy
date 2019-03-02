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
 *  Version
 *  1.1    Currently testing temperature, humidity, accleration, and presence sensors, by displaying debug
 *
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
        input "humiditysensors", "capability.relativeHumidityMeasurement", title: "Humidity Sensors", multiple: true, required: false
	    input "lightMeters", "capability.illuminanceMeasurement", title: "Illuminance Sensors", multiple: true,  required: false
		input "uvsensors", "capability.ultravioletIndex", title: "UV Sensors", multiple: true,  required: false
	}
	section("Security Sensors") {
        input "contacts", "capability.contactSensor", title: "Contact Sensors", multiple: true, required: false
		input "locks", "capability.lock", title: "Locks", multiple: true, required: false
		input "accelerationsensors", "capability.accelerationSensor", title: "Acceleration Sensors", multiple: true, required: false
		input "presencesensors", "capability.presenceSensor", title: "Presence Sensors", multiple: true, required: false
	}
	section("Misc") {
        input "buttons", "capability.button", title: "Buttons", multiple: true, required: false
        input "musicdevices", "capability.switch", title: "Music Devices", multiple: true, required: false
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
    subscribe(humiditysensors, "humidity", humidityHandler)
	subscribe(lightMeters, "illuminance", illuminanceHandler)
	subscribe(uvsensors, "ultravioletIndex", uvsensorHandler)
	subscribe(contacts, "contact", contactHandler)
 	subscribe(locks, "lock", lockHandler)
	subscribe(accelerationsensors, "acceleration", accelerationSensorHandler)
	subscribe(presencesensors, "presence", presenceHandler)
 	subscribe(buttons, "button", buttonHandler)
 	subscribe(musicdevices, "status", musicdevicesHandler)
	}

def sendEvent(sensorId, sensorName, sensorType, value) {
    //log.debug "sending ${sensorName} ${sensorType} at ${value}"
    def cleanedSensorId = sensorId.replace(" ", "")
    def params = [
        uri: "${appSettings.EventHubURL}",
        body: "{ sensorId : \"${cleanedSensorId}\", sensorName : \"${sensorName}\", sensorType : \"${sensorType}\", value : \"${value}\"}",
        contentType: "application/xml; charset=utf-8",
        requestContentType: "application/atom+xml;type=entry;charset=utf-8",
        headers: ["Authorization": "${appSettings.EventHubSecret}"],
    ]

	try {
        httpPost(params) { resp -> 
        log.debug "response message ${resp}"
        }
    } catch (e) {
        //For some reason SmartThings treats 200 as an error response, so we need to comment this out to avoid errors. Uncomment the line below to debug errors 
        //log.error "something went wrong: $e"
    }
}

def lightHandler(evt) {
    sendEvent(evt.displayName + 'light', evt.displayName, 'light', evt.value)
	//log.debug "sending ${evt.displayName} light is ${evt.value} at ${evt.date}"    
}

def switchHandler(evt) {
    sendEvent(evt.displayName + 'switch', evt.displayName, 'switch', evt.value)
	//log.debug "sending ${evt.displayName} switch ${evt.value} at ${evt.date}"
}

def powerHandler(evt) {
    sendEvent( evt.displayName + 'powerMeter', evt.displayName, 'power', evt.value)
	//log.debug "sending ${evt.displayName} power at ${evt.value} at ${evt.date}"
}

def temperatureHandler(evt) {    
   sendEvent(evt.displayName + 'temp', evt.displayName, 'temperature', evt.value)
   //sendEvent(evt.displayName + 'temp', evt.displayName, 'temperature', evt.value.minus(" F") , evt.date)
   //log.debug "sending ${evt.displayName} temp at ${temperature} at ${evt.date}"
   //log.debug "sending ${evt.displayName} temp at ${evt.value} at ${evt.date}"
}

def motionHandler(evt) {
    if (evt.value == 'active') {
        sendEvent(evt.displayName + 'motion', evt.displayName, 'motion', 'motion detected')
		//log.debug "sending ${evt.displayName} motion detected at ${evt.date}"
    }
    if (evt.value == 'inactive') {
        sendEvent(evt.displayName + 'motion', evt.displayName, 'motion', 'no motion detected')
		//log.debug "sending ${evt.displayName} no motion detected at ${evt.date}"
	}
}

def humidityHandler(evt) {
    sendEvent(evt.displayName + 'humidity', evt.displayName, 'humidity', evt.value)
	//log.debug "sending ${evt.displayName} humditiy at ${evt.value} at ${evt.date}"
}

def illuminanceHandler(evt) {
    sendEvent(evt.displayName + 'light', evt.displayName, 'lumens', evt.value)
	//log.debug "sending ${evt.displayName} lumens at ${evt.value} at ${evt.date}"
}

def uvsensorHandler(evt) {
    sendEvent(evt.displayName + 'ultraviolet', evt.displayName, 'ultraviolet', evt.value)
	log.debug "sending ${evt.displayName} ultraviolet at ${evt.value} at ${evt.date}"
}

def contactHandler(evt) {
    sendEvent(evt.displayName + 'contact', evt.displayName, 'contactsensor', evt.value)
	//log.debug "sending ${evt.displayName} contact ${evt.value} at ${evt.date}"
}

def lockHandler(evt) {
    //log.debug "got to ${evt.displayName} handler"
 	 if (evt.value == 'locked') {
        sendEvent(evt.displayName + 'lock', evt.displayName, 'lock', evt.value)
		//log.debug "sending ${evt.displayName} lock ${evt.value} at ${evt.date}"
    }
    if (evt.value == 'unlocked') {
        sendEvent(evt.displayName + 'lock', evt.displayName, 'lock', evt.value)
		//log.debug "sending ${evt.displayName} lock ${evt.value} at ${evt.date}"
	}
}

def accelerationSensorHandler(evt) {
    //log.debug "got to ${evt.displayName} handler"
	sendEvent(evt.displayName + 'acceleration', evt.displayName, 'accelerationsensor', evt.value)
	//log.debug "sending ${evt.displayName} acceleration ${evt.value} at ${evt.date}"
}

def presenceHandler(evt) {
    //log.debug "got to ${evt.displayName} handler"
	sendEvent(evt.displayName + 'presence', evt.displayName, 'presencesensor', evt.value)
	//log.debug "sending ${evt.displayName} presence ${evt.value} at ${evt.date}"
}

def buttonHandler(evt) {
  	sendEvent(evt.displayName + 'button', evt.displayName, 'button', evt.value)
	//log.debug "sending ${evt.displayName} button ${evt.value} at ${evt.date}"
}

def musicdevicesHandler(evt) {
  	//log.debug "got to ${evt.displayName} handler"
	if (evt.value == 'playing') {
        sendEvent(evt.displayName + 'musicdevice', evt.displayName, 'musicdevice', 'playing')
		//log.debug "sending ${evt.displayName} playing at ${evt.date}"
    }
    if (evt.value == 'paused') {
        sendEvent(evt.displayName + 'musicdevice', evt.displayName, 'musicdevice', 'stopped')
		//log.debug "sending ${evt.displayName} stopped at ${evt.date}"
	}
	if (evt.value == 'stopped') {
        sendEvent(evt.displayName + 'musicdevice', evt.displayName, 'musicdevice', 'stopped')
		//log.debug "sending ${evt.displayName} stopped at ${evt.date}"
	}
}