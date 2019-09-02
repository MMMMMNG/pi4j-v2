package com.pi4j.library.pigpio.impl;

/*-
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: LIBRARY  :: JNI Wrapper for PIGPIO Library
 * FILENAME      :  PiGpioBase.java
 *
 * This file is part of the Pi4J project. More information about
 * this project can be found here:  https://pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2019 Pi4J
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import com.pi4j.library.pigpio.*;
import com.pi4j.library.pigpio.internal.PIGPIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.pi4j.library.pigpio.PiGpioConst.*;

/**
 * <p>Abstract PiGpioBase class.</p>
 *
 * @author Robert Savage (<a href="http://www.savagehomeautomation.com">http://www.savagehomeautomation.com</a>)
 * @version $Id: $Id
 */
public abstract class PiGpioBase implements PiGpio {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final Set<Integer> serialHandles = Collections.synchronizedSet(new HashSet<>());
    protected final Set<Integer> i2cHandles = Collections.synchronizedSet(new HashSet<>());
    protected final Set<Integer> spiHandles = Collections.synchronizedSet(new HashSet<>());
    protected List<PiGpioStateChangeListener> stateChangeListeners = new CopyOnWriteArrayList<>();
    protected Map<Integer,List<PiGpioStateChangeListener>> pinChangeListeners = new ConcurrentHashMap<>();
    protected boolean initialized = false;


