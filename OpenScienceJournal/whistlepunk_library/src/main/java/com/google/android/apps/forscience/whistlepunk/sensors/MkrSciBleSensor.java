package com.google.android.apps.forscience.whistlepunk.sensors;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.android.apps.forscience.ble.MkrSciBleManager;
import com.google.android.apps.forscience.whistlepunk.Clock;
import com.google.android.apps.forscience.whistlepunk.metadata.MkrSciBleSensorSpec;
import com.google.android.apps.forscience.whistlepunk.sensorapi.AbstractSensorRecorder;
import com.google.android.apps.forscience.whistlepunk.sensorapi.ScalarSensor;
import com.google.android.apps.forscience.whistlepunk.sensorapi.SensorEnvironment;
import com.google.android.apps.forscience.whistlepunk.sensorapi.SensorRecorder;
import com.google.android.apps.forscience.whistlepunk.sensorapi.SensorStatusListener;
import com.google.android.apps.forscience.whistlepunk.sensorapi.StreamConsumer;

/**
 * Class to get sensor data from a MkrSciBle sensor.
 */
public class MkrSciBleSensor extends ScalarSensor {

    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static final String SENSOR_SCIENCE_KIT_INPUT_1 = "sk_input_1";
    public static final String SENSOR_SCIENCE_KIT_INPUT_2 = "sk_input_2";
    public static final String SENSOR_SCIENCE_KIT_INPUT_3 = "sk_input_3";
    public static final String SENSOR_SCIENCE_KIT_VOLTAGE = "sk_voltage";
    public static final String SENSOR_SCIENCE_KIT_CURRENT = "sk_current";
    public static final String SENSOR_SCIENCE_KIT_RESISTANCE = "sk_resistance";
    public static final String SENSOR_SCIENCE_KIT_ACCELEROMETER_X = "sk_accelerometer_x";
    public static final String SENSOR_SCIENCE_KIT_ACCELEROMETER_Y = "sk_accelerometer_y";
    public static final String SENSOR_SCIENCE_KIT_ACCELEROMETER_Z = "sk_accelerometer_z";
    public static final String SENSOR_SCIENCE_KIT_LINEAR_ACCELEROMETER = "sk_linear_accelerometer";
    public static final String SENSOR_SCIENCE_KIT_GYROSCOPE_X = "sk_gyroscope_x";
    public static final String SENSOR_SCIENCE_KIT_GYROSCOPE_Y = "sk_gyroscope_y";
    public static final String SENSOR_SCIENCE_KIT_GYROSCOPE_Z = "sk_gyroscope_z";
    public static final String SENSOR_SCIENCE_KIT_MAGNETOMETER = "sk_magnetometer";

    public static final String SENSOR_NANO_33_BLE_SENSE_ACCELEROMETER_X = "n33bs_accelerometer_x";
    public static final String SENSOR_NANO_33_BLE_SENSE_ACCELEROMETER_Y = "n33bs_accelerometer_y";
    public static final String SENSOR_NANO_33_BLE_SENSE_ACCELEROMETER_Z = "n33bs_accelerometer_z";
    public static final String SENSOR_NANO_33_BLE_SENSE_LINEAR_ACCELEROMETER = "n33bs_linear_accelerometer";
    public static final String SENSOR_NANO_33_BLE_SENSE_GYROSCOPE_X = "n33bs_gyroscope_x";
    public static final String SENSOR_NANO_33_BLE_SENSE_GYROSCOPE_Y = "n33bs_gyroscope_y";
    public static final String SENSOR_NANO_33_BLE_SENSE_GYROSCOPE_Z = "n33bs_gyroscope_z";
    public static final String SENSOR_NANO_33_BLE_SENSE_MAGNETOMETER = "n33bs_magnetometer";
    public static final String SENSOR_NANO_33_BLE_SENSE_TEMPERATURE = "n33bs_temperature";
    public static final String SENSOR_NANO_33_BLE_SENSE_PRESSURE = "n33bs_pressure";
    public static final String SENSOR_NANO_33_BLE_SENSE_HUMIDITY = "n33bs_humidity";
    public static final String SENSOR_NANO_33_BLE_SENSE_PROXIMITY = "n33bs_proximity";
    public static final String SENSOR_NANO_33_BLE_SENSE_COLOR_ILLUMINANCE = "n33bs_color_illuminance";
    public static final String SENSOR_NANO_33_BLE_SENSE_COLOR_TEMPERATURE = "n33bs_color_temperature";

