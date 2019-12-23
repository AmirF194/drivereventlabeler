package com.sharif.armin.drivingeventlabeler.util;

import android.hardware.SensorManager;
import android.util.Log;

import com.opencsv.CSVReader;
import com.sharif.armin.drivingeventlabeler.activity.MainActivity;

import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class Utils {
    public static int freq2delay(int f) {
        return (int) (1000 * 1000 / f);
    }

    public static float[] rotate(float[] q, float[] v){
        Quaternion qq = new Quaternion(q[0], q[1], q[2], q[3]);
        return rotate(qq, v);
    }

    public static float[] rotate(Quaternion q, float[] v){
        Quaternion tmp = new Quaternion(0, v[0], v[1], v[2]);
        tmp = q.multiply(tmp).multiply(q.getConjugate());
        float[] res = new float[] {(float)tmp.getQ1(), (float) tmp.getQ2(), (float) tmp.getQ3()};
        return res;
    }

    public static Quaternion getAccMgmOrientationVector(float[] rac, float[] mgm){
        float[] rotationMatrix = new float[9];
        if (SensorManager.getRotationMatrix(rotationMatrix, null, rac, mgm)){
            float[] rotatinVector = new float[3];
            SensorManager.getOrientation(rotationMatrix, rotatinVector);
            Rotation rotation = new Rotation(RotationOrder.XYZ, RotationConvention.VECTOR_OPERATOR,
                    rotatinVector[1], -rotatinVector[2], rotatinVector[0]);
            return new Quaternion(rotation.getQ0(), rotation.getQ1(),rotation.getQ2(),rotation.getQ3());
        }
        return null;
    }

    public static Quaternion euler2quaternion(float[] e){
        double cy = Math.cos(e[2] * 0.5);
        double sy = Math.sin(e[2] * 0.5);
        double cp = Math.cos(e[1] * 0.5);
        double sp = Math.sin(e[1] * 0.5);
        double cr = Math.cos(e[0] * 0.5);
        double sr = Math.sin(e[0] * 0.5);
        double w = cy * cp * cr + sy * sp * sr;
        double x = cy * cp * sr - sy * sp * cr;
        double y = sy * cp * sr + cy * sp * cr;
        double z = sy * cp * cr - cy * sp * sr;
        return new Quaternion(w, x, y, z);
    }

    public static float[] quaternion2euler(float[] q){
        Quaternion qq = new Quaternion(q[0], q[1], q[2], q[3]);
        return quaternion2euler(qq);
    }

    public static float[] quaternion2euler(Quaternion q){
        double sinr_cosp = +2.0 * (q.getQ0() * q.getQ1() + q.getQ2() * q.getQ3());
        double cosr_cosp = +1.0 - 2.0 * (q.getQ1() * q.getQ1() + q.getQ2() * q.getQ2());
        double roll = Math.atan2(sinr_cosp, cosr_cosp);
        double pitch;
        double sinp = +2.0 * (q.getQ0() * q.getQ2() - q.getQ3() * q.getQ1());
        if (Math.abs(sinp) >= 1)
            pitch = Math.signum(sinp) * Math.PI / 2;
        else
            pitch = Math.asin(sinp);
        double siny_cosp = +2.0 * (q.getQ0() * q.getQ3() + q.getQ1() * q.getQ2());
        double cosy_cosp = +1.0 - 2.0 * (q.getQ2() * q.getQ2() + q.getQ3() * q.getQ3());
        double yaw = Math.atan2(siny_cosp, cosy_cosp);
        float []euler = {(float)(roll*180f/Math.PI), (float)(pitch*180f/Math.PI), (float)(yaw*180f/Math.PI)};
        return euler;
    }

    public static float[] getGyrBias() {
        CSVReader csvReader;
        String [] line;
        String path = MainActivity.directory.getPath() + File.separator + "biases.csv";
        float[] ret = {0, 0, 0};
        try {
            csvReader = new CSVReader(new FileReader(path));

            while ((line = csvReader.readNext()) != null) {
                if (line[0].equals("gyr")) {
                    ret[0] = Float.valueOf(line[1]);
                    ret[1] = Float.valueOf(line[2]);
                    ret[2] = Float.valueOf(line[3]);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static float[] getRacBias(){
        CSVReader csvReader;
        String [] line;
        String path = MainActivity.directory.getPath() + File.separator + "biases.csv";
        float[] ret = {0, 0, 0};
        try {
            csvReader = new CSVReader(new FileReader(path));

            while ((line = csvReader.readNext()) != null) {
                if (line[0].equals("rac")) {
                    ret[0] = Float.valueOf(line[1]);
                    ret[1] = Float.valueOf(line[2]);
                    ret[2] = Float.valueOf(line[3]);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

}