    /**
     * Close all open handles
     * Returns nothing.
     */
    protected void closeAllOpenHandles() throws IOException {
        // close all open SPI handles
        spiHandles.forEach((handle) -> {
            try {
                logger.trace("[SHUTDOWN] -- CLOSING OPEN SPI HANDLE: [{}]", handle);
                spiClose(handle.intValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // close all open SERIAL handles
        serialHandles.forEach((handle) -> {
            try {
                logger.trace("[SHUTDOWN] -- CLOSING OPEN SERIAL HANDLE: [{}]", handle);
                serClose(handle.intValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // close all open I2C handles
        i2cHandles.forEach((handle) -> {
            try {
                logger.trace("[SHUTDOWN] -- CLOSING OPEN I2C HANDLE: [{}]", handle);
                i2cClose(handle.intValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * <p>validateReady.</p>
     *
     * @throws java.io.IOException if any.
     */
    protected void validateReady() throws IOException {
        validateInitialized();
    }

    /**
     * <p>validateInitialized.</p>
     *
     * @throws java.io.IOException if any.
     */
    protected void validateInitialized() throws IOException {
        if(!this.initialized)
            throw new IOException("PIGPIO NOT INITIALIZED; make sure you call the PiGpio::initialize() function first.");
    }

    /**
     * --------------------------------------------------------------------------
     * GPIO PINS
     * --------------------------------------------------------------------------
     * A Broadcom numbered GPIO, in the range 0-53.
     *
     * There are 54 General Purpose Input Outputs (GPIO) named GPIO0 through GPIO53.
     *
     * They are split into two banks. Bank 1 consists of GPIO0 through GPIO31.
     * Bank 2 consists of GPIO32 through GPIO53.
     *
     * All the GPIO which are safe for the user to read and write are in bank 1.
     * Not all GPIO in bank 1 are safe though. Type 1 boards have 17 safe GPIO.
     * Type 2 boards have 21. Type 3 boards have 26.
     *
     * See gpioHardwareRevision.
     *
     * The user GPIO are marked with an X in the following table.
     *
     *           0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
     * Type 1    X  X  -  -  X  -  -  X  X  X  X  X  -  -  X  X
     * Type 2    -  -  X  X  X  -  -  X  X  X  X  X  -  -  X  X
     * Type 3          X  X  X  X  X  X  X  X  X  X  X  X  X  X
     *
     *          16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31
     * Type 1    -  X  X  -  -  X  X  X  X  X  -  -  -  -  -  -
     * Type 2    -  X  X  -  -  -  X  X  X  X  -  X  X  X  X  X
     * Type 3    X  X  X  X  X  X  X  X  X  X  X  X  -  -  -  -
     *
     * @param pin a int.
     * @throws java.lang.IllegalArgumentException if any.
     * @throws java.lang.IllegalArgumentException if any.
     */
    protected void validateUserPin(int pin) throws IllegalArgumentException {
        validatePin(pin, true);
    }

    /**
     * <p>validatePin.</p>
     *
     * @param pin a int.
     * @throws java.lang.IllegalArgumentException if any.
     * @throws java.lang.IllegalArgumentException if any.
     */
    protected void validatePin(int pin) throws IllegalArgumentException {
        validatePin(pin, false);
    }

    /**
     * <p>validatePin.</p>
     *
     * @param pin a int.
     * @param userPin a boolean.
     * @throws java.lang.IllegalArgumentException if any.
     * @throws java.lang.IllegalArgumentException if any.
     */
    protected void validatePin(int pin, boolean userPin) throws IllegalArgumentException {
        int min = PI_MIN_GPIO;
        int max = ((userPin ? PI_MAX_USER_GPIO : PI_MAX_GPIO));
        if(pin < min || pin > max)
            throw new IllegalArgumentException("Invalid PIN number: " + pin + "; (supported pins: " + min + "-" + max + ")");
    }

    /**
     * <p>validateDutyCycle.</p>
     *
     * @param dutyCycle a int.
     * @throws java.lang.IllegalArgumentException if any.
     * @throws java.lang.IllegalArgumentException if any.
     */
    protected void validateDutyCycle(int dutyCycle) throws IllegalArgumentException{
        int min = 0;
        int max = PI_MAX_DUTYCYCLE_RANGE;
        if(dutyCycle < min || dutyCycle > max)
            throw new IllegalArgumentException("Invalid Duty Cycle: " + dutyCycle +
                    "; (supported duty-cycle: " + min + " - " + max + ")");
    }

    /**
     * <p>validateDutyCycleRange.</p>
     *
     * @param range a int.
     * @throws java.lang.IllegalArgumentException if any.
     * @throws java.lang.IllegalArgumentException if any.
     */
    protected void validateDutyCycleRange(int range) throws IllegalArgumentException{
        int min = PI_MIN_DUTYCYCLE_RANGE;
        int max = PI_MAX_DUTYCYCLE_RANGE;
        if(range < min || range > max)
            throw new IllegalArgumentException("Invalid Duty Cycle Range: " + range +
                    "; (supported range: " + min + " - " + max + ")");
    }

    /**
     * <p>validatePulseWidth.</p>
     *
     * @param pulseWidth a int.
     * @throws java.lang.IllegalArgumentException if any.
     * @throws java.lang.IllegalArgumentException if any.
     */
    protected void validatePulseWidth(int pulseWidth) throws IllegalArgumentException{
        if(pulseWidth == 0) return;
        int min = PI_MIN_SERVO_PULSEWIDTH;
        int max = PI_MAX_SERVO_PULSEWIDTH;
        if(pulseWidth < min || pulseWidth > max)
            throw new IllegalArgumentException("Invalid Pulse-Width: " + pulseWidth +
                    "; (supported pulse-width: " + min + " - " + max + ")");
    }

    /**
     * <p>validateDelayMicroseconds.</p>
     *
     * @param micros a int.
     */
    protected void validateDelayMicroseconds(long micros){
        int min = 0;
        int max = PI_MAX_MICS_DELAY;
        if(micros < min || micros > max)
            throw new IllegalArgumentException("Invalid microseconds delay: " + micros +
                    "; (supported range: " + min + " - " + max + ")");
    }

    /**
     * <p>validateDelayMilliseconds.</p>
     *
     * @param millis a int.
     */
    protected void validateDelayMilliseconds(int millis){
        int min = 0;
        int max = PI_MAX_MILS_DELAY;
        if(millis < min || millis > max)
            throw new IllegalArgumentException("Invalid milliseconds delay: " + millis +
                    "; (supported range: " + min + " - " + max + ")");
    }

    /**
     * <p>validateResult.</p>
     *
     * @param result a {@link com.pi4j.library.pigpio.PiGpioPacket} object.
     * @throws java.io.IOException if any.
     */
    protected void validateResult(PiGpioPacket result) throws IOException{
        validateResult(result.result());
    }

    /**
     * <p>validateResult.</p>
     *
     * @param result a {@link com.pi4j.library.pigpio.PiGpioPacket} object.
     * @param throwException a boolean.
     * @throws java.io.IOException if any.
     */
    protected void validateResult(PiGpioPacket result, boolean throwException) throws IOException{
        validateResult(result.result(), throwException);
    }

    /**
     * <p>validateResult.</p>
     *
     * @param value a long.
     * @throws java.io.IOException if any.
     */
    protected void validateResult(long value) throws IOException {
        validateResult(value, true);
    }

    /**
     * <p>validateResult.</p>
     *
     * @param value a long.
     * @param throwException a boolean.
     * @throws java.io.IOException if any.
     */
    protected void validateResult(long value, boolean throwException) throws IOException {
        if(value < 0) {
            PiGpioError err = PiGpioError.from(value);
            logger.warn("PIGPIO ERROR: " + err.name() + "; " + err.message());
            if(throwException) {
                throw new IOException("PIGPIO ERROR: " + err.name() + "; " + err.message());
            }
        }
    }

    /**
     * <p>validateHandle.</p>
     *
     * @param handle a int.
     * @throws java.io.IOException if any.
     */
    protected void validateHandle(int handle) throws IOException {
        // validate I2C handle
        if(handle < 0) {
            throw new IOException("PIGPIO ERROR: INVALID I2C/SPI/SERIAL HANDLE [" + handle + "]; Valid range: >0");
        }
    }

    /**
     * <p>validateI2cRegister.</p>
     *
     * @param register a int.
     * @throws java.io.IOException if any.
     */
    protected void validateI2cRegister(int register) throws IOException {
        // validate I2C/SMBus register range
        if(register < 0 || register > 255) {
            throw new IOException("PIGPIO ERROR: INVALID I2C REGISTER [" + register + "]; Valid range: 0-255");
        }
    }

    /**
     * <p>validateI2cDeviceAddress.</p>
     *
     * @param device a int.
     * @throws java.io.IOException if any.
     */
    protected void validateI2cDeviceAddress(int device) throws IOException {
        // validate I2C/SMBus device address :: 0-0x7F
        if(device < 0 || device > 0x7F) {
            throw new IOException("PIGPIO ERROR: INVALID I2C DEVICE ADDRESS [" + device + "]; Valid range: 0-127");
        }
    }

    /**
     * <p>validateI2cBus.</p>
     *
     * @param bus a int.
     * @throws java.io.IOException if any.
     */
    protected void validateI2cBus(int bus) throws IOException {
        // validate I2C/SMBus bus number :: >=0
        if(bus < 0) {
            throw new IOException("PIGPIO ERROR: INVALID I2C BUS [" + bus + "]; Valid range: >=0");
        }
    }

    /**
     * <p>validateI2cBlockLength.</p>
     *
     * @param length a int.
     * @throws java.io.IOException if any.
     */
    protected void validateI2cBlockLength(int length) throws IOException {
        // validate I2C/SMBus payload data length :: 0-32
        if(length < 0 || length > 32) {
            throw new IOException("PIGPIO ERROR: INVALID I2C PAYLOAD DATA LENGTH [" + length + "]; Valid range: 0-32");
        }
    }

    /**
     * <p>validateGpioGlitchFilter.</p>
     *
     * @param interval a int.
     * @throws java.io.IOException if any.
     */
    protected void validateGpioGlitchFilter(int interval) throws IOException {
        // validate GPIO glitch filter interval value :: 0-300000
        if(interval < 0 || interval > 300000) {
            throw new IOException("PIGPIO ERROR: INVALID GPIO GLITCH FILTER INTERVAL [" + interval + "]; Valid range: 0-300000");
        }
    }

    /**
     * <p>validateGpioNoiseFilter.</p>
     *
     * @param steady a int.
     * @param active a int.
     * @throws java.io.IOException if any.
     */
    protected void validateGpioNoiseFilter(int steady, int active) throws IOException {
        // validate GPIO noise filter properties
        if(steady < 0 || steady > 300000) {
            throw new IOException("PIGPIO ERROR: INVALID GPIO NOISE FILTER -> STEADY INTERVAL [" + steady + " us]; Valid range: 0-300000");
        }
        if(active < 0 || active > 1000000) {
            throw new IOException("PIGPIO ERROR: INVALID GPIO NOISE FILTER -> ACTIVE INTERVAL [" + steady + " us]; Valid range: 0-1000000");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addPinListener(int pin, PiGpioStateChangeListener listener){
        List<PiGpioStateChangeListener> listeners = null;

        // if the pin already exists in the map, then get the listeners collection by pin number
        if(pinChangeListeners.containsKey(pin)){
            listeners = pinChangeListeners.get(pin);
        }

        // if the pin does not exist in the map, then create a new
        // listener collection for this pin and add it to the map
        else if(!pinChangeListeners.containsKey(pin)){
            listeners = new CopyOnWriteArrayList<>();
            pinChangeListeners.put(pin, listeners);
        }

        // add the new listener object to the listeners collection for this pin index
        if(!listeners.contains(listener)){
            listeners.add(listener);
        }

        // enable this GPIO pin for notification monitoring
        try {
            this.gpioEnableNotifications(pin);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removePinListener(int pin, PiGpioStateChangeListener listener){
        List<PiGpioStateChangeListener> listeners = null;

        // if the pin does not exist in the map, then we are done; nothing to remove
        if(!pinChangeListeners.containsKey(pin)){
            return;
        }

        // if the pin already exists in the map, then get the listeners collection by pin number
        listeners = pinChangeListeners.get(pin);

        // remove the existing listener object from the listeners collection for this pin index
        if(!listeners.contains(listener)){
            listeners.remove(listener);
        }

        // disable this GPIO pin for notification monitoring
        if(listeners.isEmpty()) {
            try {
                this.gpioDisableNotifications(pin);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removePinListeners(int pin){
        List<PiGpioStateChangeListener> listeners = null;

        // if the pin does not exist in the map, then we are done; nothing to remove
        if(!pinChangeListeners.containsKey(pin)){
            return;
        }

        // if the pin already exists in the map, then get the listeners collection by pin number
        listeners = pinChangeListeners.get(pin);

        // remove all listeners from this pin's collection of listeners
        listeners.clear();

        // disable this GPIO pin for notification monitoring
        try {
            this.gpioDisableNotifications(pin);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeAllPinListeners(){
        // remove all pin listeners
        pinChangeListeners.clear();
    }

    /** {@inheritDoc} */
    @Override
    public void addListener(PiGpioStateChangeListener listener){
        // add listener
        if(!stateChangeListeners.contains(listener)) {
            stateChangeListeners.add(listener);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeListener(PiGpioStateChangeListener listener){
        // remove listener
        if(stateChangeListeners.contains(listener)) {
            stateChangeListeners.remove(listener);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeAllListeners(){
        // remove all listeners
        stateChangeListeners.clear();
    }

    /**
     * <p>dispatchEvent.</p>
     *
     * @param event a {@link com.pi4j.library.pigpio.PiGpioStateChangeEvent} object.
     */
    protected void dispatchEvent(final PiGpioStateChangeEvent event){
        // dispatch event to each registered listener
        stateChangeListeners.forEach(listener -> {
            try {
                listener.onChange(event);
            }
            catch (Exception e){
                logger.error(e.getMessage(), e);
            }
        });

        // dispatch event to each registered pin listener
        int pin = event.pin();

        if(pinChangeListeners.containsKey(pin)){
            var listeners = pinChangeListeners.get(pin);
            listeners.forEach(listener -> {
                try {
                    listener.onChange(event);
                }
                catch (Exception e){
                    logger.error(e.getMessage(), e);
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     *
     * Returns the hardware revision (as hexadecimal string).
     *
     * If the hardware revision can not be found or is not a valid hexadecimal number the function returns 0.
     * The hardware revision is the last few characters on the Revision line of /proc/cpuinfo.
     * The revision number can be used to determine the assignment of GPIO to pins (see gpio).
     *
     * There are at least three types of board.
     *  - Type 1 boards have hardware revision numbers of 2 and 3.
     *  - Type 2 boards have hardware revision numbers of 4, 5, 6, and 15.
     *  - Type 3 boards have hardware revision numbers of 16 or greater.
     *
     *     for "Revision : 0002" the function returns 2.
     *     for "Revision : 000f" the function returns 15.
     *     for "Revision : 000g" the function returns 0.
     * @see <a href="http://abyz.me.uk/rpi/pigpio/cif.html#gpioHardwareRevision">PIGPIO::gpioHardwareRevision</a>
     */
    @Override
    public String gpioHardwareRevisionString() throws IOException {
        logger.trace("[HARDWARE] -> GET REVISION (STRING)");
        validateReady();
        long revision = gpioHardwareRevision();
        String revisionString = Integer.toHexString((int)revision);
        logger.trace("[HARDWARE] <- REVISION (STRING): {}", revisionString);
        return revisionString;
    }
}