    public static final String HANDLER_RAW = "raw";
    public static final String HANDLER_TEMPERATURE_CELSIUS = "temperature_celsius";
    public static final String HANDLER_TEMPERATURE_FAHRENHEIT = "temperature_fahrenheit";
    public static final String HANDLER_LIGHT = "light";

    private String address;

    private String characteristic;

    private ValueHandler valueHandler;

    public MkrSciBleSensor(String sensorId, MkrSciBleSensorSpec spec) {
        super(sensorId);
        address = spec.getAddress();
        final String sensorKind = spec.getSensor();
        final String sensorHandler = spec.getHandler();
        switch (sensorKind) {
            // Science Kit Sensors
            case SENSOR_SCIENCE_KIT_INPUT_1:
                characteristic = MkrSciBleManager.SCIENCE_KIT_INPUT_1_UUID;
                switch (sensorHandler) {
                    case HANDLER_TEMPERATURE_CELSIUS:
                        valueHandler = new FormulaValueHandler(0, value -> (((value * 3300d) / 1023d) - 500) * 0.1d);
                        break;
                    case HANDLER_TEMPERATURE_FAHRENHEIT:
                        valueHandler = new FormulaValueHandler(0, value -> (((((value * 3300d) / 1023d) - 500) * 0.1d) * (9d / 5d)) + 32d);
                        break;
                    case HANDLER_LIGHT:
                        valueHandler = new FormulaValueHandler(0, value -> ((value * 3300d) / 1023d) * 0.5d);
                        break;
                    default:
                        valueHandler = new SimpleValueHandler(0);
                        break;
                }
                break;
            case SENSOR_SCIENCE_KIT_INPUT_2:
                characteristic = MkrSciBleManager.SCIENCE_KIT_INPUT_2_UUID;
                if (HANDLER_LIGHT.equals(sensorHandler)) {
                    valueHandler = new FormulaValueHandler(0, value -> ((value * 3300d) / 1023d) * 0.5d);
                } else {
                    valueHandler = new SimpleValueHandler(0);
                }
                break;
            case SENSOR_SCIENCE_KIT_INPUT_3:
                characteristic = MkrSciBleManager.SCIENCE_KIT_INPUT_3_UUID;
                valueHandler = new SimpleValueHandler(0);
                break;
            case SENSOR_SCIENCE_KIT_VOLTAGE:
                characteristic = MkrSciBleManager.SCIENCE_KIT_VOLTAGE_UUID;
                valueHandler = new SimpleValueHandler(0);
                break;
            case SENSOR_SCIENCE_KIT_CURRENT:
                characteristic = MkrSciBleManager.SCIENCE_KIT_CURRENT_UUID;
                valueHandler = new SimpleValueHandler(0);
                break;
            case SENSOR_SCIENCE_KIT_RESISTANCE:
                characteristic = MkrSciBleManager.SCIENCE_KIT_RESISTANCE_UUID;
                valueHandler = new ResistanceValueHandler(0);
                break;
            case SENSOR_SCIENCE_KIT_ACCELEROMETER_X:
                characteristic = MkrSciBleManager.SCIENCE_KIT_ACCELEROMETER_UUID;
                valueHandler = new ScienceKitAccelerometerValueHandler(0);
                break;
            case SENSOR_SCIENCE_KIT_ACCELEROMETER_Y:
                characteristic = MkrSciBleManager.SCIENCE_KIT_ACCELEROMETER_UUID;
                valueHandler = new ScienceKitAccelerometerValueHandler(1);
                break;
            case SENSOR_SCIENCE_KIT_ACCELEROMETER_Z:
                characteristic = MkrSciBleManager.SCIENCE_KIT_ACCELEROMETER_UUID;
                valueHandler = new ScienceKitAccelerometerValueHandler(2);
                break;
            case SENSOR_SCIENCE_KIT_LINEAR_ACCELEROMETER:
                characteristic = MkrSciBleManager.SCIENCE_KIT_ACCELEROMETER_UUID;
                valueHandler = new ScienceKitLinearAccelerometerValueHandler();
                break;
            case SENSOR_SCIENCE_KIT_GYROSCOPE_X:
                characteristic = MkrSciBleManager.SCIENCE_KIT_GYROSCOPE_UUID;
                valueHandler = new SimpleValueHandler(0);
                break;
            case SENSOR_SCIENCE_KIT_GYROSCOPE_Y:
                characteristic = MkrSciBleManager.SCIENCE_KIT_GYROSCOPE_UUID;
                valueHandler = new SimpleValueHandler(1);
                break;
            case SENSOR_SCIENCE_KIT_GYROSCOPE_Z:
                characteristic = MkrSciBleManager.SCIENCE_KIT_GYROSCOPE_UUID;
                valueHandler = new SimpleValueHandler(2);
                break;
            case SENSOR_SCIENCE_KIT_MAGNETOMETER:
                characteristic = MkrSciBleManager.SCIENCE_KIT_MAGNETOMETER_UUID;
                valueHandler = new ScienceKitMagnetometerValueHandler();
                break;
            // Nano 33 BLE Sensors
            case SENSOR_NANO_33_BLE_SENSE_ACCELEROMETER_X:
                characteristic = MkrSciBleManager.NANO_33_BLE_SENSE_ACCELEROMETER_UUID;
                valueHandler = new FormulaValueHandler(0, value -> value * -10);
                break;
            case SENSOR_NANO_33_BLE_SENSE_ACCELEROMETER_Y:
                characteristic = MkrSciBleManager.NANO_33_BLE_SENSE_ACCELEROMETER_UUID;
                valueHandler = new FormulaValueHandler(1, value -> value * -10);
                break;
            case SENSOR_NANO_33_BLE_SENSE_ACCELEROMETER_Z:
                characteristic = MkrSciBleManager.NANO_33_BLE_SENSE_ACCELEROMETER_UUID;
                valueHandler = new FormulaValueHandler(2, value -> value * 10);
                break;
            case SENSOR_NANO_33_BLE_SENSE_LINEAR_ACCELEROMETER:
                characteristic = MkrSciBleManager.NANO_33_BLE_SENSE_ACCELEROMETER_UUID;
                valueHandler = new Nano33BleSenseLinearAccelerometerValueHandler();
                break;
            case SENSOR_NANO_33_BLE_SENSE_GYROSCOPE_X:
                characteristic = MkrSciBleManager.NANO_33_BLE_SENSE_GYROSCOPE_UUID;
                valueHandler = new FormulaValueHandler(0, value -> -value);
                break;
            case SENSOR_NANO_33_BLE_SENSE_GYROSCOPE_Y:
                characteristic = MkrSciBleManager.NANO_33_BLE_SENSE_GYROSCOPE_UUID;
                valueHandler = new FormulaValueHandler(1, value -> -value);
                break;
            case SENSOR_NANO_33_BLE_SENSE_GYROSCOPE_Z:
                characteristic = MkrSciBleManager.NANO_33_BLE_SENSE_GYROSCOPE_UUID;
                valueHandler = new SimpleValueHandler(2);
                break;
            case SENSOR_NANO_33_BLE_SENSE_MAGNETOMETER:
                characteristic = MkrSciBleManager.NANO_33_BLE_SENSE_MAGNETOMETER_UUID;
                valueHandler = new Nano33BLESenseMagnetometerValueHandler();
                break;
            case SENSOR_NANO_33_BLE_SENSE_TEMPERATURE:
                characteristic = MkrSciBleManager.NANO_33_BLE_SENSE_TEMPERATURE_UUID;
                switch (sensorHandler) {
                    case HANDLER_TEMPERATURE_FAHRENHEIT:
                        valueHandler = new FormulaValueHandler(0, value -> (value * (9d / 5d)) + 32d);
                        break;
                    case HANDLER_TEMPERATURE_CELSIUS:
                    default:
                        valueHandler = new SimpleValueHandler(0);
                        break;
                }
                break;
            case SENSOR_NANO_33_BLE_SENSE_PRESSURE:
                characteristic = MkrSciBleManager.NANO_33_BLE_SENSE_PRESSURE_UUID;
                valueHandler = new FormulaValueHandler(0, value -> value / 101.325d);
                break;
            case SENSOR_NANO_33_BLE_SENSE_HUMIDITY:
                characteristic = MkrSciBleManager.NANO_33_BLE_SENSE_HUMIDITY_UUID;
                valueHandler = new SimpleValueHandler(0);
                break;
            case SENSOR_NANO_33_BLE_SENSE_PROXIMITY:
                characteristic = MkrSciBleManager.NANO_33_BLE_SENSE_PROXIMITY_UUID;
                valueHandler = new FormulaValueHandler(0, value -> 100d - ((value / 255d) * 100d));
                break;
            case SENSOR_NANO_33_BLE_SENSE_COLOR_ILLUMINANCE:
                characteristic = MkrSciBleManager.NANO_33_BLE_SENSE_COLOR_UUID;
                valueHandler = new Nano33BLESenseColorIlluminanceValueHandler();
                break;
            case SENSOR_NANO_33_BLE_SENSE_COLOR_TEMPERATURE:
                characteristic = MkrSciBleManager.NANO_33_BLE_SENSE_COLOR_UUID;
                valueHandler = new Nano33BLESenseColorTemperatureValueHandler();
                break;
            default:
                throw new RuntimeException("Unmanaged mkr sci ble sensor: " + sensorKind);
        }
    }

