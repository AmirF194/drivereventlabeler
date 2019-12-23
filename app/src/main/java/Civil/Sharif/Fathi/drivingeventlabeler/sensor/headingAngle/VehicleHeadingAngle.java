package com.sharif.armin.drivingeventlabeler.sensor.headingAngle;

public class VehicleHeadingAngle {
    private static final float MS2S = 1.0f / 1000.0f;

    private float sigma2W, sigma2A;
    private float[] muX = new float[2];
    private float[][] covX = new float[2][2];
    private float prevTime = 0;

    public float getAngle(){
        return this.muX[0];
    }
    public void setMuX(float [] muX){
        this.muX = muX;
    }
    public void setCovX(float [][] covX){
        this.covX = covX;
    }
    public void setSigmaW(float sigma2W){
        this.sigma2W = sigma2W;
    }
    public void setSigmaA(float sigma2A){
        this.sigma2A = sigma2A;
    }
    public void setPrevTime(float prevTime){
        this.prevTime = prevTime;
    }

    public void reset(){
        prevTime = 0;
    }

    public void predict(float w, long time){
        if (prevTime != 0) {
            float dT = (time - prevTime) * MS2S;
            float alpha = this.muX[0], bias = this.muX[1];
            float c11 = this.covX[0][0], c12 = this.covX[0][1],
                    c21 = this.covX[1][0], c22 = this.covX[1][1];
            this.muX[0] = alpha + (w - bias) * dT;
            this.muX[1] = bias;
            this.covX[0][0] = c11 - dT * c21 + dT * dT * c22 - dT * c12 + dT * this.sigma2W;
            this.covX[0][1] = c12 - dT * c22;
            this.covX[1][0] = c21 - dT * c22;
            this.covX[1][1] = c22 + this.sigma2W;
        }
        prevTime = time;
    }
    public void update(float alphaZ){
        float alpha = this.muX[0], bias = this.muX[1];
        float c11 = this.covX[0][0], c12 = this.covX[0][1],
                c21 = this.covX[1][0], c22 = this.covX[1][1];
        float covY = c11 + sigma2A;
        float K11 = 0, K21 = 0;
        if (covY != 0){
            K11 = c11 / covY;
            K21 = c21 / covY;
        }
        this.muX[0] = alpha + K11 * (alphaZ - alpha);
        this.muX[1] = bias + K21 * (alphaZ - alpha);
        this.covX[0][0] = c11 * (1 - K11);
        this.covX[0][1] = c12 * (1 - K11);
        this.covX[1][0] = -c11 * K21 + c21;
        this.covX[1][1] = -K21 * c12 + c22;
    }
}
