package com.kang.jwt.bean.out;

public class ReturnOut<T>{
    private int resultCode=1;
    private String resultMessage="ok";
    private T data;


    public ReturnOut() {
    }

    public ReturnOut(T data) {
        this.data = data;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ReturnOut [data=" + data + ", resultCode=" + resultCode + ", resultMessage=" + resultMessage + "]";
    }
}