    @Override
    protected SensorRecorder makeScalarControl(
            StreamConsumer c,
            SensorEnvironment environment,
            Context context,
            SensorStatusListener listener) {
        final Clock clock = environment.getDefaultClock();
        final MkrSciBleManager.Listener mkrSciBleListener =
                new MkrSciBleManager.Listener() {

                    private boolean connected = false;

                    @Override
                    public void onFirmwareVersion(long firmwareVersion) {
                        valueHandler.setFirmwareVersion(firmwareVersion);
                    }

                    @Override
                    public void onValuesUpdated(double[] values) {
                        if (!connected) {
                            connected = true;
                            handler.post(
                                    () -> listener.onSourceStatus(getId(), SensorStatusListener.STATUS_CONNECTED));
                        }
                        valueHandler.handle(c, clock.getNow(), values);
                    }
                };
        return new AbstractSensorRecorder() {
            @Override
            public void startObserving() {
                handler.post(
                        () -> listener.onSourceStatus(getId(), SensorStatusListener.STATUS_CONNECTING));
                MkrSciBleManager.subscribe(context, address, characteristic, mkrSciBleListener);
            }

            @Override
            public void stopObserving() {
                MkrSciBleManager.unsubscribe(address, characteristic, mkrSciBleListener);
                listener.onSourceStatus(getId(), SensorStatusListener.STATUS_DISCONNECTED);
            }
        };
    }

