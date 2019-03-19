package com.bean;

import android.hardware.usb.UsbDevice;

import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.widget.UVCCameraTextureView;

public class UsbDevcieEntity {
    String floor;
    UsbDevice usbDevice;
    UVCCameraTextureView cameraTextureView;
    UVCCameraHandler handler;

    public UsbDevcieEntity(String floor, UsbDevice usbDevice, UVCCameraTextureView cameraTextureView, UVCCameraHandler handler) {
        this.floor = floor;
        this.usbDevice = usbDevice;
        this.cameraTextureView = cameraTextureView;
        this.handler = handler;
    }

    public String getFloor() {
        return floor == null ? "" : floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public UsbDevice getUsbDevice() {
        return usbDevice;
    }

    public void setUsbDevice(UsbDevice usbDevice) {
        this.usbDevice = usbDevice;
    }

    public UVCCameraTextureView getCameraTextureView() {
        return cameraTextureView;
    }

    public void setCameraTextureView(UVCCameraTextureView cameraTextureView) {
        this.cameraTextureView = cameraTextureView;
    }

    public UVCCameraHandler getHandler() {
        return handler;
    }

    public void setHandler(UVCCameraHandler handler) {
        this.handler = handler;
    }
}