    private abstract static class ValueHandler {
        long firmwareVersion = 0;

        void setFirmwareVersion(long version) {
            this.firmwareVersion = version;
        }

        abstract void handle(StreamConsumer c, long ts, double[] values);
    }

    private static class SimpleValueHandler extends ValueHandler {
        private int index;

        private SimpleValueHandler(int index) {
            this.index = index;
        }

        @Override
        public void handle(StreamConsumer c, long ts, double[] values) {
            if (values.length > index) {
                c.addData(ts, values[index]);
            }
        }
    }

    private interface Formula {
        double compute(double value);
    }

    private static class FormulaValueHandler extends ValueHandler {
        private int index;
        private Formula formula;

        private FormulaValueHandler(int index, Formula formula) {
            this.index = index;
            this.formula = formula;
        }

        @Override
        public void handle(StreamConsumer c, long ts, double[] values) {
            if (values.length > index) {
                c.addData(ts, formula.compute(values[index]));
            }
        }
    }

    private static class ScienceKitAccelerometerValueHandler extends ValueHandler {
        private int index;

        private ScienceKitAccelerometerValueHandler(int index) {
            this.index = index;
        }

        @Override
        public void handle(StreamConsumer c, long ts, double[] values) {
            if (values.length > index) {
                c.addData(ts, values[index] * (firmwareVersion < 2 ? 10 : 1));
            }
        }
    }

    private static class ScienceKitLinearAccelerometerValueHandler extends ValueHandler {
        @Override
        public void handle(StreamConsumer c, long ts, double[] values) {
            if (values.length < 3) {
                return;
            }
            c.addData(ts, Math.sqrt((values[0] * values[0]) + (values[1] * values[1]) + (values[2] * values[2])) * (firmwareVersion < 2 ? 10 : 1));
        }
    }

    private static class Nano33BleSenseLinearAccelerometerValueHandler extends ValueHandler {
        @Override
        public void handle(StreamConsumer c, long ts, double[] values) {
            if (values.length < 3) {
                return;
            }
            c.addData(ts, Math.sqrt((values[0] * values[0]) + (values[1] * values[1]) + (values[2] * values[2])) * 10);
        }
    }

    private static class ResistanceValueHandler extends ValueHandler {

        private int index;

        private ResistanceValueHandler(int index) {
            this.index = index;
        }

        @Override
        public void handle(StreamConsumer c, long ts, double[] values) {
            if (values.length > index) {
                double v = values[index] / 1000D;
                if (v > 1000D) {
                    c.addData(ts, 1000D);
                } else c.addData(ts, Math.max(v, 0D));
            }
        }
    }

    private static class ScienceKitMagnetometerValueHandler extends ValueHandler {
        @Override
        public void handle(StreamConsumer c, long ts, double[] values) {
            if (values.length < 3) {
                return;
            }
            c.addData(ts, Math.sqrt((values[0] * values[0]) + (values[1] * values[1]) + (values[2] * values[2])) * 100);
        }
    }

    private static class Nano33BLESenseMagnetometerValueHandler extends ValueHandler {
        @Override
        public void handle(StreamConsumer c, long ts, double[] values) {
            if (values.length < 3) {
                return;
            }
            c.addData(ts, Math.sqrt((values[0] * values[0]) + (values[1] * values[1]) + (values[2] * values[2])));
        }
    }

    private static class Nano33BLESenseColorIlluminanceValueHandler extends ValueHandler {
        @Override
        public void handle(StreamConsumer c, long ts, double[] values) {
            if (values.length < 4) {
                return;
            }
            final double y = (values[3] / 4097d) * 2.8d * 1000d;
            c.addData(ts, y);
        }
    }

    private static class Nano33BLESenseColorTemperatureValueHandler extends ValueHandler {
        @Override
        public void handle(StreamConsumer c, long ts, double[] values) {
            if (values.length < 3) {
                return;
            }
            // 1. Map RGB values to their XYZ counterparts.
            final double r = (values[0] / 4097d);
            final double g = (values[1] / 4097d);
            final double b = (values[2] / 4097d);
            final double x = (0.412453 * r) + (0.35758 * g) + (0.180423 * b);
            final double y = (0.212671 * r) + (0.71516 * g) + (0.072169 * b);
            final double z = (0.019334 * r) + (0.119193 * g) + (0.950227 * b);
            // 2. Calculate the chromaticity co-ordinates
            final double xchrome = x / (x + y + z);
            final double ychrome = y / (x + y + z);
            // 3. Use to determine the CCT
            final double n = (xchrome - 0.3320) / (0.1858 - ychrome);
            // 4. Calculate the final CCT
            double cct = (449.0 * Math.pow(n, 3)) + (3525.0 * Math.pow(n, 2)) + (6823.3 * n) + 5520.33;
            if (cct > 15000d) {
                cct = 15000d;
            } else if (cct < 0) {
                cct = 0d;
            }
            // Sets the results in degrees Kelvin
            c.addData(ts, cct);
        }
    }

    public static MkrSciBleSensorSpec validateSpec(MkrSciBleSensorSpec spec) {
        String sensorKind = spec.getSensor();
        switch (sensorKind) {
            case SENSOR_SCIENCE_KIT_INPUT_1:
            case SENSOR_SCIENCE_KIT_INPUT_2:
            case SENSOR_SCIENCE_KIT_INPUT_3:
            case SENSOR_SCIENCE_KIT_VOLTAGE:
            case SENSOR_SCIENCE_KIT_CURRENT:
            case SENSOR_SCIENCE_KIT_RESISTANCE:
            case SENSOR_SCIENCE_KIT_ACCELEROMETER_X:
            case SENSOR_SCIENCE_KIT_ACCELEROMETER_Y:
            case SENSOR_SCIENCE_KIT_ACCELEROMETER_Z:
            case SENSOR_SCIENCE_KIT_LINEAR_ACCELEROMETER:
            case SENSOR_SCIENCE_KIT_GYROSCOPE_X:
            case SENSOR_SCIENCE_KIT_GYROSCOPE_Y:
            case SENSOR_SCIENCE_KIT_GYROSCOPE_Z:
            case SENSOR_SCIENCE_KIT_MAGNETOMETER:
            case SENSOR_NANO_33_BLE_SENSE_ACCELEROMETER_X:
            case SENSOR_NANO_33_BLE_SENSE_ACCELEROMETER_Y:
            case SENSOR_NANO_33_BLE_SENSE_ACCELEROMETER_Z:
            case SENSOR_NANO_33_BLE_SENSE_LINEAR_ACCELEROMETER:
            case SENSOR_NANO_33_BLE_SENSE_GYROSCOPE_X:
            case SENSOR_NANO_33_BLE_SENSE_GYROSCOPE_Y:
            case SENSOR_NANO_33_BLE_SENSE_GYROSCOPE_Z:
            case SENSOR_NANO_33_BLE_SENSE_MAGNETOMETER:
            case SENSOR_NANO_33_BLE_SENSE_TEMPERATURE:
            case SENSOR_NANO_33_BLE_SENSE_PRESSURE:
            case SENSOR_NANO_33_BLE_SENSE_HUMIDITY:
            case SENSOR_NANO_33_BLE_SENSE_PROXIMITY:
            case SENSOR_NANO_33_BLE_SENSE_COLOR_ILLUMINANCE:
            case SENSOR_NANO_33_BLE_SENSE_COLOR_TEMPERATURE:
                break;
            default:
                throw new RuntimeException("Unmanaged mkr sci ble sensor: " + sensorKind);
        }
        return spec;
    }
}
